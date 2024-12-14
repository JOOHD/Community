package Joo.community.repository.board;

import org.springframework.data.jpa.repository.JpaRepository;
import yoon.community.domain.board.Board;
import yoon.community.domain.board.LikeBoard;
import yoon.community.domain.member.Member;

import java.util.Optional;

public interface LikeBoardRepository extends JpaRepository<LikeBoard, Long> {

    Optional<LikeBoard> findByBoardAndMember(Board board, Member member);
}
