package joo.community.service.board;

import joo.community.dto.board.*;
import joo.community.entity.board.Board;
import joo.community.entity.board.Favorite;
import joo.community.entity.board.Image;
import joo.community.entity.board.LikeBoard;
import joo.community.entity.user.User;
import joo.community.exception.BoardNotFoundException;
import joo.community.exception.FavoriteNotFoundException;
import joo.community.exception.MemberNotEqualsException;
import joo.community.exception.MemberNotFoundException;
import joo.community.repository.board.BoardRepository;
import joo.community.repository.board.FavoriteRepository;
import joo.community.repository.board.LikeBoardRepository;
import joo.community.repository.user.UserRepository;
import joo.community.service.file.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Service
public class BoardService {

    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final LikeBoardRepository likeBoardRepository;
    private final FavoriteRepository favoriteRepository;

    private final FileService fileService;

    public BoardService(final BoardRepository boardRepository, final FileService fileService, final LikeBoardRepository likeBoardRepository, final FavoriteRepository favoriteRepository, UserRepository userRepository) {
        this.boardRepository = boardRepository;
        this.fileService = fileService;
        this.likeBoardRepository = likeBoardRepository;
        this.favoriteRepository = favoriteRepository;
        this.userRepository = userRepository;
    }

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
//        boards.stream().forEach(i -> boardSimpleDtoList.add(new BoardSimpleDto().toDto(i)));
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
    public List<BoardSimpleDto> search(String keyword, Pageable pageable) {
//        boards.stream().forEach(i -> boardSimpleDtoList.add(new BoardSimpleDto().toDto(i)));
        return boardRepository.findByTitleContaining(keyword, pageable).stream()
                .map(BoardSimpleDto::toDto)
                .collect(Collectors.toList());
    }

    // 게시글 좋아요
    @Transactional
    public String likeBoard(Long id) {
        Board board = boardRepository.findById(id).orElseThrow(BoardNotFoundException::new);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow(MemberNotFoundException::new);

        if(likeBoardRepository.findByBoardAndUser(board, user) == null) {
            // 좋아요를 누른적 없다면 LikeBoard 생성 후, 좋아요 처리
            board.setLiked(board.getLiked() + 1);
            LikeBoard likeBoard = new LikeBoard(board, user); // true 처리
            likeBoardRepository.save(likeBoard);
            return "좋아요 처리 완료";
        } else {
            // 좋아요를 누른적 있다면 취소 처리 후 테이블 삭제
            LikeBoard likeBoard = likeBoardRepository.findByBoardAndUser(board, user);
            likeBoard.unLikeBoard(board);
            likeBoardRepository.delete(likeBoard);
            return "좋아요 취소";
        }
    }

    // 게시글 즐겨찾기
    @Transactional
    public String updateOfFavoriteBoard(Long id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(BoardNotFoundException::new);

        User user = getCurrentUser();

        /*
        if(favoriteRepository.findByBoardAndUser(board, user) == null) {
            // 좋아요를 누른적 없다면 Favorite 생성 후, 즐겨찾기 처리
            board.setFavorited(board.getFavorited() + 1);
            Favorite favorite = new Favorite(board, user); // true 처리
            favoriteRepository.save(favorite);
            return "즐겨찾기 처리 완료";
        } else {
            // 즐겨찾기 누른적 있다면 즐겨찾기 처리 후 테이블 삭제
            Favorite favorite = favoriteRepository.findFavoriteByBoard(board);
            favorite.unFavoriteBoard(board);
            favoriteRepository.delete(favorite);
            return "즐겨찾기 취소";
        }
        */
        return removeFavoriteBoard(board, user);
    }

    // '좋아요' 가 가장 많은 게시글
    @Transactional(readOnly = true)
    public List<BoardSimpleDto> findBestBoards(Pageable pageable, Long minimum) {
//        boards.stream().forEach(i -> boardSimpleDtoList.add(new BoardSimpleDto().toDto(i)));
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
    
    //
    public String createFavoriteBoard(Board board, User user) {
        Favorite favorite = new Favorite(board, user);
        favoriteRepository.save(favorite);
        return "즐겨찾기 추가"; // enum 상수로 리팩토링
    }
    
    // 즐겨찾기 취소
    public String removeFavoriteBoard(Board board, User user) {
        Favorite favorite = favoriteRepository.findByBoardAndUser(board, user)
                .orElseThrow(FavoriteNotFoundException::new);
        
        favoriteRepository.delete(favorite);
        
        return "즐겨찾기 취소"; // enum 상수로 리팩토링
    }
}
