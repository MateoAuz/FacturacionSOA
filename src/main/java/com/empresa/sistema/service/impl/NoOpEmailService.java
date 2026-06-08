package com.empresa.sistema.service.impl;

import com.empresa.sistema.entity.DetalleVenta;
import com.empresa.sistema.entity.Factura;
import com.empresa.sistema.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementación vacía activa cuando app.mail.enabled=false (valor por defecto).
 * No envía ningún correo; sólo registra un mensaje de depuración.
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "false", matchIfMissing = true)
public class NoOpEmailService implements EmailService {

    @Override
    public void enviarFactura(Factura factura, List<DetalleVenta> detalles, byte[] pdfBytes) {
        log.debug("[NoOpEmailService] Envío de correo deshabilitado. Factura {} – cliente: {}",
                factura.getNumeroSecuencial(),
                factura.getCliente().getCorreo());
    }
}
