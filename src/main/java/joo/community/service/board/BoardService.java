package joo.community.service.board;

import joo.community.dto.board.*;
import joo.community.entity.board.Board;
import joo.community.entity.board.Favorite;
import joo.community.entity.board.Image;
import joo.community.entity.board.LikeBoard;
import joo.community.entity.user.User;
import joo.community.exception.*;
import joo.community.repository.board.BoardRepository;
import joo.community.repository.board.FavoriteRepository;
import joo.community.repository.board.LikeBoardRepository;
import joo.community.repository.user.UserRepository;
import joo.community.service.file.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Service
public class BoardService {

    // final 이 붙은 필드에 대해 @RequiredArgsConstructor 생성자 자동 생성.
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final LikeBoardRepository likeBoardRepository;
    private final FavoriteRepository favoriteRepository;

    private final FileService fileService;

    // 게시글 생성
    @Transactional
    public BoardCreateResponse createBoard(BoardCreateRequest req) {

        User user = getCurrentUser();

        List<Image> images = req.getImages().stream()
                .map(i -> new Image(i.getOriginalFilename()))
                .collect(toList());

        Board board = boardRepository.save(new Board(req.getTitle(), req.getContent(), user, images));

        // board fileImages 저장소(AWS S3)에 업로드
        uploadImages(board.getImages(), req.getImages());
        
        return new BoardCreateResponse(board.getId(), board.getTitle(), board.getContent());
    }

    // 게시글 전체 조회
    @Transactional(readOnly = true)
    public List<BoardSimpleDto> findAllBoards(Pageable pageable) {
        return boardRepository.findAll(pageable).stream()
                .map(BoardSimpleDto::toDto)
                .collect(Collectors.toList());
    }

    // 게시글 단건 조회
    @Transactional(readOnly = true)
    public BoardResponseDto findBoard(Long id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(BoardNotFoundException::new);

        // writer_nickname은 User 엔티티에 속한 데이터로, 중복 저장 않으려는 설계 원칙에 따라 Board 에 포함되지 않습니다.
        return BoardResponseDto.toDto(board, board.getUser().getNickname());
    }

    // 게시글 수정
    @Transactional
    public BoardResponseDto editBoard(Long id, BoardUpdateRequest req) {
        Board board = boardRepository.findById(id).orElseThrow(BoardNotFoundException::new);
        User user = getCurrentUser();

        validateUser(board, user);

        Board.ImageUpdatedResult result = board.update(req);

        uploadImages(result.getAddedImages(), result.getAddedImageFiles());
        deleteImages(result.getDeletedImages());

        return BoardResponseDto.toDto(board, user.getNickname());

    }

    // 게시글 삭제
    @Transactional
    public void deleteBoard(Long id) {
        Board board = boardRepository.findById(id).orElseThrow(BoardNotFoundException::new);
        User user = getCurrentUser();

        validateUser(board, user);

        boardRepository.delete(board);
    }

    // 게시글 검색
    @Transactional(readOnly = true)
    public List<BoardSimpleDto> searchBoard(String keyword, Pageable pageable) {
        return boardRepository.findByTitleContaining(keyword, pageable).stream()
                .map(BoardSimpleDto::toDto)
                .collect(Collectors.toList());
    }

    /*
    @Transactional
    public String likeBoard(Long id, User user) {
        Board board = boardRepository.findById(id)
                .orElseThrow(BoardNotFoundException::new);

        LikeBoard likeBoard = likeBoardRepository.findByBoardAndUser(board, user)
                .orElse(null);

        if (likeBoard == null) {
            // 유저가 글에 처음 좋아요를 누른 경우
            board.setLiked(board.getLiked() + 1); // 좋아요 수 증가
            LikeBoard newLikeBoard = new LikeBoard(board, user);
            likeBoardRepository.save(newLikeBoard); // LikeBoard 테이블 생성
            return "좋아요 처리 완료";
        } else {
            // 유저가 이미 좋아요를 누른 경우 (좋아요 취소)
            board.setLiked(board.getLiked() - 1); // 좋아요 수 감소
            likeBoardRepository.delete(likeBoard); // LikeBoard 테이블 삭제
            return "좋아요 취소";
        }
    }
    */

    // 게시글 좋아요 & 즐겨찾기
    @Transactional
    public String processBoardAndFavoriteState(Long id, User user, String action) {
        Board board = boardRepository.findById(id)
                .orElseThrow(BoardNotFoundException::new);

        if ("like".equals(action)) {
            return processLikeState(board, user);
        } else if ("favorite".equals(action)) {
            return processFavoriteState(board, user);
        }
        throw new IllegalArgumentException("Invalid action: " + action);
    }

