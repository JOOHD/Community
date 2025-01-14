package joo.community.service.board;

import joo.community.dto.board.BoardCreateRequest;
import joo.community.dto.board.BoardCreateResponse;
import joo.community.entity.board.Board;
import joo.community.entity.board.Image;
import joo.community.entity.user.User;
import joo.community.exception.MemberNotFoundException;
import joo.community.repository.board.BoardRepository;
import joo.community.repository.user.UserRepository;
import joo.community.service.file.FileService;
import lombok.RequiredArgsConstructor;
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

    @Transactional
    public BoardCreateResponse create(BoardCreateRequest req) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow(MemberNotFoundException::new);
        List<Image> images = req.getImages().stream()
                                            .map(i -> new Image(i.getOriginalFilename()))
                                            .collect(toList());

        Board board = boardRepository.save(new Board(req.getTitle(), req.getContent(), user, images));

        // board fileImages 저장소(AWS S3)에 업로드
        uploadImages(board.getImages(), req.getImages());
        
        return new BoardCreateResponse(board.getId(), board.getTitle(), board.getContent());
    }

    // images = 게시글 이미지, fileImages = 클라이언트에서 전송한 실제 이미지 파일 리스트
    // req.getImages() = 작성자가 서버로 보낸 원본 이미지, board.getImages() = req.getImages 를 entity 로 변환하여 db에 저장될 images
    private void uploadImages(List<Image> images, List<MultipartFile> fileImages) {
        IntStream.range(0, images.size()).forEach(i -> fileService.upload(fileImages.get(i), images.get(i).getUniqueName()));
    }
}
