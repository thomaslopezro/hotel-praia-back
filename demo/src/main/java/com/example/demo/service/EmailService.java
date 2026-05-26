package com.example.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.mail.internet.MimeMessage;

import java.util.List;
import java.util.Map;

/**
 * Envia correos. Soporta tres backends:
 *   1. SendGrid HTTPS API (si SENDGRID_API_KEY esta seteada). Para produccion en
 *      Render porque su Free tier bloquea SMTP saliente (puerto 587/465).
 *   2. SMTP via JavaMailSender (si MAIL_USERNAME esta seteado). Para dev local.
 *   3. Sin configurar: solo loguea el link de fallback.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final String SENDGRID_URL = "https://api.sendgrid.com/v3/mail/send";

    private final JavaMailSender mailSender;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.front.url:http://localhost:4200}")
    private String frontUrl;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Value("${sendgrid.api.key:}")
    private String sendgridApiKey;

    @Value("${sendgrid.sender.email:}")
    private String sendgridSenderEmail;

    @Value("${sendgrid.sender.name:Hotel Praia}")
    private String sendgridSenderName;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarCorreoVerificacion(String destinatario, String nombre, String token) {
        String link = frontUrl + "/verificar?token=" + token;
        enviar(destinatario, nombre, "Hotel Praia - Verifica tu cuenta",
               buildHtml(nombre, link), link, "verificacion");
    }

    public void enviarCorreoRecuperacion(String destinatario, String nombre, String token) {
        String link = frontUrl + "/restablecer-password?token=" + token;
        enviar(destinatario, nombre, "Hotel Praia - Recupera tu contrasena",
               buildHtmlRecuperacion(nombre, link), link, "recuperacion");
    }

    private void enviar(String destinatario, String nombre, String subject,
                        String htmlContent, String linkFallback, String tipo) {
        // 1. SendGrid HTTPS API (preferido en prod)
        if (sendgridApiKey != null && !sendgridApiKey.isBlank()
                && sendgridSenderEmail != null && !sendgridSenderEmail.isBlank()) {
            try {
                enviarPorSendgrid(destinatario, nombre, subject, htmlContent);
                log.info("Correo de {} enviado a {} via SendGrid", tipo, destinatario);
                return;
            } catch (Exception e) {
                log.error("Error enviando correo de {} a {} via SendGrid: {}",
                          tipo, destinatario, e.getMessage());
                log.warn("Link de {} (fallback): {}", tipo, linkFallback);
                return;
            }
        }

        // 2. SMTP (dev local con Gmail)
        if (fromEmail != null && !fromEmail.isBlank()) {
            try {
                enviarPorSmtp(destinatario, subject, htmlContent);
                log.info("Correo de {} enviado a {} via SMTP", tipo, destinatario);
                return;
            } catch (Exception e) {
                log.error("Error enviando correo de {} a {} via SMTP: {}",
                          tipo, destinatario, e.getMessage());
                log.warn("Link de {} (fallback): {}", tipo, linkFallback);
                return;
            }
        }

        // 3. Sin configurar - solo log
        log.warn("=================================================================");
        log.warn("EMAIL NO CONFIGURADO. Link de {} (copia y pega):", tipo);
        log.warn(linkFallback);
        log.warn("=================================================================");
    }

    private void enviarPorSendgrid(String destinatario, String nombre, String subject, String html) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(sendgridApiKey);

        Map<String, Object> body = Map.of(
            "personalizations", List.of(Map.of(
                "to", List.of(Map.of("email", destinatario, "name", nombre == null ? "" : nombre))
            )),
            "from", Map.of("email", sendgridSenderEmail, "name", sendgridSenderName),
            "subject", subject,
            "content", List.of(Map.of("type", "text/html", "value", html))
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        restTemplate.postForEntity(SENDGRID_URL, request, String.class);
    }

    private void enviarPorSmtp(String destinatario, String subject, String html) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(destinatario);
        helper.setFrom(fromEmail);
        helper.setSubject(subject);
        helper.setText(html, true);
        mailSender.send(message);
    }

    private String buildHtml(String nombre, String link) {
        return ""
            + "<div style=\"font-family:Arial,sans-serif;max-width:600px;margin:0 auto;background:#f5efe3;padding:20px;\">"
            + "  <div style=\"background:#08121a;padding:32px 28px;border-radius:12px;color:#f5efe3;\">"
            + "    <h1 style=\"color:#d6b36a;margin:0 0 16px;font-family:Georgia,serif;\">Hotel Praia</h1>"
            + "    <h2 style=\"color:#fff;margin:0 0 20px;\">Hola " + (nombre == null ? "" : nombre) + ",</h2>"
            + "    <p style=\"color:#cbd5e1;line-height:1.7;\">Gracias por registrarte en Hotel Praia. "
            + "    Para activar tu cuenta y poder reservar, haz click en el siguiente boton:</p>"
            + "    <p style=\"text-align:center;margin:32px 0;\">"
            + "      <a href=\"" + link + "\" "
            + "         style=\"background:linear-gradient(135deg,#d6b36a,#b99243);color:#08121a;"
            + "                padding:14px 32px;border-radius:999px;text-decoration:none;"
            + "                font-weight:600;display:inline-block;\">Verificar mi cuenta</a>"
            + "    </p>"
            + "    <p style=\"color:#94a3b8;font-size:13px;line-height:1.6;\">Si el boton no funciona, copia y pega este link en tu navegador:<br>"
            + "       <a href=\"" + link + "\" style=\"color:#d6b36a;word-break:break-all;\">" + link + "</a></p>"
            + "    <hr style=\"border:none;border-top:1px solid #1e293b;margin:28px 0;\">"
            + "    <p style=\"color:#64748b;font-size:12px;\">Si tu no creaste esta cuenta, puedes ignorar este correo.</p>"
            + "  </div>"
            + "</div>";
    }

    private String buildHtmlRecuperacion(String nombre, String link) {
        return ""
            + "<div style=\"font-family:Arial,sans-serif;max-width:600px;margin:0 auto;background:#f5efe3;padding:20px;\">"
            + "  <div style=\"background:#08121a;padding:32px 28px;border-radius:12px;color:#f5efe3;\">"
            + "    <h1 style=\"color:#d6b36a;margin:0 0 16px;font-family:Georgia,serif;\">Hotel Praia</h1>"
            + "    <h2 style=\"color:#fff;margin:0 0 20px;\">Hola " + (nombre == null ? "" : nombre) + ",</h2>"
            + "    <p style=\"color:#cbd5e1;line-height:1.7;\">Recibimos una solicitud para restablecer la contrasena "
            + "    de tu cuenta. Haz click en el siguiente boton para elegir una nueva:</p>"
            + "    <p style=\"text-align:center;margin:32px 0;\">"
            + "      <a href=\"" + link + "\" "
            + "         style=\"background:linear-gradient(135deg,#d6b36a,#b99243);color:#08121a;"
            + "                padding:14px 32px;border-radius:999px;text-decoration:none;"
            + "                font-weight:600;display:inline-block;\">Restablecer contrasena</a>"
            + "    </p>"
            + "    <p style=\"color:#94a3b8;font-size:13px;line-height:1.6;\">Si el boton no funciona, copia y pega este link en tu navegador:<br>"
            + "       <a href=\"" + link + "\" style=\"color:#d6b36a;word-break:break-all;\">" + link + "</a></p>"
            + "    <p style=\"color:#fbbf24;font-size:13px;margin-top:18px;\">Este enlace expira en 1 hora.</p>"
            + "    <hr style=\"border:none;border-top:1px solid #1e293b;margin:28px 0;\">"
            + "    <p style=\"color:#64748b;font-size:12px;\">Si tu no solicitaste este cambio, puedes ignorar este correo. "
            + "    Tu contrasena actual seguira funcionando.</p>"
            + "  </div>"
            + "</div>";
    }
}
