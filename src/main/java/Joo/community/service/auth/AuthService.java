package Joo.community.service.auth;

import Joo.community.config.constant.Constant;
import Joo.community.config.jwt.TokenProvider;
import Joo.community.domain.member.Authority;

import Joo.community.domain.member.Member;
import Joo.community.domain.member.RefreshToken;
import Joo.community.domain.member.User;
import Joo.community.domain.point.Point;
import Joo.community.dto.sign.*;
import Joo.community.exception.LoginFailureException;
import Joo.community.exception.MemberNicknameAlreadyExistsException;
import Joo.community.exception.MemberUsernameAlreadyExistsException;
import Joo.community.exception.UsernameAlreadyExistsException;

import Joo.community.repository.member.MemberRepository;
import Joo.community.repository.member.UserRepository;
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

import java.sql.Ref;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String RANKING_KEY = Constant.REDIS_RANKING_KEY;

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, String> redisTemplate;
    private final TokenProvider tokenProvider;

    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final PointRepository pointRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public void signup(RegisterDto req) {
        validateSignUpInfo(req);

        User user = new User();
        user.setUsername(req.getUsername());
    }

    /*
    @Transactional
    public Member signup(final SignUpRequestDto req) {
        validateSignUpInfo(req);
        Member member = createSignupFormOfUser(req);
        return member;
    }
    */

    @Transactional
    public void savePointEntity(final Member member) {
        Point point = new Point(member);
        pointRepository.save(point);
        redisTemplate.opsForZSet().add(RANKING_KEY, member.getUsername(), point.getPoint());
    }

    @Transactional
    public TokenResponseDto signIn(LoginRequestDto req) {
        User user = userRepository.findByUsername(req.getUsername()).orElseThrow(() -> {
            return new LoginFailureException();
        });

        validatePassword(req, user);

        // 1. Login ID/PW 를 기반으로 AuthenticationToken 생성
        UsernamePasswordAuthenticationToken authenticationToken = req.toAuthentication();

        // 2. 실제로 검증 (사용자 비밀번호 체크) 이 이루어지는 부분
        // authenticate 메서드가 실행이 될 때, CustomUserDetailsService 에서 만들었던 loadUserByUsername 메서드가 실행됨.
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        // 3. 인증 정보를 기반으로 JWT 토큰 생성.
        TokenDto tokenDto = tokenProvider.generateTokenDto(authentication);

        // 4. RefreshToken 저장.
        RefreshToken refreshToken = RefreshToken.builder()
                .key(authentication.getName())
                .value(tokenDto.getRefreshToken())
                .build();

        refreshTokenRepository.save(refreshToken);
        TokenResponseDto tokenResponseDto = new TokenResponseDto(tokenDto.getAccessToken(),tokenDto.getRefreshToken());

        // 5. 토큰 발급
        return tokenResponseDto;
    }

    /*
    @Transactional
    public TokenResponseDto signIn(final LoginRequestDto req) {
        Member member = memberRepository.findByUsername(req.getUsername()).orElseThrow(() -> {
            throw new LoginFailureException();
        });

        validatePassword(req, member);

        // 2. 실제로 검증 (사용자 비밀번호 체크) 이 이루어지는 부분
        //    authenticate 메서드가 실행이 될 때 CustomUserDetailsService 에서 만들었던 loadUserByUsername 메서드가 실행됨
        Authentication authentication = getUserAuthentication(req);

        // 3. 인증 정보를 기반으로 JWT 토큰 생성
        TokenDto tokenDto = tokenProvider.generateTokenDto(authentication);

        // 4. RefreshToken 저장
        RefreshToken refreshToken = buildRefreshToken(authentication, tokenDto);
        refreshTokenRepository.save(refreshToken);
        
        // 5. 토큰 발급
        return new TokenResponseDto(tokenDto.getAccessToken(), tokenDto.getRefreshToken());
    }
    */

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

    @Transactional
    public TokenResponseDto reissue(TokenRequestDto tokenRequestDto) {

        // 1. RefreshToken 검증
        if (!tokenProvider.validateToken(tokenRequestDto.getRefreshToken())) {
            throw new RuntimeException("RefreshToken 이 유효하지 않습니다.");
        }

        // 2. AccessToken 에서 Member ID 가져오기
        Authentication authentication = tokenProvider.getAuthentication(tokenRequestDto.getAccessToken());

        // 3. 저장소에서 Member ID 를 기반으로 RefreshToken 값 가져옴
        RefreshToken refreshToken = refreshTokenRepository.findByKey(authentication.getName())
                .orElseThrow(() -> new RuntimeException("로그아웃 된 사용자입니다."));

        // 4. RefreshToken 일치하는지 검사
        if (!refreshToken.getValue().equals(tokenRequestDto.getRefreshToken())) {
            throw new RuntimeException("토큰의 유지 정보가 일치하지 않습니다.");
        }

        // 5. 새로운 토큰 생성
        TokenDto tokenDto = tokenProvider.generateTokenDto(authentication);

        // 6. 저장소 정보 업데이트
        RefreshToken newRefreshToken = refreshToken.updateValue(tokenDto.getRefreshToken());
        refreshTokenRepository.save(newRefreshToken);

        // 7. 토큰 발급
        TokenResponseDto tokenResponseDto = new TokenResponseDto(tokenDto.getAccessToken(), tokenDto.getRefreshToken());

        return tokenResponseDto;
    }

    /*
    @Transactional
    public TokenResponseDto reissue(final TokenRequestDto tokenRequestDto) {
        validateRefreshToken(tokenRequestDto);

        Authentication authentication = tokenProvider.getAuthentication(tokenRequestDto.getAccessToken());
        RefreshToken refreshToken = refreshTokenRepository.findByKey(authentication.getName())
                .orElseThrow(() -> new RuntimeException("로그아웃 된 사용자입니다."));

        validateRefreshTokenOwner(refreshToken, tokenRequestDto);

        TokenDto tokenDto = tokenProvider.generateTokenDto(authentication);
        RefreshToken newRefreshToken = refreshToken.updateValue(tokenDto.getRefreshToken());
        refreshTokenRepository.save(newRefreshToken);

        return new TokenResponseDto(tokenDto.getAccessToken(), tokenDto.getRefreshToken());
    }
    */

    private Member createSignupFormOfUser(final SignUpRequestDto req) {
        return Member.builder()
                .username(req.getUsername())
                .password(passwordEncoder.encode(req.getPassword()))
                .nickname(req.getNickname())
                .name(req.getName())
                .authority(Authority.ROLE_USER)
                .build();
    }

    private void validateSignUpInfo(RegisterDto registerDto) {
        if (userRepository.existsByUsername(registerDto.getUsername()))
            throw new MemberUsernameAlreadyExistsException(registerDto.getUsername());
        if (userRepository.existsByNickname(registerDto.getNickname()))
            throw new MemberNicknameAlreadyExistsException(registerDto.getNickname());
    }

    private void validatePassword(LoginRequestDto loginRequestDto, User user) {
        if (!passwordEncoder.matches(loginRequestDto.getPassword(), user.getPassword())) {
            throw new LoginFailureException();
        }
    }

    /*
    private void validateSignUpInfo(final SignUpRequestDto signUpRequestDto) {
        if (memberRepository.existsByUsername(signUpRequestDto.getUsername())) {
            throw new UsernameAlreadyExistsException(signUpRequestDto.getUsername());
        }

        if (memberRepository.existsByNickname(signUpRequestDto.getNickname())) {
            throw new MemberNicknameAlreadyExistsException(signUpRequestDto.getNickname());
        }
    }


    private void validatePassword(final LoginRequestDto loginRequestDto, final Member member) {
        if (!passwordEncoder.matches(loginRequestDto.getPassword(), member.getPassword())) {
            throw new LoginFailureException();
        }
    }
    */

    private void validateRefreshToken(final TokenRequestDto tokenRequestDto) {
        if (!tokenProvider.validateToken(tokenRequestDto.getRefreshToken())) {
            throw new RuntimeException("Refresh Token 이 유효하지 않습니다.");
        }
    }

    private void validateRefreshTokenOwner(final RefreshToken refreshToken, final TokenRequestDto tokenRequestDto) {
        if (!refreshToken.getValue().equals(tokenRequestDto.getRefreshToken())) {
            throw new RuntimeException("토큰의 유저 정보가 일치하지 않습니다.");
        }
    }
}

