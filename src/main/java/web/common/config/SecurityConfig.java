package web.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import web.common.auth.JwtAuthenticationFilter;

@Configuration
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
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/web-admin/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/apartment-managers/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/apartment-managers").permitAll()
                        .requestMatchers("/api/web-admin/**").hasRole("WEB_ADMIN")
                        .requestMatchers("/api/resident-signup-requests/**").hasRole("APARTMENT_MANAGER")
                        .requestMatchers("/api/residents/**").hasRole("APARTMENT_MANAGER")
                        .requestMatchers("/api/vehicles/**").hasRole("APARTMENT_MANAGER")
                        .requestMatchers("/api/parking-lots/**").hasRole("APARTMENT_MANAGER")
                        .requestMatchers("/api/parking-zones/**").hasRole("APARTMENT_MANAGER")
                        .requestMatchers("/api/manager-inquiries/**").hasRole("APARTMENT_MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/apartment-managers/*/my-page").hasRole("APARTMENT_MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/apartment-managers/*").hasRole("APARTMENT_MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/apartment-managers/*").hasRole("WEB_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/apartment-managers").hasRole("WEB_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/apartment-managers/*").hasAnyRole("WEB_ADMIN", "APARTMENT_MANAGER")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
