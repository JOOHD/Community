package joo.community.dto.board;

import joo.community.entity.board.Board;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BoardSimpleDto {

    private Long id;
    private String title;
    private String nickname;
    private int liked;
    private int favorited;

    public static BoardSimpleDto toDto(Board board) {
        return new BoardSimpleDto(board.getId(), board.getTitle(), board.getUser().getNickname(), board.getLiked(),
                board.getFavorited());
    }
}
