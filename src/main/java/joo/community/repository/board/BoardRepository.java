package joo.community.repository.board;

import joo.community.entity.board.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// findBoard(Long id) 로 인한 JpaRepository<Board, Integer -> Long)>
public interface BoardRepository extends JpaRepository<Board, Long> {

    // 검색
    List<Board> findByTitleContaining(String keyword, Pageable pageable);
    // 전체 게시글
    Page<Board> findAll(Pageable pageable);
}
