package com.ddaaniel.queue.controller;

import com.ddaaniel.queue.domain.model.Agendamento;
import com.ddaaniel.queue.domain.model.Paciente;
import com.ddaaniel.queue.domain.model.enuns.StatusAgendamento;
import com.ddaaniel.queue.domain.repository.AgendamentoRepository;
import com.ddaaniel.queue.domain.repository.PacienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@RestController
public class FilaWebSocketController {

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    // Chamar o próximo paciente na fila para o especialista
    @MessageMapping("/chamarPaciente")
    @SendTo("/topic/pacienteChamada")
    public Map<String, Object> chamarPaciente(Long idEspecialista) {
        return chamarPrimeiroPacientePorEspecialista(idEspecialista).join();
    }

    private CompletableFuture<Map<String, Object>> chamarPrimeiroPacientePorEspecialista(Long idEspecialista) {
        return CompletableFuture.supplyAsync(() -> {
            List<Agendamento> agendamentos = agendamentoRepository
                    .findAllByEspecialista_IdAndStatusAndPaciente_PresencaConfirmado(
                            idEspecialista, StatusAgendamento.EM_ESPERA, true);

            Optional<Agendamento> primeiroAgendamento = agendamentos.stream()
                    .sorted(Comparator
                            .comparingInt((Agendamento a) -> a.getPaciente().getPrioridade().getPrioridade())
                            .thenComparing(Agendamento::getDataHoraChegada))
                    .findFirst();

            if (primeiroAgendamento.isPresent()) {
                Agendamento agendamento = primeiroAgendamento.get();
                agendamento.setStatus(StatusAgendamento.EM_ATENDIMENTO);
                agendamentoRepository.save(agendamento);

                Paciente paciente = agendamento.getPaciente();
                Map<String, Object> pacienteInfo = new HashMap<>();
                pacienteInfo.put("id", paciente.getId_paciente());
                pacienteInfo.put("nome", paciente.getNomeCompleto());
                pacienteInfo.put("sexo", paciente.getSexo());
                pacienteInfo.put("prioridade", paciente.getPrioridade().getPrioridade());
                pacienteInfo.put("horaChegada", paciente.getDataHoraChegada());
                pacienteInfo.put("status", agendamento.getStatus());

                return pacienteInfo;
            } else {
                return Map.of("status", "NOT_FOUND", "message",
                        "Nenhum paciente em espera para o especialista com ID " + idEspecialista);
            }
        });
    }

    // Obter o primeiro paciente em espera e em atendimento
    @MessageMapping("/primeiroPacienteEspecialista")
    @SendTo("/topic/primeiroPacienteAtualizado")
    public Map<String, Object> getPrimeiroPacientePorEspecialista(Long especialistaId) {
        return getPrimeiroPacientePorEspecialistaId(especialistaId);
    }

    private Map<String, Object> getPrimeiroPacientePorEspecialistaId(Long especialistaId) {
        Map<String, Object> pacienteInfo = new HashMap<>();

        List<Agendamento> agendamentosEmEspera = agendamentoRepository
                .findAllByEspecialista_IdAndStatusAndPaciente_PresencaConfirmado(
                        especialistaId, StatusAgendamento.EM_ESPERA, true);

        Optional<Agendamento> primeiroEmEspera = agendamentosEmEspera.stream()
                .sorted(Comparator.comparingInt((Agendamento a) ->
                                a.getPaciente().getPrioridade().getPrioridade())
                        .thenComparing(Agendamento::getDataAgendamento))
                .findFirst();

        List<Agendamento> agendamentosEmAtendimento = agendamentoRepository
                .findAllByEspecialista_IdAndStatusAndPaciente_PresencaConfirmado(
                        especialistaId, StatusAgendamento.EM_ATENDIMENTO, true);

        Optional<Agendamento> primeiroEmAtendimento = agendamentosEmAtendimento.stream().findFirst();

        pacienteInfo.put("PacienteEmEspera", primeiroEmEspera.map(agendamento -> {
            Map<String, String> esperaInfo = new HashMap<>();
            esperaInfo.put("Nome", agendamento.getPaciente().getNomeCompleto());
            esperaInfo.put("Status", agendamento.getStatus().name());
            return esperaInfo;
        }).orElse(null));

        pacienteInfo.put("PacienteEmAtendimento", primeiroEmAtendimento.map(agendamento -> {
            Map<String, String> atendimentoInfo = new HashMap<>();
            atendimentoInfo.put("Nome", agendamento.getPaciente().getNomeCompleto());
            atendimentoInfo.put("Status", agendamento.getStatus().name());
            return atendimentoInfo;
        }).orElse(null));

        return pacienteInfo;
    }

    // Contar pacientes em espera
    @MessageMapping("/contagemEspecialista")
    @SendTo("/topic/contagemAtualizada")
    public Map<String, Integer> getContagemPacientesPorEspecialista(Long especialistaId) {
        return contarPacientesPorEspecialistaId(especialistaId);
    }

    private Map<String, Integer> contarPacientesPorEspecialistaId(Long especialistaId) {
        int contagem = agendamentoRepository.countByEspecialista_IdAndStatus(
                especialistaId, StatusAgendamento.EM_ESPERA);

        return Map.of("QuantidadePacientesEmEspera", contagem);
    }

    // Confirmar presença de um paciente
    @MessageMapping("/marcarPresenca")
    @SendTo("/topic/presencaConfirmada")
    public Map<String, String> marcarPresenca(Map<String, Object> payload) {
        String codigoCodigo = (String) payload.get("codigoCodigo");
        Long idAgendamento = Long.valueOf(payload.get("idAgendamento").toString());

        return CompletableFuture.supplyAsync(() -> {
            var pacienteOpt = pacienteRepository.findByCodigoCodigo(codigoCodigo);

            if (pacienteOpt.isPresent()) {
                Paciente objPaciente = pacienteOpt.get();

                boolean jaEmEspera = agendamentoRepository.existsByPacienteAndStatus(objPaciente, StatusAgendamento.EM_ESPERA);
                if (jaEmEspera) {
                    return Map.of("status", "CONFLICT", "message", "Paciente já na fila.");
                }

                Optional<Agendamento> agendamentoOpt = agendamentoRepository.findById(idAgendamento);
                if (agendamentoOpt.isPresent()) {
                    Agendamento objAgendamento = agendamentoOpt.get();
                    objAgendamento.setStatus(StatusAgendamento.EM_ESPERA);
                    objAgendamento.setDataHoraChegada(LocalDateTime.now());
                    agendamentoRepository.save(objAgendamento);

                    objPaciente.setPresencaConfirmado(true);
                    pacienteRepository.save(objPaciente);

                    return Map.of("status", "OK", "message", "Presença confirmada.");
                } else {
                    return Map.of("status", "NOT_FOUND", "message", "Agendamento não encontrado.");
                }
            } else {
                return Map.of("status", "NOT_FOUND", "message", "Paciente não encontrado.");
            }
        }).join();
    }
}
