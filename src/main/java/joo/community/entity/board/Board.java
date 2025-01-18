package joo.community.entity.board;

import joo.community.dto.board.BoardUpdateRequest;
import joo.community.entity.user.User;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@Builder
@Entity
public class Board {

    /*
        conver~ method 클라이언트에서 전달받은 데이터는 일반적 (file, id등) 형태
        이 데이터를 entity 객체로 변환하여 domain model 사용 형태로 변환
        ex)
            MultipartFile -> Image, imageIds -> Image
     */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    @Lob // 대용량 데이터가 들어갈 수 있도록
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE) // User (작성자) 제거 시, 게시글 삭제.
    private User user;

    @OneToMany(mappedBy = "board", cascade = CascadeType.PERSIST, orphanRemoval = true) // image 고아객체 될 경우 DB에서 삭제
    private List<Image> images;

    @DateTimeFormat(pattern = "yyyy-mm-dd-ss")
    private LocalDateTime createDate; // 날짜

    @Column(nullable = true)
    private int liked; // 추천 수

    @Column(nullable = true)
    private int favorited; // 즐겨찾기 수

    @PrePersist // DB에 INSERT 되기 직전에 실행. 즉 DB에 값을 넣으면 자동으로 실행됨
    public void createDate() { // LocalDate : 날짜, LocalDateTime : 날짜 + 시간
        this.createDate = LocalDateTime.now();
    }

    // 추가 생성자 (명확한 객체 생성)
    public Board(String title,
                 String content,
                 User user,
                 List<Image> images) {
        this.title = title;
        this.content = content;
        this.user = user;
        this.liked = 0;
        this.favorited = 0;
        this.images = new ArrayList<>();
        addImages(images); // 코드 중복 방지 & 캡슐화
    }

    // 이미지 추가
    private void addImages(List<Image> added) { // 업로드된 이미지 파일 이름을 기반으로 Image.from() 호출
        added.stream().forEach(i -> {
            images.add(i);
            i.initBoard(this);
        });
    }

    // 수정 (title, content, add/delete image)
    public ImageUpdatedResult update(BoardUpdateRequest req) {
        this.title = req.getTitle();
        this.content = req.getContent();
        ImageUpdatedResult result = findImageUpdatedResult(req.getAddedImages(), req.getDeletedImages());
        addImages(result.getAddedImages());
        deleteImages(result.getDeletedImages());
        return result;
    }

    // 이미지 삭제
    private void deleteImages(List<Image> deleted) {
        deleted.stream().forEach(di -> this.images.remove(di));
    }

    // BoardUpdateRequest 받은 image 정보로, add/delete List<image> create
    // 각 변호나 메서드는 입력 데이터를 image 객체 리스트로 처리
    private ImageUpdatedResult findImageUpdatedResult(List<MultipartFile> addedImageFiles, List<Integer> deletedImageIds) {
        List<Image> addedImages = convertImageFilesToImages(addedImageFiles);
        List<Image> deletedImages = convertImageIdsToImages(deletedImageIds);
        return new ImageUpdatedResult(addedImageFiles, addedImages, deletedImages);
    }

    // MultipartFile -> Image 객체 리스트 변환
    // getOriginalFileName() 을 from 정적 메서드에 전달하여 Image 객체 생성
    private List<Image> convertImageFilesToImages(List<MultipartFile> imageFiles) {
        return imageFiles.stream()
                .map(imageFile -> Image.from(imageFile.getOriginalFilename())).collect(toList());
    }

    // imageIds 를 이용해 기존 Images -> Image 객체 변환
    private List<Image> convertImageIdsToImages(List<Integer> imageIds) {
        return imageIds.stream()
                .map(this::convertImageIdToImage) // 람다식을 메서드 참조로 변경
                .filter(Optional::isPresent) // get() 호출 전, isPresent() 체크, NullPointerException 방지.
                .map(Optional::get) // Optional 의 get 을 메서드 참조로 사용
                .collect(toList());
    }

    // 특정 ID 를 가진 Image 객체를 찾는다.
    private Optional<Image> convertImageIdToImage(int id) {
        return this.images.stream()
                .filter(i -> i.getId() == (id)).findAny(); // 첫 번째 일치하는 객체를 반환(Optional<Image>)
    }

    // 좋아요 증가
    public void increaseLikeCount() {
        this.liked += 1;
    }

    // 좋아요 감소
    public void decreaseLikeCount() {
        this.liked -= 1;
    }

    // 즐겨찾기 추가
    public void increaseFavoriteCount() {
        this.favorited += 1;
    }

    // 즐겨찾기 취소
    public void decreaseFavoriteCount() {
        this.favorited -= 1;
    }

    @Getter
    @AllArgsConstructor
    public static class ImageUpdatedResult { // 캡슐화하여 추가 및 삭제 리스트 관리
        private java.util.List<MultipartFile> addedImageFiles;
        private java.util.List<Image> addedImages;
        private java.util.List<Image> deletedImages;
    }
}
