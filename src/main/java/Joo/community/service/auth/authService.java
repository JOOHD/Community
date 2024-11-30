package Joo.community.service.auth;

import Joo.community.config.constant.Constant;
import Joo.community.config.jwt.TokenProvider;
import Joo.community.domain.member.Authority;

import Joo.community.domain.member.Member;
import Joo.community.domain.member.RefreshToken;
import Joo.community.domain.point.Point;
import Joo.community.dto.sign.*;
import Joo.community.exception.LoginFailureException;
import Joo.community.exception.UsernameAlreadyExistsException;

import Joo.community.repository.member.MemberRepository;
import Joo.community.repository.point.PointRepository;
import Joo.community.repository.refreshToken.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class authService {

    private static final String RANKING_KEY = Constant.REDIS_RANKING_KEY;

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, String> redisTemplate;
    private final TokenProvider tokenProvider;

    private final MemberRepository memberRepository;
    private final PointRepository pointRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public Member signup(final SignUpRequestDto req) {
        validateSignUpInfo(req);
        Member member = createSignupFormOfUser(req);
        return member;
    }

    @Transactional
    public void savePointEntity(final Member member) {
        Point point = new Point(member);
        pointRepository.save(point);
        redisTemplate.opsForZSet().add(RANKING_KEY, member.getUsername(), point.getPoint());
    }

    @Transactional
    public TokenResponseDto signIn(final LoginRequestDto req) {
        Member member = memberRepository.findByUsername(req.getUsername()).orElseThrow(() -> {
            throw new LoginFailureException();
        });

        validatePassword(req, member);
        Authentication authentication = getUserAuthentication(req);
        TokenDto tokenDto = tokenProvider.generateTokenDto(authentication);
        RefreshToken refreshToken = buildRefreshToken(authentication, tokenDto);
        refreshTokenRepository.save(refreshToken);
        return new TokenResponseDto(tokenDto.getAccessToken(), tokenDto.getRefreshToken());
    }

    private RefreshToken buildRefreshToken(final Authentication authentication, TokenDto tokenDto) {
        return RefreshToken.builder()
                .key(authentication.getName())
                .value(tokenDto.getRefreshToken())
                .build();
    }

    private Authentication getUserAuthentication(final LoginRequestDto req) {
        UsernamePasswordAuthenticationToken authenticationToken = req.toAuthentication();
        return authenticationManagerBuilder.getObject().authenticate(authenticationToken);
    }

    private Member createSignupFormOfUser(final SignUpRequestDto req) {
        return Member.builder()
                .username(req.getUsername())
                .password(passwordEncoder.encode(req.getPassword()))
                .nickname(req.getNickname())
                .name(req.getName())
                .authority(Authority.ROLE_USER)
                .build();
    }

    private void validateSignUpInfo(final SignUpRequestDto signUpRequestDto) {
        if (memberRepository.existsByUsername(signUpRequestDto.getUsername())) {
            throw new UsernameAlreadyExistsException(signUpRequestDto.getUsername());
        }
    }

    private void validatePassword(final LoginRequestDto loginRequestDto, final Member member) {
        if (!passwordEncoder.matches(loginRequestDto.getPassword(), member.getPassword())) {
            throw new LoginFailureException();
        }
    }

    private void validateRefreshToken(final TokenRequestDto tokenRequestDto) {
        if (!tokenProvider.validateToken(tokenRequestDto.getRefreshToken())) {
            throw new RuntimeException("Refresh Token 이 유효하지 않습니다.");
        }
    }
}

