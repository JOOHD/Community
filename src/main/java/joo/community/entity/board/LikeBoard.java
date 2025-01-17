package joo.community.entity.board;

import joo.community.entity.user.User;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@Builder
@Entity
public class LikeBoard {

    // 사용자가 글에 좋아요를 누르면, 사용자 - 게시글 간 테이블이 생긴다.

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false) // FK
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Column(nullable = false)
    private boolean status; // true 좋아요, false 좋아요 취소

    @DateTimeFormat(pattern = "yyyy-mm-dd")
    private LocalDateTime createDate; // 날짜

    @PrePersist // DB 에 INSERT 되기 직전에 실행. 즉 DB에 값을 넣으면 자동으로 실행됨
    public void createDate() {
        this.createDate = LocalDateTime.now();
    }

    public LikeBoard(Board board, User user) {
        this.board = board;
        this.user = user;
        this.status = true;
    }

    public void unLikeBoard(Board board) {
        this.status = false;
        board.setLiked(board.getLiked() - 1);
    }
}
