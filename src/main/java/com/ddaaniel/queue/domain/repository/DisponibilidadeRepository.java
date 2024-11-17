package com.ddaaniel.queue.domain.repository;

import com.ddaaniel.queue.domain.model.Indisponibilidade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DisponibilidadeRepository extends JpaRepository<Indisponibilidade, Long> {
}
