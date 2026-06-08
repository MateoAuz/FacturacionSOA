package com.empresa.sistema.service;

import com.empresa.sistema.entity.DetalleVenta;
import com.empresa.sistema.entity.Factura;
import com.empresa.sistema.entity.SolicitudStock;
import com.empresa.sistema.entity.Usuario;

import java.util.List;

public interface EmailService {
    /**
     * Envía la factura emitida al correo del cliente como HTML + PDF adjunto.
     *
     * @param factura  entidad de la factura ya emitida
     * @param detalles líneas de detalle de la factura
     * @param pdfBytes bytes del PDF generado
     */
    void enviarFactura(Factura factura, List<DetalleVenta> detalles, byte[] pdfBytes);
    void enviarSolicitudStock(SolicitudStock solicitud, List<Usuario> bodegueros);
}
