package com.medicology.auth.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
    @Value("${frontend.url}")
    private String frontendUrl;
    @Value("${spring.mail.email}")
    private String fromEmail;
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private org.thymeleaf.TemplateEngine templateEngine;

    @Async
    public void sendVerificationEmail(String email, UUID token, String type) {
        org.thymeleaf.context.Context context = new org.thymeleaf.context.Context();

        context.setVariable("verifyUrl", frontendUrl + "/auth/" + type + "?token=" + token);
        context.setVariable("deleteUrl", frontendUrl + "/auth/delete?token=" + token);

        String content = templateEngine.process("EmailTemplate", context);
        MimeMessage message = mailSender.createMimeMessage();

        // BẮT ĐẦU KHỐI TRY ĐỂ XỬ LÝ LỖI
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("Xác thực tài khoản của bạn");
            helper.setText(content, true);

            mailSender.send(message); // Đừng quên dòng này để thực sự gửi mail đi nhé!

        } catch (jakarta.mail.MessagingException e) {
            // Nếu có lỗi (sai email, server mail sập...) nó sẽ nhảy vào đây
            throw new RuntimeException("Gửi mail thất bại: " + e.getMessage());
        }
    }
}
