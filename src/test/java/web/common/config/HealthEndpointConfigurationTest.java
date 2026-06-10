package web.common.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class HealthEndpointConfigurationTest {

    @Test
    void securityAllowsPublicHealthEndpointsWithoutWebIgnoring() throws IOException {
        String securityConfig = Files.readString(Path.of("src/main/java/web/common/config/SecurityConfig.java"));

        assertThat(securityConfig).contains(
                "path(\"/\")",
                "path(\"/health\")",
                "path(\"/favicon.ico\")",
                "path(\"/error\")"
        );
        assertThat(securityConfig).doesNotContain("WebSecurityCustomizer", "web.ignoring()");
    }

    @Test
    void healthControllerProvidesRootAndHealthResponses() throws IOException {
        String healthController = Files.readString(
                Path.of("src/main/java/web/common/controller/HealthController.java")
        );

        assertThat(healthController).contains(
                "@GetMapping(\"/\")",
                "Spring Boot server is running",
                "@GetMapping(\"/health\")",
                "OK"
        );
    }
}
