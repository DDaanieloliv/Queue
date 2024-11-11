package com.ddaaniel.queue.domain.repository;

import com.ddaaniel.queue.domain.model.Especialista;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EspecialistaRepository extends JpaRepository<Especialista, Long> {
    //Page<Especialista> findAll(PageRequest pageRequest);
}
