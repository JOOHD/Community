package Joo.community.repository.member;

import org.springframework.data.jpa.repository.JpaRepository;
import Joo.community.domain.member.Member;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByNickname(String nickname);

    Optional<Member> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByNickname(String nickname);

    List<Member> findByReportedIsTrue();
}
