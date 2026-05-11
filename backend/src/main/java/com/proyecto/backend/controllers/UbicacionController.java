package com.proyecto.backend.controllers;

import com.proyecto.backend.models.*;
import com.proyecto.backend.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ubicaciones")

public class UbicacionController {

    @Autowired
    private PaisRepository paisRepository;

    @Autowired
    private ProvinciaRepository provinciaRepository;

    @Autowired
    private CiudadRepository ciudadRepository;

    @GetMapping("/paises")
    public List<Pais> listarPaises() {
        return paisRepository.findAll();
    }

    @GetMapping("/provincias/{paisId}")
    public List<Provincia> listarProvinciasPorPais(@PathVariable Long paisId) {
        return provinciaRepository.findByPaisId(paisId);
    }

    @GetMapping("/ciudades/{provinciaId}")
    public List<Ciudad> listarCiudadesPorProvincia(@PathVariable Long provinciaId) {
        return ciudadRepository.findByProvinciaId(provinciaId);
    }
}
