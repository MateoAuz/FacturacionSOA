package com.empresa.sistema.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class VistaController {

    @GetMapping({"/", "/login"})
    public String login() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }

    @GetMapping("/productos")
    public String productos() {
        return "gestion/productos";
    }

    @GetMapping("/clientes")
    public String clientes() {
        return "gestion/clientes";
    }

    @GetMapping("/inventario")
    public RedirectView inventario() {
        return new RedirectView("/productos");
    }

    @GetMapping("/ventas")
    public String ventas() {
        return "operaciones/ventas";
    }

    @GetMapping("/facturas")
    public String facturas() {
        return "operaciones/facturas";
    }

    @GetMapping("/facturas/{id}")
    public String facturaDetalle() {
        return "operaciones/factura-detalle";
    }

    @GetMapping("/usuarios")
    public String usuarios() {
        return "admin/usuarios";
    }
}