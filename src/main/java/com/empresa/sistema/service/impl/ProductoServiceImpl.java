package com.empresa.sistema.service.impl;

import com.empresa.sistema.dto.request.ProductoRequestDTO;
import com.empresa.sistema.dto.response.ProductoResponseDTO;
import com.empresa.sistema.entity.Categoria;
import com.empresa.sistema.entity.Producto;
import com.empresa.sistema.repository.CategoriaRepository;
import com.empresa.sistema.repository.ProductoRepository;
import com.empresa.sistema.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    @Override
    public List<ProductoResponseDTO> listarTodos() {
        return productoRepository.findByActivoTrue().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public ProductoResponseDTO buscarPorId(Integer id) {
        return toDTO(productoRepository.findById(id).orElseThrow(() -> new RuntimeException("Producto no encontrado: " + id)));
    }

    @Override
    public ProductoResponseDTO buscarPorCodigo(String codigo) {
        return toDTO(productoRepository.findByCodigo(codigo).orElseThrow(() -> new RuntimeException("Producto no encontrado: " + codigo)));
    }

    @Override
    public ProductoResponseDTO crear(ProductoRequestDTO dto) {
        Categoria categoria = dto.getIdCategoria() != null ?
                categoriaRepository.findById(dto.getIdCategoria()).orElse(null) : null;
        Producto p = Producto.builder()
                .codigo(dto.getCodigo()).nombre(dto.getNombre()).descripcion(dto.getDescripcion())
                .categoria(categoria).precioVenta(dto.getPrecioVenta())
                .unidadMedida(dto.getUnidadMedida()).aplicaIva(dto.getAplicaIva()).activo(true)
                .tipoSri(Producto.TipoSri.valueOf(dto.getTipoSri())).build();
        return toDTO(productoRepository.save(p));
    }

    @Override
    public ProductoResponseDTO actualizar(Integer id, ProductoRequestDTO dto) {
        Producto p = productoRepository.findById(id).orElseThrow(() -> new RuntimeException("Producto no encontrado: " + id));
        Categoria categoria = dto.getIdCategoria() != null ?
                categoriaRepository.findById(dto.getIdCategoria()).orElse(null) : null;
        p.setNombre(dto.getNombre()); p.setDescripcion(dto.getDescripcion());
        p.setCategoria(categoria); p.setPrecioVenta(dto.getPrecioVenta());
        p.setUnidadMedida(dto.getUnidadMedida()); p.setAplicaIva(dto.getAplicaIva());
        p.setTipoSri(Producto.TipoSri.valueOf(dto.getTipoSri()));
        return toDTO(productoRepository.save(p));
    }

    @Override
    public void eliminar(Integer id) {
        productoRepository.findById(id).orElseThrow(() -> new RuntimeException("Producto no encontrado: " + id));
        productoRepository.deleteById(id);
    }

    @Override
    public List<ProductoResponseDTO> buscarPorCategoria(Integer idCategoria) {
        return productoRepository.findByCategoria_IdCategoriaAndActivoTrue(idCategoria)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<ProductoResponseDTO> buscarPorNombre(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCase(nombre)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public PageResponseDTO<ProductoResponseDTO> buscarPaginado(String search, Integer idCategoria, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("nombre").ascending());
        Page<Producto> resultado = productoRepository.buscarPaginado(
                (search != null && !search.isBlank()) ? search : null,
                idCategoria,
                pageable);
        return PageResponseDTO.<ProductoResponseDTO>builder()
                .contenido(resultado.getContent().stream().map(this::toDTO).collect(Collectors.toList()))
                .paginaActual(resultado.getNumber())
                .totalPaginas(resultado.getTotalPages())
                .totalElementos(resultado.getTotalElements())
                .tamanioPagina(resultado.getSize())
                .primera(resultado.isFirst())
                .ultima(resultado.isLast())
                .build();
    }

    private ProductoResponseDTO toDTO(Producto p) {
        return ProductoResponseDTO.builder()
                .idProducto(p.getIdProducto()).codigo(p.getCodigo()).nombre(p.getNombre())
                .descripcion(p.getDescripcion())
                .categoria(p.getCategoria() != null ? p.getCategoria().getNombre() : null)
                .precioVenta(p.getPrecioVenta()).unidadMedida(p.getUnidadMedida())
                .aplicaIva(p.getAplicaIva()).activo(p.getActivo())
                .tipoSri(p.getTipoSri().name()).fechaRegistro(p.getFechaRegistro()).build();
    }


}
