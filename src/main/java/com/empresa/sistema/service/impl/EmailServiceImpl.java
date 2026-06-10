package com.empresa.sistema.service.impl;

import com.empresa.sistema.entity.ConfiguracionEmpresa;
import com.empresa.sistema.entity.DetalleVenta;
import com.empresa.sistema.entity.Factura;
import com.empresa.sistema.repository.ConfiguracionEmpresaRepository;
import com.empresa.sistema.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import com.empresa.sistema.entity.SolicitudStock;
import com.empresa.sistema.entity.Usuario;

/**
 * Implementación real activa cuando app.mail.enabled=true.
 * Si RESEND_API_KEY está configurado, envía vía API HTTP de Resend (sin SMTP).
 * En otro caso usa JavaMailSender (SMTP).
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "true")
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender                 mailSender;
    private final ConfiguracionEmpresaRepository empresaRepository;

    @Value("${app.base-url:}")
    private String baseUrl;

    @Value("${spring.mail.username}")
    private String smtpUsername;

    /** Si se configura, usa Resend API en lugar de SMTP */
    @Value("${RESEND_API_KEY:}")
    private String resendApiKey;

    /** Dirección "from" cuando se usa Resend (debe ser un dominio verificado en resend.com) */
    @Value("${RESEND_FROM:onboarding@resend.dev}")
    private String resendFrom;

    private static final DateTimeFormatter FMT_FECHA =
            DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", new Locale("es", "EC"));

    // ── Resend HTTP helper ──────────────────────────────────────────────────
    private boolean sendViaResend(String to, String subject, String html, byte[] pdfBytes, String pdfName) {
        try {
            String attachmentJson = "";
            if (pdfBytes != null && pdfBytes.length > 0) {
                String b64 = Base64.getEncoder().encodeToString(pdfBytes);
                attachmentJson = """
                        ,"attachments":[{"filename":"%s","content":"%s"}]
                        """.formatted(pdfName, b64);
            }
            // Escapar comillas en HTML para JSON
            String htmlEscaped = html.replace("\\", "\\\\").replace("\"", "\\\"")
                                     .replace("\n", "\\n").replace("\r", "");
            String json = """
                    {"from":"%s","to":["%s"],"subject":"%s","html":"%s"%s}
                    """.formatted(resendFrom, to, subject, htmlEscaped, attachmentJson).strip();

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10)).build();
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.resend.com/emails"))
                    .header("Authorization", "Bearer " + resendApiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .timeout(Duration.ofSeconds(15))
                    .build();
            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                log.info("[Resend] Correo enviado a {} — status {}", to, resp.statusCode());
                return true;
            } else {
                log.error("[Resend] Error {} enviando a {}: {}", resp.statusCode(), to, resp.body());
                return false;
            }
        } catch (Exception e) {
            log.error("[Resend] Excepción enviando a {}: {}", to, e.getMessage());
            return false;
        }
    }

    @Override
    public void enviarFactura(Factura factura, List<DetalleVenta> detalles, byte[] pdfBytes) {
        String correoCliente = factura.getCliente().getCorreo();
        if (correoCliente == null || correoCliente.isBlank()) {
            log.warn("[EmailService] Cliente {} no tiene correo – factura {}",
                    factura.getCliente().getIdentificacion(), factura.getNumeroSecuencial());
            return;
        }

        ConfiguracionEmpresa empresa = empresaRepository.findFirstBy()
                .orElse(defaultEmpresa());

        String fromName = empresa.getNombreComercial() != null
                ? empresa.getNombreComercial() : empresa.getRazonSocial();
        String subject  = "Factura " + factura.getNumeroSecuencial() + " de " + fromName;
        String html     = buildHtml(factura, empresa);
        String pdfName  = "Factura_" + factura.getNumeroSecuencial() + ".pdf";

        if (resendApiKey != null && !resendApiKey.isBlank()) {
            sendViaResend(correoCliente, subject, html, pdfBytes, pdfName);
        } else {
            try {
                MimeMessage msg = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
                helper.setFrom(smtpUsername, fromName);
                helper.setTo(correoCliente);
                helper.setSubject(subject);
                helper.setText(html, true);
                if (pdfBytes != null && pdfBytes.length > 0) {
                    helper.addAttachment(pdfName, new ByteArrayResource(pdfBytes), "application/pdf");
                }
                mailSender.send(msg);
                log.info("[EmailService] Factura {} enviada a {}", factura.getNumeroSecuencial(), correoCliente);
            } catch (Exception e) {
                log.error("[EmailService] Error enviando factura {}: {}", factura.getNumeroSecuencial(), e.getMessage(), e);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────

    private String buildHtml(Factura factura, ConfiguracionEmpresa empresa) {

        String nombreEmpresa = empresa.getNombreComercial() != null
                ? empresa.getNombreComercial() : empresa.getRazonSocial();

        String nombreCliente = factura.getSnapCliNombres() != null
                ? factura.getSnapCliNombres() + (factura.getSnapCliApellidos() != null ? " " + factura.getSnapCliApellidos() : "")
                : factura.getCliente().getNombres() + (factura.getCliente().getApellidos() != null ? " " + factura.getCliente().getApellidos() : "");

        String fecha = (factura.getFechaEmision() != null
                ? factura.getFechaEmision()
                : factura.getFechaFactura()).format(FMT_FECHA);

        String total = "$" + String.format("%.2f", factura.getTotal());

        // Botón "Ver Documento" solo si hay base URL configurada
        String botonVer = "";
        if (baseUrl != null && !baseUrl.isBlank()) {
            String urlDoc = baseUrl.stripTrailing() + "/api/facturas/" + factura.getIdFactura() + "/pdf";
            botonVer = """
                    <tr><td align="center" style="padding:8px 40px 28px;">
                      <p style="margin:0 0 16px;font-size:13px;color:#555;font-family:Arial,sans-serif;">
                        Consulta el comprobante detallado en línea:
                      </p>
                      <a href="%s"
                         style="display:inline-block;background:#1468B1;color:#ffffff;
                                font-size:14px;font-weight:700;text-decoration:none;
                                padding:14px 40px;border-radius:30px;letter-spacing:0.5px;
                                font-family:Arial,sans-serif;">
                        VER DOCUMENTO
                      </a>
                    </td></tr>
                    """.formatted(urlDoc);
        }

        String direccionEmpresa = empresa.getDireccionMatriz() != null ? esc(empresa.getDireccionMatriz()) : "";
        String ruc = empresa.getRuc() != null ? esc(empresa.getRuc()) : "";

        return """
                <!DOCTYPE html>
                <html lang="es">
                <head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Factura</title></head>
                <body style="margin:0;padding:0;background:#f4f4f4;font-family:Arial,sans-serif;">
                <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f4f4f4;">
                <tr><td align="center" style="padding:32px 16px;">

                  <!-- Contenedor principal -->
                  <table width="540" cellpadding="0" cellspacing="0"
                         style="background:#ffffff;border-radius:4px;overflow:hidden;
                                box-shadow:0 2px 8px rgba(0,0,0,.08);max-width:540px;">

                    <!-- Franja superior azul -->
                    <tr><td style="background:#1468B1;height:5px;font-size:1px;">&nbsp;</td></tr>

                    <!-- Logo / nombre empresa -->
                    <tr><td align="right" style="padding:18px 28px 0;">
                      <span style="font-size:20px;font-weight:800;color:#1468B1;letter-spacing:-0.5px;font-family:Arial,sans-serif;">
                        %s
                      </span>
                    </td></tr>

                    <!-- Separador -->
                    <tr><td style="padding:0 28px;">
                      <hr style="border:none;border-top:1px solid #e5e7eb;margin:14px 0 20px;">
                    </td></tr>

                    <!-- Nombre cliente + mensaje -->
                    <tr><td align="center" style="padding:0 28px 8px;">
                      <p style="margin:0;font-size:16px;font-weight:700;color:#12274B;font-family:Arial,sans-serif;">
                        %s
                      </p>
                      <p style="margin:6px 0 0;font-size:14px;color:#555;font-family:Arial,sans-serif;">
                        Has recibido una Factura de <strong>%s</strong>
                      </p>
                    </td></tr>

                    <!-- Separador -->
                    <tr><td style="padding:0 28px;">
                      <hr style="border:none;border-top:1px solid #e5e7eb;margin:16px 0;">
                    </td></tr>

                    <!-- Número de factura -->
                    <tr><td align="center" style="padding:0 28px 4px;">
                      <p style="margin:0;font-size:15px;font-weight:700;color:#12274B;font-family:Arial,sans-serif;">
                        %s
                      </p>
                    </td></tr>

                    <!-- Fecha -->
                    <tr><td align="center" style="padding:8px 28px;">
                      <div style="display:inline-block;background:#f4f4f4;border-radius:3px;
                                  padding:8px 24px;font-size:13px;color:#555;font-family:Arial,sans-serif;">
                        %s
                      </div>
                    </td></tr>

                    <!-- Valor -->
                    <tr><td align="center" style="padding:20px 28px 8px;">
                      <p style="margin:0 0 4px;font-size:13px;color:#888;font-family:Arial,sans-serif;">Por el valor de:</p>
                      <p style="margin:0;font-size:36px;font-weight:700;color:#12274B;font-family:Arial,sans-serif;">
                        %s
                      </p>
                    </td></tr>

                    <!-- Separador -->
                    <tr><td style="padding:0 28px;">
                      <hr style="border:none;border-top:1px solid #e5e7eb;margin:20px 0 8px;">
                    </td></tr>

                    <!-- Botón Ver Documento (opcional) -->
                    %s

                    <!-- Footer -->
                    <tr><td style="background:#f8f8f8;padding:16px 28px;border-top:1px solid #e5e7eb;">
                      <p style="margin:0;font-size:12px;font-weight:700;color:#12274B;text-align:center;font-family:Arial,sans-serif;">
                        %s
                      </p>
                      <p style="margin:4px 0 0;font-size:11px;color:#888;text-align:center;font-family:Arial,sans-serif;">
                        RUC %s
                      </p>
                      <p style="margin:2px 0 0;font-size:11px;color:#888;text-align:center;font-family:Arial,sans-serif;">
                        %s
                      </p>
                    </td></tr>

                  </table>

                </td></tr></table>
                </body></html>
                """.formatted(
                esc(nombreEmpresa),           // logo empresa
                esc(nombreCliente),           // nombre cliente
                esc(nombreEmpresa),           // "Has recibido una Factura de X"
                esc(factura.getNumeroSecuencial()),
                fecha,
                total,
                botonVer,
                esc(nombreEmpresa),
                ruc,
                direccionEmpresa
        );
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private ConfiguracionEmpresa defaultEmpresa() {
        return ConfiguracionEmpresa.builder()
                .razonSocial("EMPRESA").ruc("9999999999001")
                .correo("noreply@empresa.com").build();
    }

    // ── Solicitud de stock inter-s