package joo.community.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.Arrays;
import java.util.List;

@Import(BeanValidatorPluginsConfiguration.class) // @Valid 을 Swagger에서 문서화 가능도록.
@Configuration
// http://localhost:8080/swagger-ui/index.html // swagger 경로
public class SwaggerConfig {

    @Bean
    public Docket api() {   // Docket : swagger 설정 핵심
        return new Docket(DocumentationType.OAS_30) // OpenAPI 3.0 설정
                .apiInfo(apiInfo()) // API name, info.... Swagger UI 상단 메타데이터 설정.
                .select()
                .apis(RequestHandlerSelectors.basePackage("community.controller")) // 문서화 대상.
                .paths(PathSelectors.any()) // 모든 엔드포인트 경로 포함.
                .build()
                .securityContexts(Arrays.asList(securityContext()))
                .securitySchemes(Arrays.asList(apiKey()));

    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Community")
                .description("Community REST API Documentation")
                .license("hddong728@naver.com")
                .licenseUrl("https://github.com/JOOHD/community")
                .version("1.0")
                .build();
    }

    private static ApiKey apiKey() {
        return new ApiKey("Authorization", "Bearer Token", "header");
    }

    private SecurityContext securityContext() {
        return SecurityContext.builder().securityReferences(defaultAuth())
                .operationSelector(oc -> oc.requestMappingPattern().startsWith("/api/")).build();
    }

    private List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "global access");
        return List.of(new SecurityReference("Authorization", new AuthorizationScope[] {authorizationScope}));
    }
}
