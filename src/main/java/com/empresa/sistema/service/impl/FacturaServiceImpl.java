package com.empresa.sistema.service.impl;

import com.empresa.sistema.dto.response.FacturaResponseDTO;
import com.empresa.sistema.entity.Factura;
import com.empresa.sistema.entity.Venta;
import com.empresa.sistema.repository.DetalleVentaRepository;
import com.empresa.sistema.repository.FacturaRepository;
import com.empresa.sistema.repository.VentaRepository;
import com.empresa.sistema.service.FacturaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FacturaServiceImpl implements FacturaService {

    private final FacturaRepository facturaRepository;
    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleVentaRepository;

    @Override
    public FacturaResponseDTO generarFactura(Integer idVenta) {
        Venta venta = ventaRepository.findById(idVenta)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada: " + idVenta));
        if (facturaRepository.findByVenta_IdVenta(idVenta).isPresent()) {
            throw new RuntimeException("Ya existe una factura para esta venta");
        }
        String secuencial = venta.getSucursal().getCodEstablecimiento() + "-"
                + venta.getSucursal().getCodPuntoEmision() + "-"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        Factura factura = Factura.builder()
                .venta(venta).numeroSecuencial(secuencial)
                .estado(Factura.EstadoFactura.EMITIDA)
                .estadoSri(Factura.EstadoSri.NO_ENVIADO)
                .build();
        return toDTO(facturaRepository.save(factura));
    }

    @Override
    public FacturaResponseDTO buscarPorId(Integer id) {
        return toDTO(facturaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada: " + id)));
    }

    @Override
    public FacturaResponseDTO buscarPorVenta(Integer idVenta) {
        return toDTO(facturaRepository.findByVenta_IdVenta(idVenta)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada para venta: " + idVenta)));
    }

    @Override
    public byte[] generarPdf(Integer idFactura) {
        // Implementación básica - retorna PDF vacío hasta integrar iText
        return new byte[0];
    }

    @Override
    public List<FacturaResponseDTO> listarTodas() {
        return facturaRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public void anular(Integer id) {
        Factura f = facturaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada: " + id));
        f.setEstado(Factura.EstadoFactura.ANULADA);
        facturaRepository.save(f);
    }

    private FacturaResponseDTO toDTO(Factura f) {
        return FacturaResponseDTO.builder()
                .idFactura(f.getIdFactura())
                .idVenta(f.getVenta().getIdVenta())
                .numeroSecuencial(f.getNumeroSecuencial())
                .fechaEmision(f.getFechaEmision())
                .pdfPath(f.getPdfPath())
                .estado(f.getEstado().name())
                .estadoSri(f.getEstadoSri().name())
                .claveAcceso(f.getClaveAcceso())
                .build();
    }
}
