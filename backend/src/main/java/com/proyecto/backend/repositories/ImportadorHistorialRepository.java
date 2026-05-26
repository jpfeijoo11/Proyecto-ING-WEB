package com.proyecto.backend.repositories;

import com.proyecto.backend.models.ImportadorHistorial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImportadorHistorialRepository extends JpaRepository<ImportadorHistorial, Long> {
    java.util.Optional<ImportadorHistorial> findByRucEmpresa(String rucEmpresa);
}
