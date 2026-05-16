package web.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import web.common.auth.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> {
                })
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(path(HttpMethod.OPTIONS, "/**")).permitAll()
                        .requestMatchers(path(HttpMethod.POST, "/api/web-admin/login")).permitAll()
                        .requestMatchers(path(HttpMethod.POST, "/api/apartment-managers/login")).permitAll()
                        .requestMatchers(path(HttpMethod.POST, "/api/apartment-managers")).permitAll()
                        .requestMatchers(path(HttpMethod.GET, "/uploads/**")).permitAll()
                        .requestMatchers(path("/api/apartment-managers/dashboard/**")).hasRole("APARTMENT_MANAGER")
                        .requestMatchers(path("/api/web-admin/**")).hasRole("WEB_ADMIN")
                        .requestMatchers(path("/api/resident-signup-requests/**")).hasRole("APARTMENT_MANAGER")
                        .requestMatchers(path("/api/residents/**")).hasRole("APARTMENT_MANAGER")
                        .requestMatchers(path("/api/vehicles/**")).hasRole("APARTMENT_MANAGER")
                        .requestMatchers(path("/api/parking-lots/**")).hasRole("APARTMENT_MANAGER")
                        .requestMatchers(path("/api/parking-zones/**")).hasRole("APARTMENT_MANAGER")
                        .requestMatchers(path("/api/manager-inquiries/**")).hasRole("APARTMENT_MANAGER")
                        .requestMatchers(path("/api/resident-inquiries/**")).hasRole("APARTMENT_MANAGER")
                        .requestMatchers(path(HttpMethod.GET, "/api/apartment-managers/{managerNo}/my-page")).hasRole("APARTMENT_MANAGER")
                        .requestMatchers(path(HttpMethod.PUT, "/api/apartment-managers/{managerNo}")).hasRole("APARTMENT_MANAGER")
                        .requestMatchers(path(HttpMethod.DELETE, "/api/apartment-managers/{managerNo}")).hasRole("WEB_ADMIN")
                        .requestMatchers(path(HttpMethod.GET, "/api/apartment-managers")).hasRole("WEB_ADMIN")
                        .requestMatchers(path(HttpMethod.GET, "/api/apartment-managers/{managerNo}")).hasAnyRole("WEB_ADMIN", "APARTMENT_MANAGER")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    private PathPatternRequestMatcher path(String pattern) {
        return PathPatternRequestMatcher.pathPattern(pattern);
    }

    private PathPatternRequestMatcher path(HttpMethod method, String pattern) {
        return PathPatternRequestMatcher.pathPattern(method, pattern);
    }
}
