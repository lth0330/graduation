package web.common.config;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CorsConfigTest {

    @Test
    void awsFrontendUrlIsIncludedInAllowedOrigins() {
        CorsConfig corsConfig = new CorsConfig("https://d3vvhygufn2oi5.cloudfront.net");

        List<String> allowedOrigins = List.of(corsConfig.allowedOrigins());

        assertThat(allowedOrigins)
                .contains("http://localhost:5173")
                .contains("http://127.0.0.1:5173")
                .contains("https://d3vvhygufn2oi5.cloudfront.net");
    }
}
