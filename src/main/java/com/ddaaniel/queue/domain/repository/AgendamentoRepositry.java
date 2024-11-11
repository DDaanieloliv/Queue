package com.ddaaniel.queue.domain.repository;


import com.ddaaniel.queue.domain.model.Agendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AgendamentoRepositry extends JpaRepository<Agendamento, Long> {



}
