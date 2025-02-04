package joo.community.config.jwt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    public static final String AUTHORIZATION_HEADER = "Authorization";

    private final TokenProvider tokenProvider;

    // 실제 필터링 로직은 doFilterInternal 에 들어간다.
    // JWT 토큰의 인증 정보(Authentication)를 현재 쓰레드의 SecurityContext 에 저장하는 역할.
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {

        // 1. Request Header 에서 토큰을 꺼냄
        String jwt = resolveToken(request);

        log.info("------------------------------------------token received: " + jwt);

        // 2. validateToken 으로 토큰 유효성 검사
        // if = true 토큰이면, 해당 토큰으로 Authentication 을 가져와서 SecurityContext 에 저장.
        // 토큰 유효성 검사와 관계없이 항상 실행되어야 하는 로직
        if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
            Authentication authentication = tokenProvider.getAuthentication(jwt);
            log.info("------------------------------------------authenticated User : " + authentication.getName());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            log.warn("------------------------------------------token is invalid or missing");
        }

        // if문 안에 있어서, postman response Body 에 응답이 없던 것
        filterChain.doFilter(request, response); // 유효성 검증 실패 시, jwtHandler 로 넘겨진다.
    }

    // Request Header 에서 토큰 정보를 꺼내오기
    private String resolveToken(HttpServletRequest request) {

        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        // bearerToken 이 null 이 아니면서 공백이 아닌 경우 & "Bearer " 로 시작하는 경우에만 실행됨
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7).trim(); // "Bearer " 이후의 실제 JWT 토큰 값만 반환
        }

        return null;
    }
}

