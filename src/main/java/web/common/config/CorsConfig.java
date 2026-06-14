package web.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.LinkedHashSet;
import java.util.Set;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final String frontendUrl;

    public CorsConfig(@Value("${app.frontend-url:http://localhost:5173}") String frontendUrl) {
        this.frontendUrl = frontendUrl;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins())
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Authorization")
                .allowCredentials(true);
    }

    String[] allowedOrigins() {
        Set<String> origins = new LinkedHashSet<>();
        origins.add("http://localhost:5173");
        origins.add("http://127.0.0.1:5173");

        if (frontendUrl != null && !frontendUrl.isBlank()) {
            origins.add(frontendUrl.trim());
        }

        return origins.toArray(String[]::new);
    }
}
