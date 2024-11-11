package com.ddaaniel.queue.domain.model;


import com.ddaaniel.queue.domain.model.enuns.StatusAgendamento;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@NoArgsConstructor
@Entity
@Data
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade = CascadeType.PERSIST) // Ou CascadeType.ALL, se quiser incluir todas as operações em cascata
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne
    @JoinColumn(name = "especialista_id", nullable = false)
    private Especialista especialista;

    private LocalDate dataAgendamento;

    private LocalTime horaAgendamento;

    @Enumerated(EnumType.STRING)
    private StatusAgendamento status = StatusAgendamento.AGUARDANDO_CONFIRMACAO;


    public Agendamento(Paciente paciente, Especialista especialista, LocalDate dataAgendamento, LocalTime horaAgendamento) {
        this.paciente = paciente;
        this.especialista = especialista;
        this.dataAgendamento = dataAgendamento;
        this.horaAgendamento = horaAgendamento;
        this.status = StatusAgendamento.AGUARDANDO_CONFIRMACAO; // Define o status padrão
    }

}

