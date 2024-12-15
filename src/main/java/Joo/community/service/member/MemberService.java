package Joo.community.service.member;

import Joo.community.config.jwt.TokenProvider;
import Joo.community.domain.member.Member;
import Joo.community.domain.member.RefreshToken;
import Joo.community.dto.member.MemberEditRequestDto;
import Joo.community.dto.member.MemberSimpleResponseDto;
import Joo.community.dto.sign.TokenDto;
import Joo.community.dto.sign.TokenRequestDto;
import Joo.community.exception.MemberNotFoundException;
import Joo.community.exception.TokenExpiredException;
import Joo.community.repository.board.FavoriteRepository;
import Joo.community.repository.member.MemberRepository;
import Joo.community.repository.refreshToken.RefreshTokenRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final FavoriteRepository favoriteRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private TokenProvider tokenProvider;

    public MemberService(final MemberRepository memberRepository, final FavoriteRepository favoriteRepository, RefreshTokenRepository refreshTokenRepository) {
        this.memberRepository = memberRepository;
        this.favoriteRepository = favoriteRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    /*
        @Transactional(readOnly = true)
        public Object findAllUsers() {
            List<User> users = userRepository.findAll();
            List<UserDto> userDtos = new ArrayList<>();
            for (User user : users) {
                userDtos.add(UserDto.toDto(user));
            }
            return userDtos;
        }
    */

    @Transactional(readOnly = true)
    public List<MemberSimpleResponseDto> findAllMembers() {
        return memberRepository.findAll().stream()
                .map(MemberSimpleResponseDto::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MemberSimpleResponseDto findMember(final Long id) {
        Member member = memberRepository.findById(id).orElseThrow(MemberNotFoundException::new);
        return MemberSimpleResponseDto.toDto(member);
    }

    @Transactional
    public Member editMemberInfo(final Member member,
                                 final MemberEditRequestDto memberEditRequestDto,
                                 final TokenRequestDto tokenRequestDto) {

        /*
         * refreshToken 처리는 어떻게 할지 추후에 고민해보기
            ㄴ token 인증이된 상태에서 수정이 가능하도록 해야 보안 문제 발생 방지
         */

        // 1. AccessToken 유효성 검사
        if (tokenProvider.validateToken(tokenRequestDto.getAccessToken())) {
            // AccessToken 이 유요한 경우, 회원 정보 수정
            member.editUser(memberEditRequestDto);
        } else {
            // AccessToken 이 만료된 경우 RefreshToken 처리
            validateRefreshToken(tokenRequestDto);

            // 새로운 AccessToken 발급
            Authentication authentication = tokenProvider.getAuthentication(tokenRequestDto.getRefreshToken());
            TokenDto newToken = tokenProvider.generateTokenDto(authentication);

            // 새로운 AccessWToken 반환 로직 추가 가능
            // ex) resposne.setHeader("Authorization", "Bearer" + newToken.getAccessToken());
        }
        // 회원 정보 수정 후 Member 반환
        return member;
    }

    @Transactional
    public void deleteMemberInfo(final Member member, TokenRequestDto tokenRequestDto) {

        // 1. AccessToken 만료 여부 확인
        if (tokenProvider.isTokenExpired(tokenRequestDto.getAccessToken())) {
            throw new RuntimeException("Access Token이 만료되었습니다.");
        }

        // 2. RefreshToken 유효성 검사 및 일치 여부 확인
        if (!tokenProvider.validateToken(tokenRequestDto.getRefreshToken())) {
            throw new RuntimeException("RefreshToken이 유효하지 않습니다.");
        }

        // 현재 사용자에 대한 RefreshToken을 데이터베이스에서 확인
        RefreshToken refreshToken = refreshTokenRepository.findByKey(member.getUsername())
                .orElseThrow(() -> new RuntimeException("로그아웃된 사용자입니다."));

        // RefreshToken 일치 여부 확인
        if (!refreshToken.getValue().equals(tokenRequestDto.getRefreshToken())) {
            throw new RuntimeException("토큰의 유저 정보가 일치하지 않습니다.");
        }

        // 회원 삭제
        memberRepository.delete(member);
    }

    private void validateRefreshToken(TokenRequestDto tokenRequestDto) {

        // RefreshToken 유효성 검사
        if (!tokenProvider.validateToken(tokenRequestDto.getRefreshToken())) {
            throw new RuntimeException("RefreshToken 이 유효하지 않습니다.");
        }

        // RefreshToken 저장소에서 존재 여부 확인
        RefreshToken refreshToken = refreshTokenRepository.findByKey(
                tokenProvider.getAuthentication(tokenRequestDto.getAccessToken()).getName()
        ).orElseThrow(() -> new RuntimeException("로그아웃된 사용자입니다."));

        // RefreshToken 일치 여부 확인
        if (!refreshToken.getValue().equals(tokenRequestDto.getRefreshToken())) {
            throw new RuntimeException("토큰의 유저 정보가 일치하지 않습니다.");
        }
    }
}























