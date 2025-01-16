package joo.community.entity.board;

import joo.community.entity.common.EntityDate;
import joo.community.exception.UnsupportedImageFormatException;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import lombok.*;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Setter
public class Image extends EntityDate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String uniqueName;

    @Column(nullable = false)
    private String originName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE) // 게시판 삭제 시, 관련 이미지 함께 삭제
    private Board board;

    private final static List<String> supportedExtensions = List.of("jpg", "jpeg", "gif", "bmp", "png");

    // from 메서드가 호출되면 image class method 작동.
    public Image(final String originName) {
        this.originName = originName; // 원본 파일명 필드에 저장.
        this.uniqueName = generateUniqueName(extractExtension(originName)); // 고유 이름 생성.
    }

    public static Image from(final String originName) {
        return new Image(originName);
    }

    public void initBoard(final Board board) {
        if (this.board == null) {
            this.board = board;
        }
    }

    // 고유 이름 생성
    private String generateUniqueName(final String extension) {
        return UUID.randomUUID() + "." + extension;
    }

    // 확정자 추출 & 검증
    private String extractExtension(final String originName) {
        String extension = originName.substring(originName.lastIndexOf(".") + 1);
        if (isSupportedFormat(extension)) { // 파일 확장자 검증
            return extension;
        }
        throw new UnsupportedImageFormatException();
    }

    private boolean isSupportedFormat(final String extension) {
        return supportedExtensions.stream()
                .anyMatch(supportedExtension -> supportedExtension.equalsIgnoreCase(extension));
    }
}
