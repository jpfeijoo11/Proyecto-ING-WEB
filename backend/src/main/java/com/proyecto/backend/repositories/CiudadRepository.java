package com.proyecto.backend.repositories;

import com.proyecto.backend.models.Ciudad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CiudadRepository extends JpaRepository<Ciudad, Long> {
    List<Ciudad> findByProvinciaId(Long provinciaId);
}
