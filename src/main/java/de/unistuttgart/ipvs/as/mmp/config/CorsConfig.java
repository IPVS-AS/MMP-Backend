package de.unistuttgart.ipvs.as.mmp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private static final String[] ALLOWED_HEADERS = new String[]{"x-requested-with", "authorization", "Content-Type",
            "Authorization", "credential", "X-XSRF-TOKEN", "Location", "location", "crossdomain"};
    private static final String ALLOWED_METHODS = "GET, PUT, POST, DELETE, OPTIONS";
    private static final String ALLOWED_ORIGIN = "*";
    private static final long MAX_AGE = 3600;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(ALLOWED_ORIGIN)
                .allowedHeaders(ALLOWED_HEADERS)
                .allowedMethods(ALLOWED_METHODS)
                .exposedHeaders(ALLOWED_HEADERS)
                .maxAge(MAX_AGE)
                .allowCredentials(true);
    }
}