    // '좋아요' 가 가장 많은 게시글
    @Transactional(readOnly = true)
    public List<BoardSimpleDto> findBestBoards(Pageable pageable, Long minimum) {
        return boardRepository.findByLikedGreaterThanEqual(pageable, minimum).stream()
                .map(BoardSimpleDto::toDto)
                .collect(toList());
    }

    // 파일 업로드
    private void uploadImages(List<Image> images, List<MultipartFile> fileImages) {
        IntStream.range(0, images.size())
                .forEach(i -> fileService.upload(fileImages.get(i), images.get(i).getUniqueName()));
    }

    // 파일 삭제
    private void deleteImages(List<Image> images) {
        images.forEach(image -> fileService.delete(image.getUniqueName()));
    }

    // 현재 사용자 가져오기
    // Controller 에서 파라미터의 @JwtAuth 를 이용하여 검증이 된 상태이므로 이 메서드를 생략 가능한 부분에서는 생략해도 괜찮다.
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(MemberNotFoundException::new);
    }

    // 작성자 검증
    private void validateUser(Board board, User user) {
        if (!board.getUser().equals(user)) {
            throw new MemberNotEqualsException();
        }
    }

    private String processLikeState(Board board, User user) {
        LikeBoard likeBoard = likeBoardRepository.findByBoardAndUser(board, user)
                .orElse(null);

        if (likeBoard == null) {
            // 유저가 글에 처음 좋아요를 누른 경우
            board.setLiked(board.getLiked() + 1); // 좋아요 수 증가
            likeBoardRepository.save(new LikeBoard(board, user)); // LikeBoard 테이블 생성
            return "좋아요 처리 완료";
        } else {
            // 유저가 이미 좋아요를 누른 경우 (좋아요 취소)
            board.setLiked(board.getLiked() - 1); // 좋아요 수 감소
            likeBoardRepository.delete(likeBoard); // LikeBoard 테이블 삭제
            return "좋아요 취소";
        }
    }

    private String processFavoriteState(Board board, User user) {
        Favorite favorite = favoriteRepository.findByBoardAndUser(board, user)
                .orElse(null);

        if (favorite == null) {
            // 유저가 글을 처음 즐겨찾기한 경우
            board.setFavorited(board.getFavorited() + 1); // 즐겨찾기 수 증가
            favoriteRepository.save(new Favorite(board, user)); // Favorite 테이블 생성
            return "즐겨찾기 처리 완료";
        } else {
            // 유저가 이미 즐겨찾기한 경우 (즐겨찾기 취소)
            board.setFavorited(board.getFavorited() - 1); // 즐겨찾기 수 감소
            favoriteRepository.delete(favorite); // Favorite 테이블 삭제
            return "즐겨찾기 취소";
        }
    }

    // 좋아요 확인
    public boolean hasLikeBoard(Board board, User user) {
        return likeBoardRepository.findByBoardAndUser(board, user)
                .isPresent();
    }

    // 좋아요 생성
    public String createLikeBoard(Board board, User user) {
        LikeBoard likeBoard = new LikeBoard(board, user);
        likeBoardRepository.save(likeBoard);
        return "좋아요 처리 완료";
    }

    // 좋아요 제거
    private String removeLikeBoard(Board board, User user) {
        LikeBoard likeBoard = likeBoardRepository.findByBoardAndUser(board, user)
                .orElseThrow(LikeHistoryNotfoundException::new);
        likeBoardRepository.delete(likeBoard);
        return "좋아요 취소 완료";
    }

    // 즐겨찾기 여부 확인
    private boolean hasFavoriteBoard(Board board, User user) {
        return favoriteRepository.findByBoardAndUser(board, user).isPresent();
    }
    
    // 즐겨찾기 추가
    public String createFavoriteBoard(Board board, User user) {
        Favorite favorite = new Favorite(board, user);
        favoriteRepository.save(favorite);
        return "즐겨찾기 추가"; // enum 상수로 리팩토링
    }
    
    // 즐겨찾기 제거
    public String removeFavoriteBoard(Board board, User user) {
        Favorite favorite = favoriteRepository.findByBoardAndUser(board, user)
                .orElseThrow(FavoriteNotFoundException::new);
        favoriteRepository.delete(favorite);
        return "즐겨찾기 취소"; // enum 상수로 리팩토링
    }
}
