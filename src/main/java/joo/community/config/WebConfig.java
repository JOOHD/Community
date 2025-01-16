package joo.community.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Duration;

@EnableWebMvc
@Configuration
@RequiredArgsConstructor
public class WebConfig  implements WebMvcConfigurer {

    private final MessageSource messageSource;
    private String location = "/Users/user/image/";

    /*
        -messages.properties
        ex)
            NotNull.user.name=사용자 이름은 필수입니다.
            Size.user.password=비밀번호는 최소 {min}자 이상이어야 합니다.
     */

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/image/**")
                .addResourceLocations("file:" + location)
                // image 접근할 때마다, 캐시 이용 제한 1시간, 지나면 재요청
                .setCacheControl(CacheControl.maxAge(Duration.ofHours(1L)).cachePublic());
    }

    @Override
    public Validator getValidator() {
        // LocalValidatorFactoryBean = javax.validation 을 Spring 환경에 통합할 때 사용
        LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
        bean.setValidationMessageSource(messageSource);
        return bean;
    }
}
