package com.ddaaniel.queue.domain.repository;

import com.ddaaniel.queue.domain.model.Conta;
import com.ddaaniel.queue.domain.model.Especialista;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EspecialistaRepository extends JpaRepository<Especialista, Long> {
    Optional<Especialista> findByConta(Conta conta);
    // Optional<Especialista> findByConta(Conta conta);

    //@Query("SELECT e FROM Especialista e LEFT JOIN FETCH e.indisponibilidades")
    //Page<Especialista> findAll(PageRequest pageRequest);
}
