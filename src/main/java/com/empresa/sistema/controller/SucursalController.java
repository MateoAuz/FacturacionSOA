package com.empresa.sistema.controller;

import com.empresa.sistema.entity.Sucursal;
import com.empresa.sistema.repository.SucursalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sucursales")
@RequiredArgsConstructor
public class SucursalController {

    private final SucursalRepository sucursalRepository;

    @GetMapping
    public List<Sucursal> listar() {
        return sucursalRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Sucursal> buscarPorId(@PathVariable Integer id) {
        return sucursalRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Sucursal crear(@RequestBody Sucursal sucursal) {
        return sucursalRepository.save(sucursal);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Sucursal> actualizar(@PathVariable Integer id, @RequestBody Sucursal datos) {
        return sucursalRepository.findById(id).map(s -> {
            s.setNombre(datos.getNombre());
            s.setCiudad(datos.getCiudad());
            s.setDireccion(datos.getDireccion());
            s.setTelefono(datos.getTelefono());
            s.setActivo(datos.getActivo());
            return ResponseEntity.ok(sucursalRepository.save(s));
        }).orElse(ResponseEntity.notFound().build());
    }
}