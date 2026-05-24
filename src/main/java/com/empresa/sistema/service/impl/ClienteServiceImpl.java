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
    public List<ClienteResponseDTO> buscarPorNombre(String nombre) {
        return clienteRepository.findByNombresContainingIgnoreCaseOrApellidosContainingIgnoreCase(nombre, nombre)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    private ClienteResponseDTO toDTO(Cliente c) {
        return ClienteResponseDTO.builder()
                .idCliente(c.getIdCliente()).tipoIdentificacion(c.getTipoIdentificacion().name())
                .identificacion(c.getIdentificacion()).nombres(c.getNombres()).apellidos(c.getApellidos())
                .razonSocial(c.getRazonSocial()).direccion(c.getDireccion()).telefono(c.getTelefono())
                .correo(c.getCorreo()).activo(c.getActivo()).fechaRegistro(c.getFechaRegistro()).build();
    }
}
