package joo.community.config.security;

import joo.community.config.jwt.JwtAccessDeniedHandler;
import joo.community.config.jwt.JwtAuthenticationEntryPoint;
import joo.community.config.jwt.JwtSecurityConfig;
import joo.community.config.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsUtils;

import java.util.List;

@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final TokenProvider tokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    private static final String API_MESSAGES = "/api/messages";
    private static final String API_MESSAGES_SENDER = "/api/messages/sender";
    private static final String API_MESSAGES_SENDER_WITH_ID = "/api/messages/sender/{id}";
    private static final String API_MESSAGES_RECEIVER = "/api/messages/receiver";
    private static final String API_MESSAGES_RECEIVER_WITH_ID = "/api/messages/receiver/{id}";

    private static final String ROLE_USER_OR_ADMIN = "hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')";
    private static final String API_USERS = "/api/users";
    private static final String API_USERS_WITH_ID = "/api/users/{id}";

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // BCryptPasswordEncoder를 사용하여 비밀번호를 인코딩
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .antMatchers("/v2/api-docs", "/swagger-resources/**", "/swagger-ui.html", "/webjars/**", "/swagger/**");
    }

    @Bean // Spring Security 5 이상에서 권장되는 방식인 SecurityFilterChain을 사용
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // CSRF 설정 Disable
        http.csrf().disable();

        // CORS
        http.
                cors().configurationSource(request -> {
                    var cors = new CorsConfiguration();
                    cors.setAllowedOrigins(List.of("https://633ec6989ec820004a30086c--cheery-kheer-fe145b.netlify.app/"));
                    cors.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
                    cors.setAllowedHeaders(List.of("*"));
                    return cors;
                });

        http
                .authorizeRequests()
                .requestMatchers(CorsUtils::isPreFlightRequest).permitAll();

        // exception handling 할 때 우리가 만든 클래스를 추가
        http
                .exceptionHandling()
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler)
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                // 로그인, 회원가입 API 는 토큰이 없는 상태에서 요청이 들어오기 때문에 permitAll 설정
                .and()
                .authorizeRequests()

                .antMatchers("/auth/**").permitAll()
                .antMatchers("/swagger-ui/**", "/v3/**", "/test").permitAll() // swagger

                // API 경로별 권한 설정
                .antMatchers(HttpMethod.GET, API_USERS).access(ROLE_USER_OR_ADMIN)
                .antMatchers(HttpMethod.GET, API_USERS_WITH_ID).access(ROLE_USER_OR_ADMIN)
                .antMatchers(HttpMethod.PUT, API_USERS_WITH_ID).access(ROLE_USER_OR_ADMIN)
                .antMatchers(HttpMethod.DELETE, API_USERS_WITH_ID).access(ROLE_USER_OR_ADMIN)

                .antMatchers(HttpMethod.POST, API_MESSAGES).authenticated() // 메시지 전송은 인증 필요
                .antMatchers(HttpMethod.GET, API_MESSAGES_SENDER).access(ROLE_USER_OR_ADMIN)
                .antMatchers(HttpMethod.GET, API_MESSAGES_SENDER_WITH_ID).access(ROLE_USER_OR_ADMIN)
                .antMatchers(HttpMethod.GET, API_MESSAGES_RECEIVER).access(ROLE_USER_OR_ADMIN)
                .antMatchers(HttpMethod.GET, API_MESSAGES_RECEIVER_WITH_ID).access(ROLE_USER_OR_ADMIN)
                .antMatchers(HttpMethod.DELETE, API_MESSAGES_SENDER_WITH_ID).access(ROLE_USER_OR_ADMIN)
                .antMatchers(HttpMethod.DELETE, API_MESSAGES_RECEIVER_WITH_ID).access(ROLE_USER_OR_ADMIN)

                // 나머지 요청은 ROLE_ADMIN만 접근 가능
                .anyRequest().hasAnyRole("ROLE_ADMIN")

                // JWT 필터 추가
                .and()
                .apply(new JwtSecurityConfig(tokenProvider));

        return http.build(); // SecurityFilterChain 반환
    }
}
