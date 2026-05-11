package com.proyecto.backend.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.backend.models.Producto;
import com.proyecto.backend.models.Usuario;
import com.proyecto.backend.repositories.ProductoRepository;
import com.proyecto.backend.repositories.UsuarioRepository;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200") 
public class ApiController {

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private ProductoRepository productoRepo;

    
   

    
    @GetMapping("/productos")
    public List<Producto> listar() {
        return productoRepo.findAll();
    }

    @PostMapping("/productos")
    public Producto crear(@RequestBody Producto producto) {
        return productoRepo.save(producto);
    }

    @DeleteMapping("/productos/{id}")
    public void eliminar(@PathVariable Long id) {
        productoRepo.deleteById(id);
    }
}
