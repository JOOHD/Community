package joo.community.dto.board;

import static java.util.stream.Collectors.toList;

import joo.community.dto.image.ImageDto;
import joo.community.entity.board.Board;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BoardResponseDto {

    private Long id;
    private String writer_nickname;
    private String title;
    private String content;
    private int likeCount;
    private int favoriteCount;
    private List<ImageDto> images;
    private LocalDateTime createDate;

    // Board entity 는 User 를 참조하는 관계이다. writer_nickName 만 따로 선언한 이유는
    // board.getUser().getNickname() 을 호출해야 하지만, 이는 불필요한 User 전체를 로드 할 수 있기에..
    // 엔티티 설계 원칙: Board 엔티티에는 사용자 닉네임을 중복 저장하지 않기 때문.
    public static BoardResponseDto toDto(Board board, String writer_nickname) {
        return new BoardResponseDto(
            board.getId(),
            writer_nickname,
            board.getTitle(),
            board.getContent(),
            board.getLiked(),
            board.getFavorited(),
            board.getImages().stream().map(ImageDto::toDto).collect(toList()),
            board.getCreateDate()
        );
    }
}
