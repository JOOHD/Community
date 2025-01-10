package joo.community.entity.board;

import joo.community.dto.board.BoardUpdateRequest;
import joo.community.entity.user.User;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.*;
import java.time.LocalDate;
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

    @DateTimeFormat(pattern = "yyyy-mm-dd")
    private LocalDate createDate; // 날짜

    @PrePersist // DB에 INSERT 되기 직전에 실행. 즉 DB에 값을 넣으면 자동으로 실행됨
    public void createDate() {
        this.createDate = LocalDate.now();
    }

    // 추가 생성자 (명확한 객체 생성)
    public Board(String title, String content, User user, List<Image> images) {
        this.title = title;
        this.content = content;
        this.user = user;
        this.images = new ArrayList<>();
        addImages(images); // 코드 중복 방지 & 캡슐화
    }

    // 수정
    public ImageUpdatedResult update(BoardUpdateRequest req) {
        this.title = req.getTitle();
        this.content = req.getContent();
        ImageUpdatedResult result = findImageUpdatedResult(req.getAddedImages(), req.getDeletedImages());
        addImages(result.getAddedImages());
        deleteImages(result.getDeletedImages());
        return result;
    }

    // 이미지 추가
    private void addImages(List<Image> added) { // 업로드된 이미지 파일 이름을 기반으로 Image.from() 호출
        added.stream().forEach(i -> {
            images.add(i);
            i.initBoard(this);
        });
    }

    // 이미지 삭제
    private void deleteImages(List<Image> deleted) {
        deleted.stream().forEach(di -> this.images.remove(di));
    }

    private ImageUpdatedResult findImageUpdatedResult(List<MultipartFile> addedImageFiles, List<Integer> deletedImageIds) {
        List<Image> addedImages = convertImageFilesToImages(addedImageFiles);
        List<Image> deletedImages = convertImageIdsToImages(deletedImageIds);
        return new ImageUpdatedResult(addedImageFiles, addedImages, deletedImages);
    }

    private List<Image> convertImageIdsToImages(List<Integer> imageIds) {
        return imageIds.stream()
                .map(id -> convertImageIdToImage(id))
                .filter(i -> i.isPresent())
                .map(i -> i.get())
                .collect(toList());
    }

    private Optional<Image> convertImageIdToImage(int id) {
        return this.images.stream()
                .filter(i -> i.getId() == (id)).findAny();
    }
    private List<Image> convertImageFilesToImages(List<MultipartFile> imageFiles) {
        return imageFiles.stream()
                .map(imageFile -> Image.from(imageFile.getOriginalFilename())).collect(toList());
    }

    @Getter
    @AllArgsConstructor
    public static class ImageUpdatedResult {
        private java.util.List<MultipartFile> addedImageFiles;
        private java.util.List<Image> addedImages;
        private java.util.List<Image> deletedImages;
    }
}
