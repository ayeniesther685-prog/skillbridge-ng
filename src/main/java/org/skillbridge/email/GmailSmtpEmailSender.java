package org.skillbridge.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.skillbridge.config.EmailProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * Sends email through whatever SMTP server is configured in spring.mail.*.
 *
 * For the beta, this is Gmail SMTP (smtp.gmail.com:587 + app password).
 * For production, swap SMTP_HOST/SMTP_PORT/SMTP_USERNAME/SMTP_PASSWORD to
 * Resend's SMTP or any other SMTP-capable provider — no code change needed.
 *
 * This class is transport-neutral despite the "Gmail" name; the name is
 * kept for clarity during the beta so operators know what to configure.
 */
@Component
public class GmailSmtpEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(GmailSmtpEmailSender.class);

    private final JavaMailSender mailSender;
    private final EmailProperties props;

    public GmailSmtpEmailSender(JavaMailSender mailSender, EmailProperties props) {
        this.mailSender = mailSender;
        this.props = props;
    }

    @Override
    public boolean send(String to, String subject, String htmlBody, String textBody) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    msg, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());

            helper.setFrom(props.getFrom(), "SkillBridge NG");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(textBody, htmlBody);

            mailSender.send(msg);
            log.info("Email sent to {} (subject: {})", to, subject);
            return true;
        } catch (MessagingException | UnsupportedEncodingException | RuntimeException e) {
            // Do not propagate — registration must not fail because SMTP is down.
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            return false;
        }
    }
}
