package com.ddaaniel.queue.domain.repository;

import com.ddaaniel.queue.domain.model.Cadastramento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CadastramentoRepository extends JpaRepository<Cadastramento,Long> {
    boolean existsByCodigoCodigo(String codigo);

    Optional<Cadastramento> findByCodigoCodigo(String password);
}
