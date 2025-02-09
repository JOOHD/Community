package joo.community.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import joo.community.entity.user.User;
;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByNickname(String nickname);

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByNickname(String nickname);

}
