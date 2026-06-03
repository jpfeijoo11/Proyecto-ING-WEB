package com.proyecto.backend.controllers;

import com.proyecto.backend.dto.EstadisticasDTO;
import com.proyecto.backend.services.EstadisticasService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/estadisticas")
@CrossOrigin(origins = "http://localhost:4200")
public class EstadisticasController {

    @Autowired
    private EstadisticasService estadisticasService;

    
    @GetMapping
    public EstadisticasDTO obtenerEstadisticas() {
        return estadisticasService.obtenerEstadisticas();
    }
}
