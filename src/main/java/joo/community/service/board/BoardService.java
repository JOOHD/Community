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
    private final FileService fileService;

    @Transactional // 게시글 생성
    public BoardCreateResponse create(BoardCreateRequest req) {
        // SecurityContextHolder 에서 현재 로그인된 사용자 정보(JWT token, session)를 가져온다.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // DB 조회 이유 : 인증된 사용자 정보가 반드시 DB 와 일치해야 된다.
        // 인증된 사용자라 하더라도 DB 에 삭제되었거나, 비활된 사용자일 수 있기 때문에.
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow(MemberNotFoundException::new);
        List<Image> images = req.getImages().stream()
                                            .map(i -> new Image(i.getOriginalFilename()))
                                            .collect(toList());

        Board board = boardRepository.save(new Board(req.getTitle(), req.getContent(), user, images));

        // board fileImages 저장소(AWS S3)에 업로드
        uploadImages(board.getImages(), req.getImages());
        
        return new BoardCreateResponse(board.getId(), board.getTitle(), board.getContent());
    }

    @Transactional(readOnly = true) // 게시글 전체 조회
    public List<BoardSimpleDto>  findAllBoards(Pageable pageable) {
        Page<Board> boards = boardRepository.findAll(pageable);
        List<BoardSimpleDto> boardSimpleDtoList = new ArrayList<>();
        boards.stream().forEach(i -> boardSimpleDtoList.add(new BoardSimpleDto().toDto(i)));
        return boardSimpleDtoList;
    }

    @Transactional(readOnly = true) // 게시글 단건 조회
    public BoardResponseDto findBoard(Long id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(BoardNotFoundException::new);

        User user = board.getUser();
        // writer_nickname은 User 엔티티에 속한 데이터로, 중복 저장 않으려는 설계 원칙에 따라 Board 에 포함되지 않습니다.
        return BoardResponseDto.toDto(board, user.getNickname());
    }

    @Transactional
    public BoardResponseDto editBoard(Long id, BoardUpdateRequest req) {

        Board board = boardRepository.findById(id).orElseThrow(BoardNotFoundException::new);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow(MemberNotFoundException::new);

        if (user != board.getUser()) { // DB != Client req
            throw new MemberNotEqualsException();
        }

        Board.ImageUpdatedResult result = board.update(req);

        uploadImages(result.getAddedImages(), result.getAddedImageFiles());
        deleteImages(result.getDeletedImages());
        return BoardResponseDto.toDto(board, user.getNickname());

    }

    // images = 게시글 이미지, fileImages = 클라이언트에서 전송한 실제 이미지 파일 리스트
    // req.getImages() = 작성자가 서버로 보낸 원본 이미지, board.getImages() = req.getImages 를 entity 로 변환하여 db에 저장될 images
    private void uploadImages(List<Image> images, List<MultipartFile> fileImages) {
        IntStream.range(0, images.size()).forEach(i -> fileService.upload(fileImages.get(i), images.get(i).getUniqueName()));
    }

    private void deleteImages(List<Image> images) {
        images.stream().forEach(i -> fileService.delete(i.getUniqueName()));
    }
}
