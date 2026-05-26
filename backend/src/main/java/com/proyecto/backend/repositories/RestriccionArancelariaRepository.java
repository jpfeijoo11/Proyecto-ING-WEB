package com.proyecto.backend.repositories;

import com.proyecto.backend.models.RestriccionArancelaria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RestriccionArancelariaRepository extends JpaRepository<RestriccionArancelaria, Long> {
    Optional<RestriccionArancelaria> findByCodigoArancelarioIgnoreCase(String codigoArancelario);
}
