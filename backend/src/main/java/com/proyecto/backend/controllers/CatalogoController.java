package com.proyecto.backend.controllers;

import com.proyecto.backend.models.CatalogoRiesgoPais;
import com.proyecto.backend.models.ImportadorHistorial;
import com.proyecto.backend.models.RestriccionArancelaria;
import com.proyecto.backend.repositories.CatalogoRiesgoPaisRepository;
import com.proyecto.backend.repositories.ImportadorHistorialRepository;
import com.proyecto.backend.repositories.RestriccionArancelariaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Expone los catálogos del motor de riesgo para que Angular
 * pueda poblar los selects del formulario de nueva operación.
 */
@RestController
@RequestMapping("/api/catalogos")
@CrossOrigin(origins = "http://localhost:4200")
public class CatalogoController {

    @Autowired
    private CatalogoRiesgoPaisRepository catalogoPaisRepo;

    @Autowired
    private ImportadorHistorialRepository importadorRepo;

    @Autowired
    private RestriccionArancelariaRepository restriccionRepo;

    // ── Catálogo de Riesgo por País/Puerto ───────────────────────────────────
    @GetMapping("/paises-riesgo")
    public List<CatalogoRiesgoPais> getPaisesRiesgo() {
        return catalogoPaisRepo.findAll();
    }

    @PostMapping("/paises-riesgo")
    public CatalogoRiesgoPais crearPaisRiesgo(@RequestBody CatalogoRiesgoPais pais) {
        return catalogoPaisRepo.save(pais);
    }

    // ── Importadores con Historial ───────────────────────────────────────────
    @GetMapping("/importadores")
    public List<ImportadorHistorial> getImportadores() {
        return importadorRepo.findAll();
    }

    @PostMapping("/importadores")
    public ImportadorHistorial crearImportador(@RequestBody ImportadorHistorial importador) {
        return importadorRepo.save(importador);
    }

    // ── Restricciones Arancelarias ───────────────────────────────────────────
    @GetMapping("/arancelarios")
    public List<RestriccionArancelaria> getRestricciones() {
        return restriccionRepo.findAll();
    }

    @PostMapping("/arancelarios")
    public RestriccionArancelaria crearRestriccion(@RequestBody RestriccionArancelaria restriccion) {
        return restriccionRepo.save(restriccion);
    }
}
