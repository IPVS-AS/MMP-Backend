package de.unistuttgart.ipvs.as.mmp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("de.unistuttgart.ipvs.as.mmp"))
                .paths(PathSelectors.ant("/v1/**"))
                .build()
                .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        return new ApiInfo(
                "MMP Backend REST API",
                "The backend rest api is used to manage the users, projects and models of the mmp application.",
                "V1",
                "Terms of service",
                new Contact("EnpRo MMP", "http://192.168.209.234", "mmpUniStuttgart@gmail.com"),
                "License of API", "API license URL", Collections.emptyList());
    }
}