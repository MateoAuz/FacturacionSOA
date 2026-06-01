package com.empresa.sistema.service.impl;

import com.empresa.sistema.dto.request.FacturaRequestDTO;
import com.empresa.sistema.dto.response.DetalleVentaResponseDTO;
import com.empresa.sistema.dto.response.FacturaResponseDTO;
import com.empresa.sistema.dto.response.PageResponseDTO;
import com.empresa.sistema.entity.*;
import com.empresa.sistema.repository.*;
import com.empresa.sistema.service.FacturaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FacturaServiceImpl implements FacturaService {

    private final FacturaRepository        facturaRepository;
    private final DetalleVentaRepository   detalleRepository;
    private final ClienteRepository        clienteRepository;
    private final UsuarioRepository        usuarioRepository;
    private final SucursalRepository       sucursalRepository;
    private final ProductoRepository       productoRepository;
    private final InventarioRepository     inventarioRepository;
    private final ConfiguracionIvaRepository ivaRepository;

    // ────────────────────────────────────────────────────────────
    // Crear factura (crea venta + detalles + descuenta stock)
    // ────────────────────────────────────────────────────────────
    @Override
    public FacturaResponseDTO crear(FacturaRequestDTO dto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario  = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Cliente cliente  = clienteRepository.findById(dto.getIdCliente())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        Sucursal sucursal = sucursalRepository.findById(dto.getIdSucursal())
                .orElseThrow(() -> new RuntimeException("Sucursal no encontrada"));
        ConfiguracionIva iva = ivaRepository.findByActivoTrue()
                .orElseThrow(() -> new RuntimeException("Configuración IVA no encontrada"));

        // Validar stock antes de procesar
        for (var d : dto.getDetalles()) {
            Producto producto = productoRepository.findById(d.getIdProducto())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + d.getIdProducto()));
            Inventario inv = inventarioRepository
                    .findByProducto_IdProductoAndSucursal_IdSucursal(d.getIdProducto(), dto.getIdSucursal())
                    .orElseThrow(() -> new RuntimeException(
                            "'" + producto.getNombre() + "' no tiene stock en esta sucursal"));
            if (inv.getCantidad() < d.getCantidad()) {
                throw new RuntimeException(
                        "Stock insuficiente para '" + producto.getNombre() +
                        "'. Disponible: " + inv.getCantidad() + ", solicitado: " + d.getCantidad());
            }
        }

        // Generar número secuencial
        long count = facturaRepository.count() + 1;
        String numSec = String.format("%s-%s-%09d",
                sucursal.getCodEstablecimiento(), sucursal.getCodPuntoEmision(), count);

        Factura factura = Factura.builder()
                .numeroSecuencial(numSec)
                .cliente(cliente).usuario(usuario).sucursal(sucursal).configuracionIva(iva)
                .metodoPago(Factura.MetodoPago.valueOf(dto.getMetodoPago()))
                .observacion(dto.getObservacion())
                .estado(Factura.EstadoFactura.GUARDADA)
                .build();

        BigDecimal subtotal = BigDecimal.ZERO;
        List<DetalleVenta> detalles = new ArrayList<>();

        for (var d : dto.getDetalles()) {
            Producto producto = productoRepository.findById(d.getIdProducto()).get();

            // Descontar stock
            Inventario inv = inventarioRepository
                    .findByProducto_IdProductoAndSucursal_IdSucursal(d.getIdProducto(), dto.getIdSucursal()).get();
            inv.setCantidad(inv.getCantidad() - d.getCantidad());
            inv.setUltimaActualizacion(LocalDateTime.now());
            inventarioRepository.save(inv);

            BigDecimal subtotalLinea = producto.getPrecioVenta().multiply(BigDecimal.valueOf(d.getCantidad()));
            subtotal = subtotal.add(subtotalLinea);

            detalles.add(DetalleVenta.builder()
                    .factura(factura).producto(producto)
                    .cantidad(d.getCantidad())
                    .precioUnitario(producto.getPrecioVenta())
                    .subtotalLinea(subtotalLinea).build());
        }

        BigDecimal ivaValor = subtotal.multiply(iva.getPorcentaje().divide(BigDecimal.valueOf(100)));
        factura.setSubtotal(subtotal);
        factura.setIvaValor(ivaValor);
        factura.setTotal(subtotal.add(ivaValor));

        Factura saved = facturaRepository.save(factura);
        detalles.forEach(d -> d.setFactura(saved));
        detalleRepository.saveAll(detalles);

        return toDTO(saved, true);
    }

    // ────────────────────────────────────────────────────────────
    @Override
    public FacturaResponseDTO buscarPorId(Integer id) {
        return toDTO(facturaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada: " + id)), true);
    }

    @Override
    public void emitirFactura(Integer id) {
        Factura f = facturaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada: " + id));
        if (f.getEstado() != Factura.EstadoFactura.GUARDADA)
            throw new RuntimeException("Solo se pueden emitir facturas en estado GUARDADA");
        f.setEstado(Factura.EstadoFactura.EMITIDA);
        f.setFechaEmision(LocalDateTime.now());
        facturaRepository.save(f);
    }

    @Override
    public void anularFactura(Integer id) {
        Factura f = facturaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada: " + id));
        if (f.getEstado() == Factura.EstadoFactura.ANULADA)
            throw new RuntimeException("La factura ya está anulada");

        // Devolver stock
        List<DetalleVenta> detalles = detalleRepository.findByFactura_IdFactura(id);
        for (DetalleVenta det : detalles) {
            inventarioRepository
                    .findByProducto_IdProductoAndSucursal_IdSucursal(
                            det.getProducto().getIdProducto(), f.getSucursal().getIdSucursal())
                    .ifPresent(inv -> {
                        inv.setCantidad(inv.getCantidad() + det.getCantidad());
                        inv.setUltimaActualizacion(LocalDateTime.now());
                        inventarioRepository.save(inv);
                    });
        }

        f.setEstado(Factura.EstadoFactura.ANULADA);
        facturaRepository.save(f);
    }

    @Override
    public List<FacturaResponseDTO> listarTodas() {
        return facturaRepository.findAll().stream()
                .map(f -> toDTO(f, false)).collect(Collectors.toList());
    }

    @Override
    public List<FacturaResponseDTO> listarPorSucursal(Integer idSucursal) {
        return facturaRepository.findBySucursal_IdSucursal(idSucursal).stream()
                .map(f -> toDTO(f, false)).collect(Collectors.toList());
    }

    @Override
    public PageResponseDTO<FacturaResponseDTO> buscarPaginado(String search, String estado,
                                                               Integer idSucursal, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaFactura").descending());
        Factura.EstadoFactura estadoEnum = (estado != null && !estado.isBlank())
                ? Factura.EstadoFactura.valueOf(estado) : null;
        Page<Factura> resultado = facturaRepository.buscarPaginado(
                (search != null && !search.isBlank()) ? search : null,
                estadoEnum, idSucursal, pageable);
        return PageResponseDTO.<FacturaResponseDTO>builder()
                .contenido(resultado.getContent().stream().map(f -> toDTO(f, false)).collect(Collectors.toList()))
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
        Factura f = facturaRepository.findById(idFactura)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada: " + idFactura));
        List<DetalleVenta> detalles = detalleRepository.findByFactura_IdFactura(idFactura);

        try {
            com.itextpdf.text.Document document = new com.itextpdf.text.Document();
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            com.itextpdf.text.pdf.PdfWriter.getInstance(document, baos);
            document.open();

            com.itextpdf.text.Font fTitulo = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 16, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Font fNormal = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10);
            com.itextpdf.text.Font fBold   = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD);

            com.itextpdf.text.Paragraph titulo = new com.itextpdf.text.Paragraph("FACTURA", fTitulo);
            titulo.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            document.add(titulo);
            document.add(new com.itextpdf.text.Paragraph(" "));

            document.add(new com.itextpdf.text.Paragraph("N° Factura: " + f.getNumeroSecuencial(), fBold));
            document.add(new com.itextpdf.text.Paragraph("Fecha: " + f.getFechaFactura().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), fNormal));
            document.add(new com.itextpdf.text.Paragraph("Sucursal: " + f.getSucursal().getNombre() + " - " + f.getSucursal().getCiudad(), fNormal));
            document.add(new com.itextpdf.text.Paragraph(" "));

            document.add(new com.itextpdf.text.Paragraph("DATOS DEL CLIENTE", fBold));
            document.add(new com.itextpdf.text.Paragraph("Nombre: " + f.getCliente().getNombres() + " " + (f.getCliente().getApellidos() != null ? f.getCliente().getApellidos() : ""), fNormal));
            document.add(new com.itextpdf.text.Paragraph("Identificación: " + f.getCliente().getIdentificacion(), fNormal));
            document.add(new com.itextpdf.text.Paragraph(" "));

            com.itextpdf.text.pdf.PdfPTable tabla = new com.itextpdf.text.pdf.PdfPTable(4);
            tabla.setWidthPercentage(100);
            tabla.setWidths(new float[]{40f, 15f, 20f, 25f});
            tabla.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Producto", fBold)));
            tabla.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Cantidad", fBold)));
            tabla.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Precio Unit.", fBold)));
            tabla.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Subtotal", fBold)));

            for (var det : detalles) {
                tabla.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(det.getProducto().getNombre(), fNormal)));
                tabla.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(String.valueOf(det.getCantidad()), fNormal)));
                tabla.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("$" + det.getPrecioUnitario(), fNormal)));
                tabla.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("$" + det.getSubtotalLinea(), fNormal)));
            }
            document.add(tabla);
            document.add(new com.itextpdf.text.Paragraph(" "));

            document.add(new com.itextpdf.text.Paragraph("Subtotal: $" + f.getSubtotal(), fNormal));
            document.add(new com.itextpdf.text.Paragraph("IVA (" + f.getConfiguracionIva().getPorcentaje() + "%): $" + f.getIvaValor(), fNormal));
            document.add(new com.itextpdf.text.Paragraph("TOTAL: $" + f.getTotal(), fBold));
            document.add(new com.itextpdf.text.Paragraph("Método de pago: " + f.getMetodoPago().name(), fNormal));

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar PDF: " + e.getMessage());
        }
    }

    // ────────────────────────────────────────────────────────────
    private FacturaResponseDTO toDTO(Factura f, boolean incluirDetalles) {
        List<DetalleVentaResponseDTO> detalles = null;
        if (incluirDetalles) {
            detalles = detalleRepository.findByFactura_IdFactura(f.getIdFactura()).stream()
                    .map(d -> DetalleVentaResponseDTO.builder()
                            .idDetalle(d.getIdDetalle())
                            .producto(d.getProducto().getNombre())
                            .codigoProducto(d.getProducto().getCodigo())
                            .cantidad(d.getCantidad())
                            .precioUnitario(d.getPrecioUnitario())
                            .subtotalLinea(d.getSubtotalLinea()).build())
                    .collect(Collectors.toList());
        }

        return FacturaResponseDTO.builder()
                .idFactura(f.getIdFactura())
                .numeroSecuencial(f.getNumeroSecuencial())
                .cliente(f.getCliente().getNombres() + " " + (f.getCliente().getApellidos() != null ? f.getCliente().getApellidos() : ""))
                .identificacionCliente(f.getCliente().getIdentificacion())
                .usuario(f.getUsuario().getNombre() + " " + f.getUsuario().getApellido())
                .sucursal(f.getSucursal().getNombre())
                .idSucursal(f.getSucursal().getIdSucursal())
                .fechaFactura(f.getFechaFactura())
                .subtotal(f.getSubtotal())
                .ivaValor(f.getIvaValor())
                .total(f.getTotal())
                .metodoPago(f.getMetodoPago().name())
                .estado(f.getEstado().name())
                .estadoSri(f.getEstadoSri().name())
                .observacion(f.getObservacion())
                .fechaEmision(f.getFechaEmision())
                .pdfPath(f.getPdfPath())
                .claveAcceso(f.getClaveAcceso())
                .detalles(detalles)
                .build();
    }
}
