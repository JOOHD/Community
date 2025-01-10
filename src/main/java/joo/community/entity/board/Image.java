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

    private Image(final String originName) {
        this.originName = originName;
        this.uniqueName = generateUniqueName(extractExtension(originName));
    }

    public static Image from(final String originName) {
        return new Image(originName);
    }

    public void initBoard(final Board board) {
        if (this.board == null) {
            this.board = board;
        }
    }

    private String generateUniqueName(final String extension) {
        return UUID.randomUUID() + "." + extension;
    }

    private String extractExtension(final String originName) {
        String extension = originName.substring(originName.lastIndexOf(".") + 1);

        if (isSupportedFormat(extension)) {
            return extension;
        }

        throw new UnsupportedImageFormatException();
    }

    private boolean isSupportedFormat(final String extension) {
        return supportedExtensions.stream()
                .anyMatch(supportedExtension -> supportedExtension.equalsIgnoreCase(extension));
    }

//    public boolean isSameImageId(final int id) {
//        return this.getId() == id;
//    }
}
