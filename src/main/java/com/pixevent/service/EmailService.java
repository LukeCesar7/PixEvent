package com.pixevent.service;

import com.pixevent.entity.Pedido;
import com.pixevent.util.QrCodeUtil;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Attachments;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Porta de src/services/EmailService.js (SendGrid).
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Value("${app.sendgrid.api-key:}")
    private String apiKey;

    @Value("${app.sendgrid.email-from}")
    private String emailFrom;

    @Value("${app.sendgrid.email-from-name}")
    private String emailFromName;

    public void enviarConfirmacao(Pedido pedido, List<Integer> numerosRifa) throws Exception {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("SENDGRID_API_KEY não configurada — e-mail de confirmação não enviado para pedido {}", pedido.getId());
            return;
        }

        String qrBase64 = QrCodeUtil.toBase64(pedido.getQrcodeToken(), 300);

        String rifaInfo = (numerosRifa != null && !numerosRifa.isEmpty())
                ? "<p>Seus números da rifa: <strong>" + join(numerosRifa) + "</strong></p>"
                : "";

        String html = """
                <h2>Pagamento confirmado! 🎉</h2>
                <p>Olá, <strong>%s</strong>!</p>
                <p>Seu pedido foi confirmado. Apresente o QR Code abaixo na portaria do evento.</p>
                %s
                <img src="cid:qrcode" alt="QR Code de entrada" width="250"/>
                <p style="font-size:12px;color:#888">Vinculado ao CPF %s</p>
                """.formatted(pedido.getNome(), rifaInfo, pedido.getCpf());

        Mail mail = new Mail();
        mail.setFrom(new Email(emailFrom, emailFromName));
        mail.setSubject("Forró dos Namorados 2026 — Seu ingresso chegou!");

        com.sendgrid.helpers.mail.objects.Personalization personalization =
                new com.sendgrid.helpers.mail.objects.Personalization();
        personalization.addTo(new Email(pedido.getEmail()));
        mail.addPersonalization(personalization);

        mail.addContent(new Content("text/html", html));

        Attachments attachment = new Attachments();
        attachment.setContent(qrBase64);
        attachment.setFilename("ingresso-qrcode.png");
        attachment.setType("image/png");
        attachment.setDisposition("inline");
        attachment.setContentId("qrcode");
        mail.addAttachments(attachment);

        SendGrid sg = new SendGrid(apiKey);
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());
        Response response = sg.api(request);

        if (response.getStatusCode() >= 300) {
            log.error("Falha ao enviar e-mail via SendGrid: {} - {}", response.getStatusCode(), response.getBody());
        }
    }

    private String join(List<Integer> numeros) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numeros.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(numeros.get(i));
        }
        return sb.toString();
    }
}
