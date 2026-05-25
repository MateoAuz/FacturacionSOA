package com.empresa.sistema.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

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
    public String inventario() {
        return "gestion/inventario";
    }

    @GetMapping("/ventas")
    public String ventas() {
        return "operaciones/ventas";
    }

    @GetMapping("/facturas")
    public String facturas() {
        return "operaciones/facturas";
    }

    @GetMapping("/usuarios")
    public String usuarios() {
        return "admin/usuarios";
    }
}