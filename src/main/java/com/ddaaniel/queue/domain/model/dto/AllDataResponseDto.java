package com.ddaaniel.queue.domain.model.dto;

import com.ddaaniel.queue.domain.model.Agendamento;
import com.ddaaniel.queue.domain.model.Conta;
import com.ddaaniel.queue.domain.model.Especialista;
import com.ddaaniel.queue.domain.model.Paciente;

import java.util.List;

public record AllDataResponseDto(
        List<Conta> contas,
        List<Especialista> especialistas,
        List<Paciente> pacientes,
        List<Agendamento> agendamentos
) {}
