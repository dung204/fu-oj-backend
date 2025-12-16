package com.example.modules.email.service;

import com.example.modules.auth.entities.Account;
import com.example.modules.auth.repositories.AccountsRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class EmailService {

  private final JavaMailSender mailSender;
  private final TemplateEngine templateEngine;
  private final AccountsRepository accountsRepository;
  private final PasswordEncoder passwordEncoder;

  @Async
  public void sendEmailWithTemplate(
    String to,
    String subject,
    String templateName,
    Map<String, Object> variables
  ) throws MessagingException {
    Context context = new Context();
    context.setVariables(variables);

    String htmlContent = templateEngine.process(templateName, context);

    MimeMessage message = mailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

    helper.setTo(to);
    helper.setSubject(subject);
    helper.setText(htmlContent, true);

    mailSender.send(message);
  }
}
