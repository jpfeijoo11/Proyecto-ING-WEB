package com.proyecto.backend.controllers;

import com.proyecto.backend.models.Usuario;
import com.proyecto.backend.repositories.UsuarioRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")

public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Usuario credenciales) {
        // Buscamos SOLO por username
        java.util.Optional<Usuario> usuarioOp = usuarioRepository.findByUsername(credenciales.getUsername());
        
        // Revisamos si el usuario existe
        if (usuarioOp.isPresent()) {
            Usuario user = usuarioOp.get();
            // Comparamos las contraseñas
            if (user.getPassword().equals(credenciales.getPassword())) {
                return ResponseEntity.ok(user); // Contraseña correcta, login exitoso
            }
        }
        // Si no existe el usuario o la clave está mal
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales incorrectas");
    }

    @PostMapping("/registro")
    public ResponseEntity<?> registrarAdmin(@Valid @RequestBody Usuario usuario, BindingResult result) {
        
        if (result.hasErrors()) {
            Map<String, String> errores = new HashMap<>();
            result.getFieldErrors().forEach(err -> {
                errores.put(err.getField(), err.getDefaultMessage());
            });
            return ResponseEntity.badRequest().body(errores);
        }

        try {
            Usuario nuevoUsuario = usuarioRepository.save(usuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoUsuario);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al guardar: Posible cédula o usuario duplicado.");
        }
    }

    @GetMapping("/listar")
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    /**
     * DELETE /api/admin/{id}
     * Elimina un usuario por ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminarUsuario(@PathVariable Long id) {
        if (!usuarioRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado.");
        }
        usuarioRepository.deleteById(id);
        return ResponseEntity.ok("Usuario eliminado correctamente.");
    }
}
