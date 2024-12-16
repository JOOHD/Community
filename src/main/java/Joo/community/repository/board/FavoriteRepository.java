package Joo.community.repository.board;

import org.springframework.data.jpa.repository.JpaRepository;
import Joo.community.domain.board.Board;
import Joo.community.domain.board.Favorite;
import Joo.community.domain.member.Member;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    Optional<Favorite> findByBoardAndMember(Board board, Member member);

    List<Favorite> findAllByMember(Member member);
}
