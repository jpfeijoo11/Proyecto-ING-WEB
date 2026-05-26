package com.proyecto.backend.repositories;

import com.proyecto.backend.models.CatalogoRiesgoPais;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CatalogoRiesgoPaisRepository extends JpaRepository<CatalogoRiesgoPais, Long> {
    Optional<CatalogoRiesgoPais> findByNombrePuertoOPaisIgnoreCase(String nombrePuertoOPais);
}
