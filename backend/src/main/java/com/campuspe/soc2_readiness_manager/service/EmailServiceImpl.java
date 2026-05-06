package com.campuspe.soc2_readiness_manager.service;

import com.campuspe.soc2_readiness_manager.entity.ReadinessItem;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private final String fromEmail;

    public EmailServiceImpl(
            JavaMailSender mailSender,
            SpringTemplateEngine templateEngine,
            @Value("${spring.mail.username}") String fromEmail) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.fromEmail = fromEmail;
    }

    @Override
    @Async
    public void sendItemCreatedNotification(ReadinessItem item) {
        log.info("Sending item created email to {}", item.getOwnerEmail());
        
        try {
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("item", item);
            
            Context thymeleafContext = new Context();
            thymeleafContext.setVariables(templateModel);
            
            String htmlBody = templateEngine.process("email/item-created.html", thymeleafContext);
            
            sendHtmlEmail(item.getOwnerEmail(), "New SOC 2 Readiness Task: " + item.getTitle(), htmlBody);
            
            log.info("Successfully sent item created email to {}", item.getOwnerEmail());
        } catch (Exception e) {
            log.error("Failed to send item created email to {}: {}", item.getOwnerEmail(), e.getMessage());
            // Do not throw the exception, we don't want to break the main application flow
        }
    }

    @Override
    @Async
    public void sendOverdueNotification(ReadinessItem item) {
        log.info("Sending item overdue email to {}", item.getOwnerEmail());
        
        try {
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("item", item);
            
            Context thymeleafContext = new Context();
            thymeleafContext.setVariables(templateModel);
            
            String htmlBody = templateEngine.process("email/item-overdue.html", thymeleafContext);
            
            sendHtmlEmail(item.getOwnerEmail(), "URGENT: Overdue SOC 2 Task: " + item.getTitle(), htmlBody);
            
            log.info("Successfully sent item overdue email to {}", item.getOwnerEmail());
        } catch (Exception e) {
            log.error("Failed to send item overdue email to {}: {}", item.getOwnerEmail(), e.getMessage());
            // Do not throw the exception
        }
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true); // true indicates HTML
        
        mailSender.send(message);
    }
}
