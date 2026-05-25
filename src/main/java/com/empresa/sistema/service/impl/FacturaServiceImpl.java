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
import com.empresa.sistema.dto.response.PageResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

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
        long count = facturaRepository.count() + 1;
        String secuencial = String.format("%s-%s-%09d",
                venta.getSucursal().getCodEstablecimiento(),
                venta.getSucursal().getCodPuntoEmision(),
                count);
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
    public PageResponseDTO<FacturaResponseDTO> buscarPaginado(String search, String estado, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaEmision").descending());
        Factura.EstadoFactura estadoEnum = (estado != null && !estado.isBlank()) ?
                Factura.EstadoFactura.valueOf(estado) : null;
        Page<Factura> resultado = facturaRepository.buscarPaginado(
                (search != null && !search.isBlank()) ? search : null,
                estadoEnum,
                pageable);
        return PageResponseDTO.<FacturaResponseDTO>builder()
                .contenido(resultado.getContent().stream().map(this::toDTO).collect(Collectors.toList()))
                .paginaActual(resultado.getNumber())
                .totalPaginas(resultado.getTotalPages())
                .totalElementos(resultado.getTotalElements())
                .tamanioPagina(resultado.getSize())
                .primera(resultado.isFirst())
                .ultima(resultado.isLast())
                .build();
    }

    @Override
    public byte[] generarPdf(Integer idFactura) {
        Factura factura = facturaRepository.findById(idFactura)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada: " + idFactura));

        Venta venta = factura.getVenta();
        var detalles = detalleVentaRepository.findByVenta_IdVenta(venta.getIdVenta());

        try {
            com.itextpdf.text.Document document = new com.itextpdf.text.Document();
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            com.itextpdf.text.pdf.PdfWriter.getInstance(document, baos);
            document.open();

            com.itextpdf.text.Font fuenteTitulo = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 16, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Font fuenteNormal = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 10);
            com.itextpdf.text.Font fuenteBold = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD);

            com.itextpdf.text.Paragraph titulo = new com.itextpdf.text.Paragraph("FACTURA", fuenteTitulo);
            titulo.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            document.add(titulo);
            document.add(new com.itextpdf.text.Paragraph(" "));

            document.add(new com.itextpdf.text.Paragraph("N° Factura: " + factura.getNumeroSecuencial(), fuenteBold));
            document.add(new com.itextpdf.text.Paragraph("Fecha: " + factura.getFechaEmision().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), fuenteNormal));
            document.add(new com.itextpdf.text.Paragraph("Sucursal: " + venta.getSucursal().getNombre() + " - " + venta.getSucursal().getCiudad(), fuenteNormal));
            document.add(new com.itextpdf.text.Paragraph(" "));

            document.add(new com.itextpdf.text.Paragraph("DATOS DEL CLIENTE", fuenteBold));
            document.add(new com.itextpdf.text.Paragraph("Nombre: " + venta.getCliente().getNombres() + " " + (venta.getCliente().getApellidos() != null ? venta.getCliente().getApellidos() : ""), fuenteNormal));
            document.add(new com.itextpdf.text.Paragraph("Identificación: " + venta.getCliente().getIdentificacion(), fuenteNormal));
            if (venta.getCliente().getDireccion() != null) {
                document.add(new com.itextpdf.text.Paragraph("Dirección: " + venta.getCliente().getDireccion(), fuenteNormal));
            }
            document.add(new com.itextpdf.text.Paragraph(" "));

            com.itextpdf.text.pdf.PdfPTable tabla = new com.itextpdf.text.pdf.PdfPTable(4);
            tabla.setWidthPercentage(100);
            tabla.setWidths(new float[]{40f, 15f, 20f, 25f});
            tabla.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Producto", fuenteBold)));
            tabla.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Cantidad", fuenteBold)));
            tabla.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Precio Unit.", fuenteBold)));
            tabla.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Subtotal", fuenteBold)));

            for (var detalle : detalles) {
                tabla.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(detalle.getProducto().getNombre(), fuenteNormal)));
                tabla.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(String.valueOf(detalle.getCantidad()), fuenteNormal)));
                tabla.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("$" + detalle.getPrecioUnitario(), fuenteNormal)));
                tabla.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("$" + detalle.getSubtotalLinea(), fuenteNormal)));
            }
            document.add(tabla);
            document.add(new com.itextpdf.text.Paragraph(" "));

            document.add(new com.itextpdf.text.Paragraph("Subtotal: $" + venta.getSubtotal(), fuenteNormal));
            document.add(new com.itextpdf.text.Paragraph("IVA (" + venta.getConfiguracionIva().getPorcentaje() + "%): $" + venta.getIvaValor(), fuenteNormal));
            document.add(new com.itextpdf.text.Paragraph("TOTAL: $" + venta.getTotal(), fuenteBold));
            document.add(new com.itextpdf.text.Paragraph(" "));
            document.add(new com.itextpdf.text.Paragraph("Método de pago: " + venta.getMetodoPago().name(), fuenteNormal));

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error al generar PDF: " + e.getMessage());
        }
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
        String clienteNombre = null;
        String identificacion = null;
        java.math.BigDecimal total = null;
        String sucursalNombre = null;

        try {
            clienteNombre = f.getVenta().getCliente().getNombres() + " " +
                    (f.getVenta().getCliente().getApellidos() != null ? f.getVenta().getCliente().getApellidos() : "");
            identificacion = f.getVenta().getCliente().getIdentificacion();
            total = f.getVenta().getTotal();
            sucursalNombre = f.getVenta().getSucursal().getNombre();
        } catch (Exception e) {
            System.out.println("ERROR en toDTO: " + e.getMessage());
        }

        return FacturaResponseDTO.builder()
                .idFactura(f.getIdFactura())
                .idVenta(f.getVenta().getIdVenta())
                .numeroSecuencial(f.getNumeroSecuencial())
                .fechaEmision(f.getFechaEmision())
                .pdfPath(f.getPdfPath())
                .estado(f.getEstado().name())
                .estadoSri(f.getEstadoSri().name())
                .claveAcceso(f.getClaveAcceso())
                .cliente(clienteNombre)
                .identificacionCliente(identificacion)
                .total(total)
                .sucursal(sucursalNombre)
                .build();
    }
}