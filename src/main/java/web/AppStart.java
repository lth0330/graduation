package web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling; // 👈 1. 스케줄러 도구 가져오기 추가!

// web, app, python 패키지를 함께 스캔해서 앱/웹/Python 연동 API가 같은 서버에서 동작하게 한다.
@EnableScheduling // 👈 2. 백그라운드 타이머 스위치 ON! (방문 차량 자동 삭제 작동)
@SpringBootApplication(scanBasePackages = {"web", "app", "python"})
@EntityScan(basePackages = {"web", "app", "python"})
@EnableJpaRepositories(basePackages = {"web", "app", "python"})
@EnableJpaAuditing
public class AppStart {
    public static void main(String[] args) {
        SpringApplication.run(AppStart.class, args); // 💡 (참고: 기존 코드 괄호 안에 빠져있던 args도 안전하게 채워 넣었습니다)
    }
}