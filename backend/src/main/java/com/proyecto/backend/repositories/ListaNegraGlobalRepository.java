package com.proyecto.backend.repositories;

import com.proyecto.backend.models.ListaNegraGlobal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ListaNegraGlobalRepository extends JpaRepository<ListaNegraGlobal, Long> {}
