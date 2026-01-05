package com.hconline.accessmanager.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recurso-protegido")
public class ProtectedController {

    @GetMapping
    public ResponseEntity<String> getRecursoProtegido() {
        return ResponseEntity.ok("Acceso autorizado al recurso protegido");
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> getRecursoAdmin() {
        return ResponseEntity.ok("Acceso autorizado al recurso de administrador");
    }
}
