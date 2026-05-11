package com.proyecto.backend.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "pais")
@Data
public class Pais {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
}