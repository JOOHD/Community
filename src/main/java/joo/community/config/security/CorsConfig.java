package joo.community.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        // 허용할 Origin
        config.setAllowedOrigins(List.of(
                "https://main--chimerical-malabi-ffde60.netlify.app",
                "http://localhost:3000"
        ));

        // 허용할 Header와 Method
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        // 쿠키 허용
        config.setAllowCredentials(true);
        // 설정 적용
        config.setMaxAge(3600L);
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }

}
