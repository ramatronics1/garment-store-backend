package com.garmentstore.notification.channel;

import com.garmentstore.notification.domain.NotificationChannelType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.internet.MimeMessage;

/**
 * Email channel implementation using Spring Boot JavaMail (Gmail SMTP).
 * Disabled automatically if notification.email.enabled=false.
 *
 * WHY @Autowired(required = false) instead of @ConditionalOnBean(JavaMailSender.class):
 *   - @ConditionalOnBean on a @Component class is evaluated during component-scanning,
 *     BEFORE Spring Boot's autoconfiguration phase runs.
 *   - JavaMailSender is created by MailSenderAutoConfiguration (an autoconfig), so it
 *     does not exist yet at scan time → @ConditionalOnBean returns false → channel is skipped.
 *   - @Autowired injection happens AFTER all beans (including autoconfigured ones) are created,
 *     so JavaMailSender is reliably present at that point.
 *   - If mail is not configured, mailSender is null and send() throws an explanatory exception,
 *     which is caught by NotificationService and logged as a FAILED notification row.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "notification.email.enabled", havingValue = "true", matchIfMissing = true)
public class EmailNotificationChannel implements NotificationChannel {

    /** Null when spring.mail.username/password are not configured. */
    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Override
    public NotificationChannelType supports() {
        return NotificationChannelType.EMAIL;
    }

    @Override
    public void send(NotificationRequest request) {
        if (mailSender == null) {
            throw new RuntimeException(
                    "JavaMailSender not configured. Set spring.mail.username and spring.mail.password.");
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(request.getRecipient());
            helper.setSubject(request.getSubject() != null ? request.getSubject() : "Vastra Notification");
            // Send as HTML for rich formatting
            helper.setText(buildHtmlBody(request.getBody()), true);
            helper.setFrom("Vastra Fashion <noreply@vastra.in>");

            mailSender.send(message);
            log.info("[Email] Sent {} to {}", request.getType(), request.getRecipient());

        } catch (Exception e) {
            log.error("[Email] Failed to send {} to {}: {}", request.getType(), request.getRecipient(), e.getMessage(), e);
            throw new RuntimeException("Email send failed: " + e.getMessage(), e);
        }
    }

    /**
     * Wraps plain text body in a branded HTML email template.
     * This makes Vastra emails look professional — like Myntra/Ajio.
     */
    private String buildHtmlBody(String plainBody) {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
              <meta charset="UTF-8"/>
              <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
              <style>
                body { font-family: 'Segoe UI', Arial, sans-serif; background: #f8f8f8; margin:0; padding:0; }
                .wrapper { max-width: 600px; margin: 32px auto; background: #ffffff; border-radius: 12px;
                           box-shadow: 0 2px 16px rgba(0,0,0,0.08); overflow: hidden; }
                .header { background: linear-gradient(135deg, #1a1a2e 0%%, #16213e 100%%);
                          padding: 28px 32px; text-align: center; }
                .logo { font-size: 28px; font-weight: 800; color: #fff; letter-spacing: 4px; }
                .logo span { color: #e91e8c; }
                .body { padding: 32px; color: #2c2c2c; line-height: 1.7; font-size: 15px; }
                .footer { background: #f0f0f0; padding: 16px 32px; text-align: center;
                          font-size: 12px; color: #888; }
                .cta { display: inline-block; margin-top: 20px; padding: 12px 28px;
                       background: #e91e8c; color: #fff; border-radius: 6px;
                       text-decoration: none; font-weight: 600; }
              </style>
            </head>
            <body>
              <div class="wrapper">
                <div class="header">
                  <div class="logo">VAS<span>T</span>RA</div>
                </div>
                <div class="body">
                  %s
                </div>
                <div class="footer">
                  &copy; 2026 Vastra Fashion Pvt. Ltd. &bull; Unsubscribe from notifications in your account settings.
                </div>
              </div>
            </body>
            </html>
            """.formatted(plainBody.replace("\n", "<br/>"));
    }
}
