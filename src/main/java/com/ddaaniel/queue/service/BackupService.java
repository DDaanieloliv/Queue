package com.ddaaniel.queue.service;

import com.ddaaniel.queue.domain.model.Agendamento;
import com.ddaaniel.queue.domain.model.Conta;
import com.ddaaniel.queue.domain.model.Especialista;
import com.ddaaniel.queue.domain.model.Paciente;
import com.ddaaniel.queue.domain.model.dto.AllDataResponseDto;
import com.ddaaniel.queue.domain.repository.AgendamentoRepository;
import com.ddaaniel.queue.domain.repository.ContaRepository;
import com.ddaaniel.queue.domain.repository.EspecialistaRepository;
import com.ddaaniel.queue.domain.repository.PacienteRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BackupService {

    @Autowired
    private  ContaRepository contaRepository;

    @Autowired
    private EspecialistaRepository especialistaRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private AgendamentoRepository agendamentoRepository;


    public AllDataResponseDto getAllData() {
        // Buscar os dados de todas as entidades
        List<Conta> contas = contaRepository.findAll();
        var especialistas = especialistaRepository.findAll();
        var pacientes = pacienteRepository.findAll();
        var agendamentos = agendamentoRepository.findAll();

        // Retornar todos os dados agrupados em um DTO
        return new AllDataResponseDto(contas, especialistas, pacientes, agendamentos);
    }
}
