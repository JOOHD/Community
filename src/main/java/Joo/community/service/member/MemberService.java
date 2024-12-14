package Joo.community.service.member;

import Joo.community.dto.member.MemberSimpleResponseDto;
import Joo.community.repository.board.FavoriteRepository;
import Joo.community.repository.member.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final FavoriteRepository favoriteRepository;

    public MemberService(final MemberRepository memberRepository, final FavoriteRepository favoriteRepository) {
        this.memberRepository = memberRepository;
        this.favoriteRepository = favoriteRepository;
    }

    @Transactional(readOnly = true)
    public List<MemberSimpleResponseDto> findAllMembers() {
        return memberRepository.findAll().stream()
                .map(MemberSimpleResponseDto::toDto)
                .collect(Collectors.toList());
    }
}
