package joo.community.repository.board;

import joo.community.entity.board.Board;
import joo.community.entity.board.Favorite;
import joo.community.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    Optional<Favorite> findByBoardAndUser(Board board, User user);

    List<Favorite> findFavoriteByBoard(Board board);
}

