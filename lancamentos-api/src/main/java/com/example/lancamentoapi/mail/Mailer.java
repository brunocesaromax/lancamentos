package com.example.lancamentoapi.mail;

import com.example.lancamentoapi.repository.LaunchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class Mailer {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
//    private final LaunchRepository repository;

    //Usar para simular emails na aplicação
    /*@EventListener
    private void test(ApplicationReadyEvent event) {
        this.sendEmail("brunocesarjavadevtest@gmail.com",
                Collections.singletonList("brunocesar.oc96@gmail.com"),
                "Testando", "Olá!<br/>Teste ok.");
        System.out.println("TERMINADO O ENVIO DE EMAIL...");
    }*/

//    @EventListener
//    private void test(ApplicationReadyEvent event) {//Evento disparado quando aplicação sobe
//        String template = "mail/alert-overdue-launchs";
//
//        List<Launch> launchList = repository.findAll();
//        Map<String, Object> variables = new HashMap<>();
//        variables.put("launchs", launchList);
//
//        this.sendEmail("brunocesar.dev.test.java@gmail.com",
//                Collections.singletonList("brunocesar.oc96@gmail.com"),
//                "Testando", template, variables);
//        System.out.println("TERMINADO O ENVIO DE EMAIL...");
//    }

    public void sendEmail(String from, List<String> targets, String subject, String template, Map<String, Object> variables) {
        Context context= new Context(new Locale("pt", "BR"));

        variables.forEach(context::setVariable);

        String message = templateEngine.process(template, context);
        sendEmail(from, targets, subject, message);
    }

    public void sendEmail(String from, List<String> targets, String subject, String message) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");

        try {
            helper.setFrom(from);
            helper.setTo(targets.toArray(new String[0]));
            helper.setSubject(subject);
            helper.setText(message, true);

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Erro ao enviar email", e);
        }
    }
}
