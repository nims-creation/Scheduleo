package com.saas.Schedulo.service.impl.email;

import com.saas.Schedulo.entity.organization.Organization;
import com.saas.Schedulo.entity.timetable.Timetable;
import com.saas.Schedulo.entity.user.User;
import com.saas.Schedulo.repository.user.UserRepository;
import com.saas.Schedulo.service.email.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;
    private final UserRepository userRepository;

    @Async
    @Override
    public void sendTimetablePublishedEmail(Timetable timetable) {
        Organization org = timetable.getOrganization();
        List<User> orgMembers = userRepository.findByOrganizationIdAndIsDeletedFalse(org.getId());

        log.info("Preparing to send Timetable Published emails to {} members of organization {}", orgMembers.size(), org.getName());

        for (User user : orgMembers) {
            if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                try {
                    sendHtmlEmail(user.getEmail(), "New Timetable Published: " + timetable.getName(), buildEmailContent(user, timetable));
                } catch (Exception e) {
                    log.error("Failed to send email to {}", user.getEmail(), e);
                }
            }
        }
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);
        
        javaMailSender.send(message);
        log.info("Sent timetable notification email to {}", to);
    }

    private String buildEmailContent(User user, Timetable timetable) {
        return "<html>" +
                "<body style='font-family: Arial, sans-serif; color: #333;'>" +
                "<div style='max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #eaeaea; border-radius: 8px;'>" +
                "<div style='text-align: center; margin-bottom: 20px;'>" +
                "<h1 style='color: #06b6d4;'>Schedulo</h1>" +
                "</div>" +
                "<h2>Hello " + user.getFirstName() + ",</h2>" +
                "<p>A new timetable has just been published for your organization: <strong>" + timetable.getOrganization().getName() + "</strong>.</p>" +
                "<div style='background-color: #f8fafc; padding: 15px; border-radius: 5px; margin: 20px 0;'>" +
                "<h3 style='margin-top: 0;'>" + timetable.getName() + "</h3>" +
                "<p><strong>Effective From:</strong> " + (timetable.getEffectiveFrom() != null ? timetable.getEffectiveFrom() : "Immediately") + "</p>" +
                "</div>" +
                "<p>Log in to your Schedulo dashboard to view your new schedule and room allocations.</p>" +
                "<div style='text-align: center; margin-top: 30px;'>" +
                "<a href='http://localhost:5173/dashboard' style='background-color: #4F46E5; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; font-weight: bold;'>View Timetable</a>" +
                "</div>" +
                "<p style='margin-top: 40px; font-size: 12px; color: #999; text-align: center;'>" +
                "© 2026 Schedulo SaaS Platform. All rights reserved.</p>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
}
