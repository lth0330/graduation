package web;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

// web, app, python 패키지를 함께 스캔해서 웹/앱/Python 연동 API가 같은 서버에서 동작하게 한다.
@SpringBootApplication(scanBasePackages = {"web", "app", "python"})
@EntityScan(basePackages = {"web", "app", "python"})
@EnableJpaRepositories(basePackages = {"web", "app", "python"})
@EnableJpaAuditing
public class AppStart {
    public static void main(String[] args) {
        SpringApplication.run(AppStart.class);
    }
}
