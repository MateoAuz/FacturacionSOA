package com.empresa.sistema.service.impl;

import com.empresa.sistema.dto.request.ClienteRequestDTO;
import com.empresa.sistema.dto.response.ClienteResponseDTO;
import com.empresa.sistema.entity.Cliente;
import com.empresa.sistema.repository.ClienteRepository;
import com.empresa.sistema.service.ClienteService;
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
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;

    @Override
    public List<ClienteResponseDTO> listarTodos() {
        return clienteRepository.findByActivoTrue().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public ClienteResponseDTO buscarPorId(Integer id) {
        return toDTO(clienteRepository.findById(id).orElseThrow(() -> new RuntimeException("Cliente no encontrado: " + id)));
    }

    @Override
    public ClienteResponseDTO buscarPorIdentificacion(String identificacion) {
        return toDTO(clienteRepository.findByIdentificacion(identificacion)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado: " + identificacion)));
    }

    @Override
    public ClienteResponseDTO crear(ClienteRequestDTO dto) {
        if (clienteRepository.findByIdentificacion(dto.getIdentificacion()).isPresent()) {
            throw new RuntimeException(
                "Ya existe un cliente registrado con la identificación " + dto.getIdentificacion() +
                ". Puedes buscarlo directamente en el listado.");
        }
        Cliente c = Cliente.builder()
                .tipoIdentificacion(Cliente.TipoIdentificacion.valueOf(dto.getTipoIdentificacion()))
                .identificacion(dto.getIdentificacion())
                .nombres(dto.getNombres())
                .apellidos(dto.getApellidos())
                .razonSocial(dto.getRazonSocial())
                .direccion(dto.getDireccion())
                .telefono(dto.getTelefono())
                .correo(dto.getCorreo())
                .activo(true)
                .build();
        return toDTO(clienteRepository.save(c));
    }

    @Override
    public ClienteResponseDTO actualizar(Integer id, ClienteRequestDTO dto) {
        Cliente c = clienteRepository.findById(id).orElseThrow(() -> new RuntimeException("Cliente no encontrado: " + id));

        // Si cambió la identificación, verificar que no exista en otro cliente
        String nuevaId = dto.getIdentificacion();
        if (nuevaId != null && !nuevaId.equals(c.getIdentificacion())) {
            clienteRepository.findByIdentificacion(nuevaId).ifPresent(existing -> {
                if (!existing.getIdCliente().equals(id)) {
                    throw new RuntimeException(
                        "Ya existe un cliente registrado con la identificación " + nuevaId);
                }
            });
            c.setIdentificacion(nuevaId);
            c.setTipoIdentificacion(Cliente.TipoIdentificacion.valueOf(dto.getTipoIdentificacion()));
        }

        c.setNombres(dto.getNombres());
        c.setApellidos(dto.getApellidos());
        c.setDireccion(dto.getDireccion());
        c.setTelefono(dto.getTelefono());
        c.setCorreo(dto.getCorreo());
        return toDTO(clienteRepository.save(c));
    }

    @Override
    public void eliminar(Integer id) {
        clienteRepository.findById(id).orElseThrow(() -> new RuntimeException("Cliente no encontrado: " + id));
        clienteRepository.deleteById(id);
    }

    @Override
    public void cambiarEstado(Integer id, Boolean activo) {
        Cliente c = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado: " + id));
        c.setActivo(activo);
        clienteRepository.save(c);
    }

    @Override
    public List<ClienteResponseDTO> buscarPorNombre(String nombre) {
        return clienteRepository.findByNombresContainingIgnoreCaseOrApellidosContainingIgnoreCase(nombre, nombre)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }
    @Override
    public PageResponseDTO<ClienteResponseDTO> buscarPaginado(String search, String campo, String tipo, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("nombres").ascending());
        Cliente.TipoIdentificacion tipoEnum = null;
        if (tipo != null && !tipo.isBlank()) {
            try { tipoEnum = Cliente.TipoIdentificacion.valueOf(tipo); } catch (Exception ignored) {}
        }
        Page<Cliente> resultado = clienteRepository.buscarPaginado(
                (search != null && !search.isBlank()) ? search : null,
                (campo != null && !campo.isBlank()) ? campo : null,
                tipoEnum,
                pageable);
        return PageResponseDTO.<ClienteResponseDTO>builder()
                .contenido(resultado.getContent().stream().map(this::toDTO).collect(Collectors.toList()))
                .paginaActual(resultado.getNumber())
                .totalPaginas(resultado.getTotalPages())
                .totalElementos(resultado.getTotalElements())
                .tamanioPagina(resultado.getSize())
                .primera(resultado.isFirst())
                .ultima(resultado.isLast())
                .build();
    }

    private ClienteResponseDTO toDTO(Cliente c) {
        return ClienteResponseDTO.builder()
                .idCliente(c.getIdCliente()).tipoIdentificacion(c.getTipoIdentificacion().name())
                .identificacion(c.getIdentificacion()).nombres(c.getNombres()).apellidos(c.getApellidos())
                .razonSocial(c.getRazonSocial()).direccion(c.getDireccion()).telefono(c.getTelefono())
                .correo(c.getCorreo()).activo(c.getActivo()).fechaRegistro(c.getFechaRegistro()).build();
    }
}
