package com.ddaaniel.queue.domain.repository;


import com.ddaaniel.queue.domain.model.Agendamento;
import com.ddaaniel.queue.domain.model.Paciente;
import com.ddaaniel.queue.domain.model.enuns.StatusAgendamento;
import com.ddaaniel.queue.domain.model.enuns.TipoEspecialista;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AgendamentoRepositry extends JpaRepository<Agendamento, Long> {

    int countByEspecialista_TipoEspecialistaAndStatus(TipoEspecialista tipoEspecialista, StatusAgendamento status);


    List<Agendamento> findAllByEspecialista_TipoEspecialistaAndStatusAndPaciente_PresencaConfirmado(TipoEspecialista tipoEspecialista, StatusAgendamento statusAgendamento, boolean b);

    Optional<Agendamento> findByPacienteAndStatus(Paciente paciente, StatusAgendamento statusAgendamento);
}
