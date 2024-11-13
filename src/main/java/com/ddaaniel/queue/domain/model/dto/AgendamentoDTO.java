package com.ddaaniel.queue.domain.model.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class AgendamentoDTO {
    private Long id;
    private String nomePaciente;
    private String nomeEspecialista;
    private LocalDate dataAgendamento;
    private LocalTime horaAgendamento;
    private String status;

    // Construtor
    public AgendamentoDTO(Long id, String nomePaciente, String nomeEspecialista, LocalDate dataAgendamento, LocalTime horaAgendamento, String status) {
        this.id = id;
        this.nomePaciente = nomePaciente;
        this.nomeEspecialista = nomeEspecialista;
        this.dataAgendamento = dataAgendamento;
        this.horaAgendamento = horaAgendamento;
        this.status = status;
    }

    // Getters e Setters
    // ...
}
