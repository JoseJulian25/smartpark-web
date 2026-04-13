package com.parking.service;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.parking.entity.Empresa;
import com.parking.entity.Reserva;
import com.parking.exception.EmailDeliveryException;
import com.parking.repository.EmpresaRepository;

import jakarta.mail.internet.MimeMessage;

@Service
public class ReservaEmailService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a", Locale.forLanguageTag("es-DO"));

    private final JavaMailSender mailSender;
    private final EmpresaRepository empresaRepository;

    @Value("${spring.mail.username:}")
    private String mailFromAddress;

    @Value("${mail.from.name:Parking System}")
    private String mailFromName;

    public ReservaEmailService(JavaMailSender mailSender, EmpresaRepository empresaRepository) {
        this.mailSender = mailSender;
        this.empresaRepository = empresaRepository;
    }

    public void enviarConfirmacionReserva(Reserva reserva) {
        Empresa empresa = empresaRepository.findFirstByOrderByIdAsc().orElse(null);
        String nombreEmpresa = empresa == null ? "Parking System" : safe(empresa.getNombre());
      String telefonoParqueo = empresa == null ? "No disponible" : safe(empresa.getTelefono());
      String emailParqueo = empresa == null ? safe(mailFromAddress) : safe(empresa.getEmail());
      String direccionParqueo = empresa == null ? "" : safe(empresa.getDireccion());
        String fromAddress = safe(mailFromAddress);

        if (fromAddress.isBlank()) {
            throw new EmailDeliveryException(
                    "No se pudo enviar el correo de confirmacion: configure MAIL_USERNAME en el backend.",
                    null);
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());

            helper.setFrom(fromAddress, mailFromName);
            helper.setTo(safe(reserva.getClienteEmail()));
            helper.setSubject("Confirmacion de reserva " + safe(reserva.getCodigoReserva()) + " | " + nombreEmpresa);
            helper.setText(buildHtmlBody(reserva, nombreEmpresa, telefonoParqueo, emailParqueo, direccionParqueo), true);

            mailSender.send(message);
        } catch (MailException | jakarta.mail.MessagingException | java.io.UnsupportedEncodingException ex) {
            throw new EmailDeliveryException(
                    "No se pudo enviar el correo al cliente. La reserva no fue creada. Verifique el correo del cliente y la configuracion SMTP.",
                    ex);
        }
    }

      public void enviarCancelacionReserva(Reserva reserva) {
        Empresa empresa = empresaRepository.findFirstByOrderByIdAsc().orElse(null);
        String nombreEmpresa = empresa == null ? "Parking System" : safe(empresa.getNombre());
        String telefonoParqueo = empresa == null ? "No disponible" : safe(empresa.getTelefono());
        String emailParqueo = empresa == null ? safe(mailFromAddress) : safe(empresa.getEmail());
        String direccionParqueo = empresa == null ? "" : safe(empresa.getDireccion());
        String fromAddress = safe(mailFromAddress);

        if (fromAddress.isBlank()) {
          throw new EmailDeliveryException(
              "No se pudo enviar el correo de cancelacion: configure MAIL_USERNAME en el backend.",
              null);
        }

        try {
          MimeMessage message = mailSender.createMimeMessage();
          MimeMessageHelper helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());

          helper.setFrom(fromAddress, mailFromName);
          helper.setTo(safe(reserva.getClienteEmail()));
          helper.setSubject("Cancelacion de reserva " + safe(reserva.getCodigoReserva()) + " | " + nombreEmpresa);
          helper.setText(buildCancelacionHtmlBody(reserva, nombreEmpresa, telefonoParqueo, emailParqueo, direccionParqueo), true);

          mailSender.send(message);
        } catch (MailException | jakarta.mail.MessagingException | java.io.UnsupportedEncodingException ex) {
          throw new EmailDeliveryException(
              "No se pudo enviar el correo de cancelacion.",
              ex);
        }
      }

        private String buildHtmlBody(Reserva reserva, String nombreEmpresa, String telefonoParqueo, String emailParqueo,
          String direccionParqueo) {
        String horaInicio = reserva.getHoraInicio() == null ? "-" : reserva.getHoraInicio().format(DATE_TIME_FORMATTER);
        String nombreCliente = safe(reserva.getClienteNombreCompleto());
        String codigoReserva = safe(reserva.getCodigoReserva());
        String placa = safe(reserva.getPlaca());
        String tipoVehiculo = safe(reserva.getTipoVehiculo() == null ? "-" : reserva.getTipoVehiculo().getNombre());
        String espacio = safe(reserva.getEspacio() == null ? "-" : reserva.getEspacio().getCodigoEspacio());
        String telefono = safe(reserva.getClienteTelefono());

        String htmlTemplate = """
            <div style=\"background:#f6f8fb;padding:24px;font-family:'Segoe UI',Tahoma,Arial,sans-serif;color:#1f2937;\">
              <table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"max-width:720px;margin:0 auto;background:#ffffff;border:1px solid #e5e7eb;border-radius:12px;overflow:hidden;\">
                <tr>
                  <td style=\"background:#0f172a;padding:20px 24px;color:#ffffff;\">
                    <div style=\"font-size:12px;letter-spacing:.08em;text-transform:uppercase;opacity:.8;\">Confirmacion oficial</div>
                    <div style=\"font-size:24px;font-weight:700;margin-top:6px;\">Reserva de parqueo creada</div>
                    <div style=\"font-size:14px;opacity:.9;margin-top:4px;\">{{EMPRESA}}</div>
                  </td>
                </tr>
                <tr>
                  <td style=\"padding:24px;\">
                    <p style=\"margin:0 0 14px 0;font-size:15px;line-height:1.5;\">Estimado/a <strong>{{CLIENTE}}</strong>, su reserva fue registrada exitosamente.</p>

                    <table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"border-collapse:collapse;border:1px solid #e5e7eb;border-radius:8px;overflow:hidden;\">
                      <tr><td style=\"padding:12px 14px;background:#f9fafb;font-weight:600;width:42%;border-bottom:1px solid #e5e7eb;\">Codigo de reserva</td><td style=\"padding:12px 14px;border-bottom:1px solid #e5e7eb;font-family:Consolas,monospace;font-weight:700;color:#0b3a9e;\">{{CODIGO}}</td></tr>
                      <tr><td style=\"padding:12px 14px;background:#f9fafb;font-weight:600;width:42%;border-bottom:1px solid #e5e7eb;\">Fecha y hora de inicio</td><td style=\"padding:12px 14px;border-bottom:1px solid #e5e7eb;\">{{HORA_INICIO}}</td></tr>
                      <tr><td style=\"padding:12px 14px;background:#f9fafb;font-weight:600;width:42%;border-bottom:1px solid #e5e7eb;\">Espacio asignado</td><td style=\"padding:12px 14px;border-bottom:1px solid #e5e7eb;\">{{ESPACIO}}</td></tr>
                      <tr><td style=\"padding:12px 14px;background:#f9fafb;font-weight:600;width:42%;border-bottom:1px solid #e5e7eb;\">Vehiculo</td><td style=\"padding:12px 14px;border-bottom:1px solid #e5e7eb;\">{{TIPO_VEHICULO}} - {{PLACA}}</td></tr>
                      <tr><td style=\"padding:12px 14px;background:#f9fafb;font-weight:600;width:42%;\">Telefono de contacto</td><td style=\"padding:12px 14px;\">{{TELEFONO}}</td></tr>
                    </table>

                    <div style=\"margin-top:16px;padding:12px 14px;background:#ecfeff;border:1px solid #a5f3fc;border-radius:8px;font-size:13px;line-height:1.5;\">
                      Presente este codigo al llegar para confirmar su entrada mas rapidamente.
                    </div>

                    <div style=\"margin-top:14px;padding:12px 14px;background:#f8fafc;border:1px solid #e2e8f0;border-radius:8px;font-size:13px;line-height:1.6;\">
                      <div style=\"font-weight:700;color:#0f172a;margin-bottom:6px;\">Contacto del parqueo</div>
                      <div><strong>Telefono:</strong> {{TELEFONO_PARQUEO}}</div>
                      <div><strong>Email:</strong> {{EMAIL_PARQUEO}}</div>
                      <div><strong>Direccion:</strong> {{DIRECCION_PARQUEO}}</div>
                    </div>
                  </td>
                </tr>
                <tr>
                  <td style=\"padding:16px 24px;background:#f9fafb;border-top:1px solid #e5e7eb;color:#6b7280;font-size:12px;line-height:1.5;\">
                    Este correo fue generado automaticamente por {{EMPRESA}}. Si no reconoce esta operacion, contacte al parqueo.
                  </td>
                </tr>
              </table>
            </div>
            """;

        return htmlTemplate
                .replace("{{EMPRESA}}", nombreEmpresa)
                .replace("{{CLIENTE}}", nombreCliente)
                .replace("{{CODIGO}}", codigoReserva)
                .replace("{{HORA_INICIO}}", horaInicio)
                .replace("{{ESPACIO}}", espacio)
                .replace("{{TIPO_VEHICULO}}", tipoVehiculo)
                .replace("{{PLACA}}", placa)
                .replace("{{TELEFONO}}", telefono)
                .replace("{{TELEFONO_PARQUEO}}", safe(telefonoParqueo).isBlank() ? "No disponible" : safe(telefonoParqueo))
                .replace("{{EMAIL_PARQUEO}}", safe(emailParqueo).isBlank() ? "No disponible" : safe(emailParqueo))
                .replace("{{DIRECCION_PARQUEO}}", safe(direccionParqueo).isBlank() ? "No disponible" : safe(direccionParqueo));
    }

    private String buildCancelacionHtmlBody(Reserva reserva, String nombreEmpresa, String telefonoParqueo,
            String emailParqueo, String direccionParqueo) {
        String horaInicio = reserva.getHoraInicio() == null ? "-" : reserva.getHoraInicio().format(DATE_TIME_FORMATTER);
        String nombreCliente = safe(reserva.getClienteNombreCompleto());
        String codigoReserva = safe(reserva.getCodigoReserva());
        String placa = safe(reserva.getPlaca());
        String tipoVehiculo = safe(reserva.getTipoVehiculo() == null ? "-" : reserva.getTipoVehiculo().getNombre());
        String espacio = safe(reserva.getEspacio() == null ? "-" : reserva.getEspacio().getCodigoEspacio());
        String motivoCancelacion = safe(reserva.getMotivoCancelacion()).isBlank() ? "No especificado" : safe(reserva.getMotivoCancelacion());

        String htmlTemplate = """
            <div style=\"background:#f6f8fb;padding:24px;font-family:'Segoe UI',Tahoma,Arial,sans-serif;color:#1f2937;\">
              <table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"max-width:720px;margin:0 auto;background:#ffffff;border:1px solid #e5e7eb;border-radius:12px;overflow:hidden;\">
                <tr>
                  <td style=\"background:#7f1d1d;padding:20px 24px;color:#ffffff;\">
                    <div style=\"font-size:12px;letter-spacing:.08em;text-transform:uppercase;opacity:.85;\">Notificacion</div>
                    <div style=\"font-size:24px;font-weight:700;margin-top:6px;\">Reserva cancelada</div>
                    <div style=\"font-size:14px;opacity:.9;margin-top:4px;\">{{EMPRESA}}</div>
                  </td>
                </tr>
                <tr>
                  <td style=\"padding:24px;\">
                    <p style=\"margin:0 0 14px 0;font-size:15px;line-height:1.5;\">Estimado/a <strong>{{CLIENTE}}</strong>, su reserva ha sido cancelada.</p>

                    <table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"border-collapse:collapse;border:1px solid #e5e7eb;border-radius:8px;overflow:hidden;\">
                      <tr><td style=\"padding:12px 14px;background:#f9fafb;font-weight:600;width:42%;border-bottom:1px solid #e5e7eb;\">Codigo de reserva</td><td style=\"padding:12px 14px;border-bottom:1px solid #e5e7eb;font-family:Consolas,monospace;font-weight:700;color:#7f1d1d;\">{{CODIGO}}</td></tr>
                      <tr><td style=\"padding:12px 14px;background:#f9fafb;font-weight:600;width:42%;border-bottom:1px solid #e5e7eb;\">Fecha y hora original</td><td style=\"padding:12px 14px;border-bottom:1px solid #e5e7eb;\">{{HORA_INICIO}}</td></tr>
                      <tr><td style=\"padding:12px 14px;background:#f9fafb;font-weight:600;width:42%;border-bottom:1px solid #e5e7eb;\">Espacio asignado</td><td style=\"padding:12px 14px;border-bottom:1px solid #e5e7eb;\">{{ESPACIO}}</td></tr>
                      <tr><td style=\"padding:12px 14px;background:#f9fafb;font-weight:600;width:42%;border-bottom:1px solid #e5e7eb;\">Vehiculo</td><td style=\"padding:12px 14px;border-bottom:1px solid #e5e7eb;\">{{TIPO_VEHICULO}} - {{PLACA}}</td></tr>
                      <tr><td style=\"padding:12px 14px;background:#f9fafb;font-weight:600;width:42%;\">Motivo de cancelacion</td><td style=\"padding:12px 14px;\">{{MOTIVO}}</td></tr>
                    </table>

                    <div style=\"margin-top:14px;padding:12px 14px;background:#f8fafc;border:1px solid #e2e8f0;border-radius:8px;font-size:13px;line-height:1.6;\">
                      <div style=\"font-weight:700;color:#0f172a;margin-bottom:6px;\">Contacto del parqueo</div>
                      <div><strong>Telefono:</strong> {{TELEFONO_PARQUEO}}</div>
                      <div><strong>Email:</strong> {{EMAIL_PARQUEO}}</div>
                      <div><strong>Direccion:</strong> {{DIRECCION_PARQUEO}}</div>
                    </div>
                  </td>
                </tr>
              </table>
            </div>
            """;

        return htmlTemplate
                .replace("{{EMPRESA}}", nombreEmpresa)
                .replace("{{CLIENTE}}", nombreCliente)
                .replace("{{CODIGO}}", codigoReserva)
                .replace("{{HORA_INICIO}}", horaInicio)
                .replace("{{ESPACIO}}", espacio)
                .replace("{{TIPO_VEHICULO}}", tipoVehiculo)
                .replace("{{PLACA}}", placa)
                .replace("{{MOTIVO}}", motivoCancelacion)
                .replace("{{TELEFONO_PARQUEO}}", safe(telefonoParqueo).isBlank() ? "No disponible" : safe(telefonoParqueo))
                .replace("{{EMAIL_PARQUEO}}", safe(emailParqueo).isBlank() ? "No disponible" : safe(emailParqueo))
                .replace("{{DIRECCION_PARQUEO}}", safe(direccionParqueo).isBlank() ? "No disponible" : safe(direccionParqueo));
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
