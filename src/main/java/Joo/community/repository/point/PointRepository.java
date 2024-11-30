package Joo.community.repository.point;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import Joo.community.domain.point.Point;

public interface PointRepository extends JpaRepository<Point, Long> {

    Page<Point> findAll(Pageable pageable);
}
