package com.empresa.sistema.service.impl;

import com.empresa.sistema.dto.request.FacturaRequestDTO;
import com.empresa.sistema.dto.response.DetalleVentaResponseDTO;
import com.empresa.sistema.dto.response.FacturaResponseDTO;
import com.empresa.sistema.dto.response.PageResponseDTO;
import com.empresa.sistema.entity.*;
import com.empresa.sistema.dto.FacturaPagoDTO;
import com.empresa.sistema.entity.FacturaPago;
import com.empresa.sistema.repository.*;
import java.util.Collections;
import com.empresa.sistema.service.EmailService;
import com.empresa.sistema.service.FacturaService;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FacturaServiceImpl implements FacturaService {

    private final FacturaRepository              facturaRepository;
    private final DetalleVentaRepository         detalleRepository;
    private final ClienteRepository              clienteRepository;
    private final UsuarioRepository              usuarioRepository;
    private final SucursalRepository             sucursalRepository;
    private final ProductoRepository             productoRepository;
    private final InventarioRepository           inventarioRepository;
    private final ConfiguracionIvaRepository     ivaRepository;
    private final ConfiguracionEmpresaRepository empresaRepository;
    private final EmailService                   emailService;
    private final FacturaPagoRepository         facturaPagoRepository;

    // ────────────────────────────────────────────────────────────
    // Crear factura
    // ────────────────────────────────────────────────────────────
    @Override
    public FacturaResponseDTO crear(FacturaRequestDTO dto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario  usuario  = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Cliente  cliente  = clienteRepository.findById(dto.getIdCliente())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        // Bloquear la sucursal para evitar race conditions en la numeración concurrente
        Sucursal sucursal = sucursalRepository.findByIdWithLock(dto.getIdSucursal())
                .orElseThrow(() -> new RuntimeException("Sucursal no encontrada"));
        ConfiguracionIva iva = ivaRepository.findByActivoTrue()
                .orElseThrow(() -> new RuntimeException("Configuración IVA no encontrada"));

        // Validar stock
        for (var d : dto.getDetalles()) {
            Producto producto = productoRepository.findById(d.getIdProducto())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + d.getIdProducto()));
            Inventario inv = inventarioRepository
                    .findByProducto_IdProductoAndSucursal_IdSucursal(d.getIdProducto(), dto.getIdSucursal())
                    .orElseThrow(() -> new RuntimeException(
                            "'" + producto.getNombre() + "' no tiene stock en esta sucursal"));
            if (inv.getCantidad() < d.getCantidad())
                throw new RuntimeException("Stock insuficiente para '" + producto.getNombre() +
                        "'. Disponible: " + inv.getCantidad() + ", solicitado: " + d.getCantidad());
        }

        // Número secuencial por sucursal (el lock garantiza unicidad bajo concurrencia)
        long seqSucursal = facturaRepository.countBySucursal_IdSucursal(dto.getIdSucursal()) + 1;
        String numSec = String.format("%s-%s-%09d",
                sucursal.getCodEstablecimiento(), sucursal.getCodPuntoEmision(), seqSucursal);

        // ── Snapshots inmutables ─────────────────────────────────────
        String snapNombreUsuario = usuario.getNombre() + " " + usuario.getApellido();
        String snapNombreCliente = cliente.getNombres()
                + (cliente.getApellidos() != null ? " " + cliente.getApellidos() : "");

        Factura factura = Factura.builder()
                .numeroSecuencial(numSec)
                .cliente(cliente).usuario(usuario).sucursal(sucursal).configuracionIva(iva)
                .metodoPago(Factura.MetodoPago.valueOf(dto.getMetodoPago()))
                .observacion(dto.getObservacion())
                .estado(Factura.EstadoFactura.GUARDADA)
                // Snapshot del cliente
                .snapCliTipoId(cliente.getTipoIdentificacion().name())
                .snapCliIdentificacion(cliente.getIdentificacion())
                .snapCliNombres(cliente.getNombres())
                .snapCliApellidos(cliente.getApellidos())
                .snapCliRazonSocial(cliente.getRazonSocial())
                .snapCliCorreo(cliente.getCorreo())
                .snapCliTelefono(cliente.getTelefono())
                .snapCliDireccion(cliente.getDireccion())
                // Snapshot del vendedor y sucursal
                .snapUsuarioNombre(snapNombreUsuario)
                .snapSucursalNombre(sucursal.getNombre())
                .snapSucursalCiudad(sucursal.getCiudad())
                // Snapshot del IVA
                .snapIvaPorcentaje(iva.getPorcentaje())
                .build();

        BigDecimal subtotal = BigDecimal.ZERO;
        List<DetalleVenta> detalles = new ArrayList<>();

        for (var d : dto.getDetalles()) {
            Producto  producto = productoRepository.findById(d.getIdProducto()).get();
            Inventario inv     = inventarioRepository
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
                    .subtotalLinea(subtotalLinea)
                    .snapProductoNombre(producto.getNombre())
                    .snapProductoCodigo(producto.getCodigo())
                    .build());
        }

        BigDecimal ivaValor = subtotal.multiply(iva.getPorcentaje().divide(BigDecimal.valueOf(100)));
        factura.setSubtotal(subtotal);
        factura.setIvaValor(ivaValor);
        factura.setTotal(subtotal.add(ivaValor));

        Factura saved = facturaRepository.save(factura);
        detalles.forEach(d -> d.setFactura(saved));
        detalleRepository.saveAll(detalles);

        // Guardar formas de pago
        List<FacturaPago> pagosEntidad = buildPagos(dto, saved);
        facturaPagoRepository.saveAll(pagosEntidad);

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

        // 1. Cambiar estado y guardar (esto SÍ debe ser transaccional)
        f.setEstado(Factura.EstadoFactura.EMITIDA);
        f.setFechaEmision(LocalDateTime.now());
        facturaRepository.save(f);

        // 2. Generar PDF y enviar correo — aislado del rollback
        //    Si el correo o el PDF fallan, la factura ya quedó EMITIDA.
        try {
            List<DetalleVenta> detalles = detalleRepository.findByFactura_IdFactura(id);
            byte[] pdf = buildPdf(f, detalles);
            emailService.enviarFactura(f, detalles, pdf);
        } catch (Exception e) {
            log.warn("Factura #{} emitida, pero el correo/PDF falló: {}", id, e.getMessage());
        }
    }

    @Override
    public void anularFactura(Integer id) {
        Factura f = facturaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada: " + id));
        if (f.getEstado() == Factura.EstadoFactura.ANULADA)
            throw new RuntimeException("La factura ya está anulada");

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
    @Transactional(readOnly = true)
    public String proximoNumero(Integer idSucursal) {
        Sucursal sucursal = sucursalRepository.findById(idSucursal)
                .orElseThrow(() -> new RuntimeException("Sucursal no encontrada"));
        long seqSucursal = facturaRepository.countBySucursal_IdSucursal(idSucursal) + 1;
        return String.format("%s-%s-%09d",
                sucursal.getCodEstablecimiento(), sucursal.getCodPuntoEmision(), seqSucursal);
    }

    @Override
    public byte[] generarPdf(Integer idFactura) {
        Factura f = facturaRepository.findById(idFactura)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada: " + idFactura));
        List<DetalleVenta> detalles = detalleRepository.findByFactura_IdFactura(idFactura);
        return buildPdf(f, detalles);
    }

    // ════════════════════════════════════════════════════════════
    // PDF PROFESIONAL
    // ════════════════════════════════════════════════════════════
    private byte[] buildPdf(Factura f, List<DetalleVenta> detalles) {
        try {
            // ── Colors ──────────────────────────────────────────────
            BaseColor DARK_BLUE   = new BaseColor(30,  58, 138);   // #1e3a8a
            BaseColor LIGHT_BLUE  = new BaseColor(219, 234, 254);  // #dbeafe
            BaseColor GRAY_BG     = new BaseColor(243, 244, 246);  // #f3f4f6
            BaseColor GRAY_ROW    = new BaseColor(249, 250, 251);  // #f9fafb
            BaseColor BORDER_GRAY = new BaseColor(229, 231, 235);  // #e5e7eb
            BaseColor TEXT_GRAY   = new BaseColor(107, 114, 128);  // #6b7280
            BaseColor TEXT_DARK   = new BaseColor( 17,  24,  39);  // #111827

            // ── Fonts ────────────────────────────────────────────────
            Font fCompName  = new Font(Font.FontFamily.HELVETICA, 13, Font.BOLD,   DARK_BLUE);
            Font fCompInfo  = new Font(Font.FontFamily.HELVETICA,  8, Font.NORMAL, TEXT_GRAY);
            Font fCompLabel = new Font(Font.FontFamily.HELVETICA,  8, Font.BOLD,   TEXT_GRAY);
            Font fFactTitle = new Font(Font.FontFamily.HELVETICA, 17, Font.BOLD,   DARK_BLUE);
            Font fNumSec    = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD,   TEXT_DARK);
            Font fSmallGray = new Font(Font.FontFamily.HELVETICA,  7, Font.NORMAL, TEXT_GRAY);
            Font fSecHead   = new Font(Font.FontFamily.HELVETICA,  8, Font.BOLD,   DARK_BLUE);
            Font fLabel     = new Font(Font.FontFamily.HELVETICA,  8, Font.BOLD,   TEXT_GRAY);
            Font fValue     = new Font(Font.FontFamily.HELVETICA,  9, Font.NORMAL, TEXT_DARK);
            Font fThdr      = new Font(Font.FontFamily.HELVETICA,  8, Font.BOLD,   BaseColor.WHITE);
            Font fTcell     = new Font(Font.FontFamily.HELVETICA,  9, Font.NORMAL, TEXT_DARK);
            Font fTotLabel  = new Font(Font.FontFamily.HELVETICA,  9, Font.NORMAL, TEXT_DARK);
            Font fGrandTot  = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD,   DARK_BLUE);

            DateTimeFormatter dtf  = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            DateTimeFormatter dtfs = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            // ── Empresa data ─────────────────────────────────────────
            ConfiguracionEmpresa emp = empresaRepository.findFirstBy()
                    .orElse(new ConfiguracionEmpresa());
            String empNombre  = emp.getNombreComercial()  != null ? emp.getNombreComercial()  : nv(emp.getRazonSocial(), "EMPRESA");
            String empRazon   = emp.getRazonSocial()      != null ? emp.getRazonSocial()       : empNombre;
            String empRuc     = nv(emp.getRuc(),          "");
            String empDir     = nv(emp.getDireccionMatriz(), "");
            String empTel     = nv(emp.getTelefono(),     "");
            String empCorreo  = nv(emp.getCorreo(),       "");
            String ambiente   = (emp.getAmbiente() != null && emp.getAmbiente() == 2) ? "PRODUCCIÓN" : "PRUEBAS";

            // ── Document ─────────────────────────────────────────────
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4, 36f, 36f, 36f, 36f);
            PdfWriter writer = PdfWriter.getInstance(doc, baos);
            doc.open();

            // ══════════════════════════════════════════════════════════
            // 1. HEADER: empresa (izq) | FACTURA box (der)
            // ══════════════════════════════════════════════════════════
            PdfPTable hdr = new PdfPTable(2);
            hdr.setWidthPercentage(100);
            hdr.setWidths(new float[]{57f, 43f});
            hdr.setSpacingAfter(6f);

            // -- Celda izquierda: datos empresa
            PdfPCell hLeft = new PdfPCell();
            hLeft.setBorder(Rectangle.NO_BORDER);
            hLeft.setPaddingRight(10f);
            hLeft.setPaddingBottom(4f);

            Paragraph pCompName = new Paragraph(empNombre, fCompName);
            pCompName.setSpacingAfter(1f);
            hLeft.addElement(pCompName);
            if (!empRazon.equals(empNombre)) {
                hLeft.addElement(new Paragraph(empRazon, fCompInfo));
            }
            hLeft.addElement(mkLine("RUC: ", empRuc,        fCompLabel, fCompInfo));
            if (!empDir.isBlank())    hLeft.addElement(mkLine("Dirección: ", empDir,    fCompLabel, fCompInfo));
            if (!empTel.isBlank())    hLeft.addElement(mkLine("Tel: ",       empTel,    fCompLabel, fCompInfo));
            if (!empCorreo.isBlank()) hLeft.addElement(mkLine("Correo: ",    empCorreo, fCompLabel, fCompInfo));
            hdr.addCell(hLeft);

            // -- Celda derecha: FACTURA box
            PdfPCell hRight = new PdfPCell();
            hRight.setBorderColor(DARK_BLUE);
            hRight.setBorderWidth(1.5f);
            hRight.setPadding(8f);
            hRight.setVerticalAlignment(Element.ALIGN_MIDDLE);

            Paragraph pFacTit = new Paragraph("FACTURA", fFactTitle);
            pFacTit.setAlignment(Element.ALIGN_CENTER);
            hRight.addElement(pFacTit);

            Paragraph pNumSec = new Paragraph("No. " + f.getNumeroSecuencial(), fNumSec);
            pNumSec.setAlignment(Element.ALIGN_CENTER);
            pNumSec.setSpacingBefore(3f);
            hRight.addElement(pNumSec);

            Paragraph pAmb = new Paragraph("AMBIENTE: " + ambiente, fSmallGray);
            pAmb.setAlignment(Element.ALIGN_CENTER);
            pAmb.setSpacingBefore(2f);
            hRight.addElement(pAmb);

            if (f.getClaveAcceso() != null && !f.getClaveAcceso().isBlank()) {
                try {
                    Paragraph pClaveLbl = new Paragraph("CLAVE DE ACCESO", fSmallGray);
                    pClaveLbl.setAlignment(Element.ALIGN_CENTER);
                    pClaveLbl.setSpacingBefore(5f);
                    hRight.addElement(pClaveLbl);

                    Barcode128 bc = new Barcode128();
                    bc.setCode(f.getClaveAcceso());
                    bc.setBarHeight(28f);
                    bc.setX(0.9f);
                    bc.setFont(null);
                    Image bcImg = bc.createImageWithBarcode(writer.getDirectContent(), null, null);
                    bcImg.setAlignment(Element.ALIGN_CENTER);
                    bcImg.scaleToFit(178f, 34f);
                    hRight.addElement(bcImg);

                    Paragraph pClaveVal = new Paragraph(f.getClaveAcceso(), fSmallGray);
                    pClaveVal.setAlignment(Element.ALIGN_CENTER);
                    hRight.addElement(pClaveVal);
                } catch (Exception ignored) { /* barcode optional */ }
            }

            if (f.getFechaEmision() != null) {
                Paragraph pFecEm = new Paragraph("Fecha emisión: " + f.getFechaEmision().format(dtf), fSmallGray);
                pFecEm.setAlignment(Element.ALIGN_CENTER);
                pFecEm.setSpacingBefore(3f);
                hRight.addElement(pFecEm);
            }
            hdr.addCell(hRight);
            doc.add(hdr);

            // ══════════════════════════════════════════════════════════
            // 2. DATOS DEL ADQUIRIENTE
            // ══════════════════════════════════════════════════════════
            PdfPTable clientSec = new PdfPTable(1);
            clientSec.setWidthPercentage(100);
            clientSec.setSpacingBefore(4f);
            clientSec.setSpacingAfter(4f);

            // Título sección
            PdfPCell secTitleCell = new PdfPCell(new Phrase("DATOS DEL ADQUIRIENTE", fSecHead));
            secTitleCell.setBackgroundColor(LIGHT_BLUE);
            secTitleCell.setPadding(5f);
            secTitleCell.setBorderColor(DARK_BLUE);
            secTitleCell.setBorder(Rectangle.BOX);
            clientSec.addCell(secTitleCell);

            // Grid de datos cliente
            PdfPTable clientGrid = new PdfPTable(4);
            clientGrid.setWidthPercentage(100);
            clientGrid.setWidths(new float[]{22f, 28f, 22f, 28f});

            // Usar snapshot si está disponible, fallback a FK (facturas antiguas)
            String nomCliente   = snapCliente(f);
            String razonSocCliente = f.getSnapCliRazonSocial() != null
                    ? f.getSnapCliRazonSocial()
                    : (f.getCliente().getRazonSocial() != null ? f.getCliente().getRazonSocial() : nomCliente);
            String tipoId       = f.getSnapCliTipoId() != null
                    ? f.getSnapCliTipoId() : f.getCliente().getTipoIdentificacion().name();
            String identif      = snapIdentificacion(f);
            String dirCli       = nv(f.getSnapCliDireccion()  != null ? f.getSnapCliDireccion()  : f.getCliente().getDireccion(),  "—");
            String telCli       = nv(f.getSnapCliTelefono()   != null ? f.getSnapCliTelefono()   : f.getCliente().getTelefono(),   "—");
            String corrCli      = nv(f.getSnapCliCorreo()     != null ? f.getSnapCliCorreo()     : f.getCliente().getCorreo(),     "—");

            clientGrid.addCell(mkLabelCell("Razón Social:", fLabel, GRAY_BG));
            clientGrid.addCell(mkValueCell(razonSocCliente, fValue));
            clientGrid.addCell(mkLabelCell(tipoId + ":", fLabel, GRAY_BG));
            clientGrid.addCell(mkValueCell(identif, fValue));

            clientGrid.addCell(mkLabelCell("Dirección:", fLabel, GRAY_BG));
            clientGrid.addCell(mkValueCell(dirCli, fValue));
            clientGrid.addCell(mkLabelCell("Teléfono:", fLabel, GRAY_BG));
            clientGrid.addCell(mkValueCell(telCli, fValue));

            clientGrid.addCell(mkLabelCell("Correo:", fLabel, GRAY_BG));
            PdfPCell corrCell = mkValueCell(corrCli, fValue);
            corrCell.setColspan(3);
            clientGrid.addCell(corrCell);

            PdfPCell clientWrapper = new PdfPCell(clientGrid);
            clientWrapper.setBorder(Rectangle.LEFT | Rectangle.RIGHT | Rectangle.BOTTOM);
            clientWrapper.setBorderColor(DARK_BLUE);
            clientWrapper.setPadding(0f);
            clientSec.addCell(clientWrapper);
            doc.add(clientSec);

            // ══════════════════════════════════════════════════════════
            // 3. STRIP: sucursal / fecha / método pago
            // ══════════════════════════════════════════════════════════
            PdfPTable strip = new PdfPTable(3);
            strip.setWidthPercentage(100);
            strip.setWidths(new float[]{40f, 28f, 32f});
            strip.setSpacingAfter(6f);

            String pdfSucNombre = f.getSnapSucursalNombre() != null ? f.getSnapSucursalNombre() : f.getSucursal().getNombre();
            String pdfSucCiudad = f.getSnapSucursalCiudad() != null ? f.getSnapSucursalCiudad() : f.getSucursal().getCiudad();
            strip.addCell(mkStripCell(
                    "Sucursal: " + pdfSucNombre + " – " + pdfSucCiudad,
                    fValue, GRAY_BG, BORDER_GRAY));
            strip.addCell(mkStripCell("Fecha: " + f.getFechaFactura().format(dtfs), fValue, GRAY_BG, BORDER_GRAY));
            // Formas de pago para el strip — cargar desde factura_pago
            List<FacturaPago> pdfPagos = facturaPagoRepository
                    .findByFactura_IdFacturaOrderByIdPagoAsc(f.getIdFactura());
            String pagoResumen = pdfPagos.isEmpty()
                    ? f.getMetodoPago().name()
                    : pdfPagos.stream()
                              .map(p -> p.getMetodoPago().name() + " $" + p.getMonto().toPlainString())
                              .collect(java.util.stream.Collectors.joining(" / "));
            strip.addCell(mkStripCell("Pago: " + pagoResumen, fValue, GRAY_BG, BORDER_GRAY));
            doc.add(strip);

            // ══════════════════════════════════════════════════════════
            // 4. TABLA DE PRODUCTOS
            // ══════════════════════════════════════════════════════════
            PdfPTable prodTbl = new PdfPTable(5);
            prodTbl.setWidthPercentage(100);
            prodTbl.setWidths(new float[]{14f, 8f, 42f, 18f, 18f});
            prodTbl.setSpacingAfter(8f);

            String[] ths = {"CÓDIGO", "CANT.", "DESCRIPCIÓN", "P. UNITARIO", "TOTAL"};
            int[]   alns = {
                Element.ALIGN_LEFT, Element.ALIGN_CENTER,
                Element.ALIGN_LEFT, Element.ALIGN_RIGHT, Element.ALIGN_RIGHT
            };
            for (int i = 0; i < ths.length; i++) {
                PdfPCell th = new PdfPCell(new Phrase(ths[i], fThdr));
                th.setBackgroundColor(DARK_BLUE);
                th.setPadding(5f);
                th.setBorder(Rectangle.NO_BORDER);
                th.setHorizontalAlignment(alns[i]);
                prodTbl.addCell(th);
            }

            boolean alt = false;
            for (DetalleVenta det : detalles) {
                BaseColor rowBg = alt ? GRAY_ROW : BaseColor.WHITE;
                alt = !alt;
                prodTbl.addCell(mkTcell(nv(snapProductoCodigo(det), ""), fTcell, rowBg, Element.ALIGN_LEFT,  BORDER_GRAY));
                prodTbl.addCell(mkTcell(String.valueOf(det.getCantidad()),         fTcell, rowBg, Element.ALIGN_CENTER, BORDER_GRAY));
                prodTbl.addCell(mkTcell(snapProductoNombre(det),                   fTcell, rowBg, Element.ALIGN_LEFT,  BORDER_GRAY));
                prodTbl.addCell(mkTcell("$ " + fmt(det.getPrecioUnitario()),     fTcell, rowBg, Element.ALIGN_RIGHT, BORDER_GRAY));
                prodTbl.addCell(mkTcell("$ " + fmt(det.getSubtotalLinea()),      fTcell, rowBg, Element.ALIGN_RIGHT, BORDER_GRAY));
            }
            doc.add(prodTbl);

            // ══════════════════════════════════════════════════════════
            // 5. INFO ADICIONAL (izq) | TOTALES (der)
            // ══════════════════════════════════════════════════════════
            PdfPTable footerTbl = new PdfPTable(2);
            footerTbl.setWidthPercentage(100);
            footerTbl.setWidths(new float[]{54f, 46f});

            // -- Izquierda: info adicional
            PdfPCell infoCell = new PdfPCell();
            infoCell.setBorderColor(BORDER_GRAY);
            infoCell.setBorder(Rectangle.BOX);
            infoCell.setPadding(8f);

            Paragraph pInfoTit = new Paragraph("INFORMACIÓN ADICIONAL", fSecHead);
            pInfoTit.setSpacingAfter(5f);
            infoCell.addElement(pInfoTit);
            // Formas de pago detalladas en sección info
            if (pdfPagos.isEmpty()) {
                infoCell.addElement(mkLine("Método de pago: ", f.getMetodoPago().name(), fLabel, fValue));
            } else {
                for (FacturaPago fp : pdfPagos) {
                    infoCell.addElement(mkLine(
                        "Pago " + fp.getMetodoPago().name() + ": ",
                        "$" + fp.getMonto().toPlainString(),
                        fLabel, fValue));
                }
            }
            if (f.getObservacion() != null && !f.getObservacion().isBlank())
                infoCell.addElement(mkLine("Observaciones: ", f.getObservacion(), fLabel, fValue));
            infoCell.addElement(mkLine("Estado: ", f.getEstado().name(), fLabel, fValue));
            footerTbl.addCell(infoCell);

            // -- Derecha: totales
            PdfPCell totalsCell = new PdfPCell();
            totalsCell.setBorderColor(BORDER_GRAY);
            totalsCell.setBorder(Rectangle.BOX);
            totalsCell.setPadding(8f);

            java.math.BigDecimal ivaBd = f.getSnapIvaPorcentaje() != null
                    ? f.getSnapIvaPorcentaje() : f.getConfiguracionIva().getPorcentaje();
            String ivaPct = ivaBd.stripTrailingZeros().toPlainString();
            PdfPTable totTbl = new PdfPTable(2);
            totTbl.setWidthPercentage(100);
            totTbl.setWidths(new float[]{58f, 42f});

            addTotRow(totTbl, "Subtotal sin IVA:",
                    "$ " + fmt(f.getSubtotal()), fTotLabel, false, BaseColor.WHITE, BORDER_GRAY);
            addTotRow(totTbl, "IVA " + ivaPct + "%:",
                    "$ " + fmt(f.getIvaValor()),  fTotLabel, false, BaseColor.WHITE, BORDER_GRAY);
            addTotRow(totTbl, "TOTAL A PAGAR:",
                    "$ " + fmt(f.getTotal()),     fGrandTot, true,  LIGHT_BLUE,     DARK_BLUE);

            totalsCell.addElement(totTbl);
            footerTbl.addCell(totalsCell);
            doc.add(footerTbl);

            doc.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error al generar PDF: " + e.getMessage(), e);
        }
    }

    // ═══════════════════════════════════════════════════════
    // PDF helper methods
    // ═══════════════════════════════════════════════════════

    private Paragraph mkLine(String lbl, String val, Font fLbl, Font fVal) {
        Paragraph p = new Paragraph();
        p.add(new Chunk(lbl, fLbl));
        p.add(new Chunk(val != null ? val : "", fVal));
        return p;
    }

    private PdfPCell mkLabelCell(String text, Font font, BaseColor bg) {
        PdfPCell c = new PdfPCell(new Phrase(text, font));
        c.setBackgroundColor(bg);
        c.setBorder(Rectangle.NO_BORDER);
        c.setPadding(4f);
        return c;
    }

    private PdfPCell mkValueCell(String text, Font font) {
        PdfPCell c = new PdfPCell(new Phrase(text != null ? text : "", font));
        c.setBorder(Rectangle.NO_BORDER);
        c.setPadding(4f);
        return c;
    }

    private PdfPCell mkStripCell(String text, Font font, BaseColor bg, BaseColor border) {
        PdfPCell c = new PdfPCell(new Phrase(text, font));
        c.setBackgroundColor(bg);
        c.setPadding(5f);
        c.setBorder(Rectangle.BOX);
        c.setBorderColor(border);
        return c;
    }

    private PdfPCell mkTcell(String text, Font font, BaseColor bg, int align, BaseColor border) {
        PdfPCell c = new PdfPCell(new Phrase(text, font));
        c.setBackgroundColor(bg);
        c.setPadding(5f);
        c.setBorder(Rectangle.BOTTOM);
        c.setBorderColor(border);
        c.setHorizontalAlignment(align);
        return c;
    }

    private void addTotRow(PdfPTable tbl, String label, String value,
                            Font font, boolean strong, BaseColor bg, BaseColor border) {
        PdfPCell lc = new PdfPCell(new Phrase(label, font));
        lc.setBackgroundColor(bg);
        lc.setPadding(5f);
        lc.setBorder(Rectangle.TOP);
        lc.setBorderColor(border);
        if (strong) lc.setBorderWidthTop(1.5f);

        PdfPCell vc = new PdfPCell(new Phrase(value, font));
        vc.setBackgroundColor(bg);
        vc.setPadding(5f);
        vc.setHorizontalAlignment(Element.ALIGN_RIGHT);
        vc.setBorder(Rectangle.TOP);
        vc.setBorderColor(border);
        if (strong) vc.setBorderWidthTop(1.5f);

        tbl.addCell(lc);
        tbl.addCell(vc);
    }

    private String fmt(BigDecimal val) {
        return val != null ? String.format("%.2f", val) : "0.00";
    }

    private String nv(String s, String def) {
        return (s != null && !s.isBlank()) ? s : def;
    }

    // ─────────────────────────────────────────────────────────────────
    // Helpers: preferir snapshot → fallback a FK (compatibilidad con registros anteriores)
    // ─────────────────────────────────────────────────────────────────
    private String snapCliente(Factura f) {
        if (f.getSnapCliNombres() != null) {
            return f.getSnapCliNombres()
                    + (f.getSnapCliApellidos() != null ? " " + f.getSnapCliApellidos() : "");
        }
        return f.getCliente().getNombres()
                + (f.getCliente().getApellidos() != null ? " " + f.getCliente().getApellidos() : "");
    }
    private String snapIdentificacion(Factura f) {
        return f.getSnapCliIdentificacion() != null
                ? f.getSnapCliIdentificacion() : f.getCliente().getIdentificacion();
    }
    private String snapUsuario(Factura f) {
        return f.getSnapUsuarioNombre() != null
                ? f.getSnapUsuarioNombre()
                : f.getUsuario().getNombre() + " " + f.getUsuario().getApellido();
    }
    private String snapSucursal(Factura f) {
        return f.getSnapSucursalNombre() != null
                ? f.getSnapSucursalNombre() : f.getSucursal().getNombre();
    }
    private String snapProductoNombre(DetalleVenta d) {
        return d.getSnapProductoNombre() != null
                ? d.getSnapProductoNombre() : d.getProducto().getNombre();
    }
    private String snapProductoCodigo(DetalleVenta d) {
        return d.getSnapProductoCodigo() != null
                ? d.getSnapProductoCodigo() : d.getProducto().getCodigo();
    }

    /** Convierte la lista de pagos del DTO en entidades FacturaPago. */
    private List<FacturaPago> buildPagos(FacturaRequestDTO dto, Factura factura) {
        if (dto.getPagos() != null && !dto.getPagos().isEmpty()) {
            return dto.getPagos().stream()
                    .map(p -> FacturaPago.builder()
                            .factura(factura)
                            .metodoPago(Factura.MetodoPago.valueOf(p.getMetodo()))
                            .monto(p.getMonto())
                            .build())
                    .collect(Collectors.toList());
        }
        // Compatibilidad: si no se envía lista, usar el campo metodoPago legacy
        return Collections.singletonList(
                FacturaPago.builder()
                        .factura(factura)
                        .metodoPago(factura.getMetodoPago())
                        .monto(factura.getTotal())
                        .build());
    }

    private FacturaResponseDTO toDTO(Factura f, boolean incluirDetalles) {
        List<DetalleVentaResponseDTO> detalles = null;
        if (incluirDetalles) {
            detalles = detalleRepository.findByFactura_IdFactura(f.getIdFactura()).stream()
                    .map(d -> DetalleVentaResponseDTO.builder()
                            .idDetalle(d.getIdDetalle())
                            .producto(snapProductoNombre(d))
                            .codigoProducto(snapProductoCodigo(d))
                            .cantidad(d.getCantidad())
                            .precioUnitario(d.getPrecioUnitario())
                            .subtotalLinea(d.getSubtotalLinea()).build())
                    .collect(Collectors.toList());
        }
        // Cargar formas de pago
        List<FacturaPagoDTO> pagos = facturaPagoRepository
                .findByFactura_IdFacturaOrderByIdPagoAsc(f.getIdFactura()).stream()
                .map(p -> FacturaPagoDTO.builder()
                        .metodo(p.getMetodoPago().name())
                        .monto(p.getMonto())
                        .build())
                .collect(Collectors.toList());
        return FacturaResponseDTO.builder()
                .idFactura(f.getIdFactura())
                .numeroSecuencial(f.getNumeroSecuencial())
                .cliente(snapCliente(f))
                .identificacionCliente(snapIdentificacion(f))
                .usuario(snapUsuario(f))
                .sucursal(snapSucursal(f))
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
                .pagos(pagos)
                .build();
    }
}
