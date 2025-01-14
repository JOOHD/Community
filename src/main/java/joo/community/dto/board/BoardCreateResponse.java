package joo.community.dto.board;

import joo.community.entity.board.Board;
import lombok.AllArgsConstructor;
import lombok.Data;
import joo.community.entity.board.Board;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NotBlank
public class BoardCreateResponse {

    private Long id;
    private String title;
    private String content;

    public static BoardCreateResponse toDto(Board board) {
        return new BoardCreateResponse(board.getId(), board.getTitle(), board.getContent());
    }
}
