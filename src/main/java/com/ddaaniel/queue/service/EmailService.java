package com.ddaaniel.queue.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void enviarEmail(String destinatario, String codigoCodigo) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(destinatario);
            helper.setSubject("Seu Código de Paciente");
            helper.setText("Olá, seu código de paciente é: " + codigoCodigo);

            mailSender.send(message);
            System.out.println("E-mail enviado com sucesso!");
        } catch (MessagingException e) {
            throw new IllegalStateException("Falha ao enviar e-mail", e);
        }
    }
}
