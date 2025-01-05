package joo.community.config.security;

import joo.community.config.jwt.JwtAccessDeniedHandler;
import joo.community.config.jwt.JwtAuthenticationEntryPoint;
import joo.community.config.jwt.JwtSecurityConfig;
import joo.community.config.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

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

    protected void configure(HttpSecurity http, WebSecurity web) throws Exception {

        web.ignoring()
                .antMatchers("/v2/api-docs", "/swagger-resources/**", "/swagger-ui.html", "/webjars/**", "/swagger/**");

        // CSRF 설정 Disable
        http.csrf().disable()

                // exception handling 할 때 우리가 만든 클래스를 추가
                .exceptionHandling()
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler)

                // 시큐리티는 기본적으로 세션을 사용
                // 여기서는 세션을 사용하지 않기 때문에 세션 설정을 Stateless 로 설정
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                .and()
                .authorizeRequests()
                .antMatchers("/swagger-ur/**", "/v3/**").permitAll()
                .antMatchers("/api/sign-up", "/api/sign-in", "/api/reissue").permitAll()
                .antMatchers("/test").permitAll()
                .anyRequest().permitAll() // 모든 요청 허용

                .antMatchers(HttpMethod.GET, API_USERS).access(ROLE_USER_OR_ADMIN)
                .antMatchers(HttpMethod.GET, API_USERS_WITH_ID).access(ROLE_USER_OR_ADMIN)
                .antMatchers(HttpMethod.PUT, API_USERS_WITH_ID).access(ROLE_USER_OR_ADMIN)
                .antMatchers(HttpMethod.DELETE, API_USERS_WITH_ID).access(ROLE_USER_OR_ADMIN)

                .antMatchers(HttpMethod.POST, API_MESSAGES).authenticated()
                .antMatchers(HttpMethod.GET, API_MESSAGES_SENDER).access(ROLE_USER_OR_ADMIN)
                .antMatchers(HttpMethod.GET, API_MESSAGES_SENDER_WITH_ID).access(ROLE_USER_OR_ADMIN)
                .antMatchers(HttpMethod.GET, API_MESSAGES_RECEIVER).access(ROLE_USER_OR_ADMIN)
                .antMatchers(HttpMethod.GET, API_MESSAGES_RECEIVER_WITH_ID).access(ROLE_USER_OR_ADMIN)
                .antMatchers(HttpMethod.DELETE, API_MESSAGES_SENDER_WITH_ID).access(ROLE_USER_OR_ADMIN)
                .antMatchers(HttpMethod.DELETE, API_MESSAGES_RECEIVER_WITH_ID).access(ROLE_USER_OR_ADMIN)

                .anyRequest().hasAnyRole("ROLE_ADMIN")
//                .anyRequest().authenticated() // 나머지는 전부 인증 필요
//                .anyRequest().permitAll()   // 나머지는 모두 그냥 접근 가능

                // JwtFilter 를 addFilterBefore 로 등록했던 JwtSecurityConfig 클래스를 적용
                .and()
                .apply(new JwtSecurityConfig(tokenProvider));
    }
}

