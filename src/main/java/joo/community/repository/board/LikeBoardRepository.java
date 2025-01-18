package joo.community.repository.board;

import joo.community.entity.board.Board;
import joo.community.entity.board.LikeBoard;
import joo.community.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeBoardRepository  extends JpaRepository<LikeBoard, Long> {

    // 좋아요 누른 회원 찾기
    Optional<LikeBoard> findByBoardAndUser(Board board, User user);
}

