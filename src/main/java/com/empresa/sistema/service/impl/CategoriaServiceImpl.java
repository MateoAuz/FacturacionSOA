package com.empresa.sistema.service.impl;

import com.empresa.sistema.dto.request.CategoriaRequestDTO;
import com.empresa.sistema.dto.response.CategoriaResponseDTO;
import com.empresa.sistema.entity.Categoria;
import com.empresa.sistema.repository.CategoriaRepository;
import com.empresa.sistema.service.CategoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;

    @Override
    public List<CategoriaResponseDTO> listarTodas() {
        return categoriaRepository.findByActivoTrue().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public CategoriaResponseDTO buscarPorId(Integer id) {
        return toDTO(categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada: " + id)));
    }

    @Override
    public CategoriaResponseDTO crear(CategoriaRequestDTO dto) {
        Categoria c = Categoria.builder().nombre(dto.getNombre()).descripcion(dto.getDescripcion()).activo(true).build();
        return toDTO(categoriaRepository.save(c));
    }

    @Override
    public CategoriaResponseDTO actualizar(Integer id, CategoriaRequestDTO dto) {
        Categoria c = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada: " + id));
        c.setNombre(dto.getNombre());
        c.setDescripcion(dto.getDescripcion());
        return toDTO(categoriaRepository.save(c));
    }

    @Override
    public void eliminar(Integer id) {
        categoriaRepository.findById(id).orElseThrow(() -> new RuntimeException("Categoría no encontrada: " + id));
        categoriaRepository.deleteById(id);
    }

    private CategoriaResponseDTO toDTO(Categoria c) {
        return CategoriaResponseDTO.builder().idCategoria(c.getIdCategoria())
                .nombre(c.getNombre()).descripcion(c.getDescripcion()).activo(c.getActivo()).build();
    }
}
