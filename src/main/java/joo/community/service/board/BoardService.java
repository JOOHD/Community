package joo.community.service.board;

import joo.community.dto.board.*;
import joo.community.entity.board.Board;
import joo.community.entity.board.Image;
import joo.community.entity.user.User;
import joo.community.exception.BoardNotFoundException;
import joo.community.exception.MemberNotEqualsException;
import joo.community.exception.MemberNotFoundException;
import joo.community.repository.board.BoardRepository;
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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Service
public class BoardService {

    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
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
}
