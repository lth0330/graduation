package web.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
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
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring()
                .requestMatchers(path("/api/parking/**"))
                .requestMatchers(path("/api/gate/**"));
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // JWT 기반 API 서버라서 CSRF, Form Login, Basic Auth를 사용하지 않는다.
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {
                })
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // 브라우저의 CORS 사전 요청은 인증 없이 통과시킨다.
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // 로그인/회원가입/공개 조회 API는 토큰 없이 접근 가능하다.
                        .requestMatchers(HttpMethod.POST, "/api/web-admin/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/apartment-managers/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/apartment-managers").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/signup").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/find-id").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/reset-pw").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/apartments").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/app/parking-zones").permitAll()
                        .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()
                        // Python 객체인식/FastAPI 연동 API는 장비 서버에서 토큰 없이 호출한다.
                        .requestMatchers(HttpMethod.GET, "/api/parking/cars").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/parking/zone/{zoneName}").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/parking/entry").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/parking/exit").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/parking/update-plate").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/gate/check").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/gate/log").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/gate/unmatched").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/gate/assign-plate").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/gate/alert").permitAll()
                        .requestMatchers(path("/api/parking/**")).permitAll()
                        .requestMatchers(path("/api/gate/**")).permitAll()
                        // 앱 입주민 기능은 RESIDENT 권한의 JWT가 있어야 사용할 수 있다.
                        .requestMatchers(HttpMethod.GET, "/api/user-info").hasRole("RESIDENT")
                        .requestMatchers(path("/api/cars/**")).hasRole("RESIDENT")
                        .requestMatchers(path("/api/inquiries/**")).hasRole("RESIDENT")
                        .requestMatchers(path("/api/notifications/**")).hasRole("RESIDENT")
                        .requestMatchers(path("/api/device-token")).hasRole("RESIDENT")
                        .requestMatchers(path("/api/settings/**")).hasRole("RESIDENT")
                        .requestMatchers(path("/api/waitlist")).hasRole("RESIDENT")
                        .requestMatchers(HttpMethod.POST, "/api/visitor-entry").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/parking-update").permitAll()
                        // 웹 관리자 화면은 관리자 권한별로 접근 범위를 분리한다.
                        .requestMatchers(path("/api/apartment-managers/dashboard/**")).hasRole("APARTMENT_MANAGER")
                        .requestMatchers(path("/api/web-admin/**")).hasRole("WEB_ADMIN")
                        .requestMatchers(path("/api/resident-signup-requests/**")).hasRole("APARTMENT_MANAGER")
                        .requestMatchers(path("/api/residents/**")).hasRole("APARTMENT_MANAGER")
                        .requestMatchers(path("/api/vehicles/**")).hasRole("APARTMENT_MANAGER")
                        .requestMatchers(path("/api/parking-lots/**")).hasRole("APARTMENT_MANAGER")
                        .requestMatchers(path("/api/parking-zones/**")).hasRole("APARTMENT_MANAGER")
                        .requestMatchers(path("/api/manager-inquiries/**")).hasRole("APARTMENT_MANAGER")
                        .requestMatchers(path("/api/resident-inquiries/**")).hasRole("APARTMENT_MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/apartment-managers/{managerNo}/my-page").hasRole("APARTMENT_MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/apartment-managers/{managerNo}").hasRole("APARTMENT_MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/apartment-managers/{managerNo}").hasRole("WEB_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/apartment-managers").hasRole("WEB_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/apartment-managers/{managerNo}").hasAnyRole("WEB_ADMIN", "APARTMENT_MANAGER")
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
