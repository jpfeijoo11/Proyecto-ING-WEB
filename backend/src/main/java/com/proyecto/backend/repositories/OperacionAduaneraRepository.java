package com.proyecto.backend.repositories;

import com.proyecto.backend.models.OperacionAduanera;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface OperacionAduaneraRepository extends JpaRepository<OperacionAduanera, Long> {
    
    
    Optional<OperacionAduanera> findByNumeroTracking(String numeroTracking);
}