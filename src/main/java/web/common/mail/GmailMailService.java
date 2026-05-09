package web.common.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GmailMailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public void sendApprovalMail(String toEmail, String receiverName, String targetName) {
        sendMail(
                toEmail,
                "[Park on] 승인 완료 안내",
                """
                안녕하세요, %s님.

                %s 신청이 승인되었습니다.
                아래 주소에서 로그인 후 서비스를 이용해주세요.

                %s
                """.formatted(receiverName, targetName, frontendUrl)
        );
    }

    public void sendRejectMail(String toEmail, String receiverName, String targetName, String rejectReason) {
        sendMail(
                toEmail,
                "[Park on] 신청 거절 안내",
                """
                안녕하세요, %s님.

                %s 신청이 거절되었습니다.

                거절 사유:
                %s
                """.formatted(receiverName, targetName, rejectReason)
        );
    }

    private void sendMail(String toEmail, String subject, String content) {
        if (toEmail == null || toEmail.isBlank()) {
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);
        } catch (MailException exception) {
            log.warn("메일 발송 실패: toEmail={}, subject={}", toEmail, subject, exception);
        }
    }
}
