package com.proyecto.backend.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200") 
public class ApiController {

    /**
     * Endpoint de prueba para verificar que el backend está corriendo correctamente.
     * Útil para el video de Loom o para diagnósticos rápidos.
     */
    @GetMapping("/status")
    public Map<String, String> getStatus() {
        Map<String, String> status = new HashMap<>();
        status.put("sistema", "CustomGate 360");
        status.put("estado", "Operativo");
        status.put("version", "1.0.0");
        return status;
    }
}