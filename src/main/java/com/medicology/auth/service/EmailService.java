package com.medicology.auth.service;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.http.*;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Value("${frontend.url}")
    private String frontendUrl;

    @Value("${sendgrid.api.key}")
    private String apiKey;

    // Nên đưa email gửi vào file config
    @Value("${sendgrid.from-email}")
    private String fromEmail;

    private final org.thymeleaf.TemplateEngine templateEngine;
    private final RestTemplate restTemplate = new RestTemplate(); // Có thể bean hóa cái này

    public EmailService(org.thymeleaf.TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    @Async
    public void sendVerificationEmail(String email, UUID token, String type) {
        var context = new org.thymeleaf.context.Context();
        context.setVariable("verifyUrl", frontendUrl + "/auth/" + type + "?token=" + token);
        context.setVariable("deleteUrl", frontendUrl + "/auth/delete?token=" + token);

        String content = templateEngine.process("EmailTemplate", context);
        log.debug("sendgrid_verification_email compose_done email={}", email);
        String url = "https://api.sendgrid.com/v3/mail/send";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Cấu trúc chuẩn SendGrid v3 dùng List.of và Map.of
        Map<String, Object> body = Map.of(
                "personalizations", List.of(
                        Map.of("to", List.of(Map.of("email", email)))),
                "from", Map.of("email", fromEmail),
                "subject", "Xác thực tài khoản của bạn",
                "content", List.of(
                        Map.of("type", "text/html", "value", content)));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            if (response.getStatusCode() == HttpStatus.ACCEPTED) {
                log.info("sendgrid_accepted email={}", email);
            }
        } catch (HttpClientErrorException e) {
            log.error("sendgrid_error status={} body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        }
    }
}