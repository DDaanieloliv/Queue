package com.ddaaniel.queue.controller;

import com.ddaaniel.queue.domain.model.Agendamento;
import com.ddaaniel.queue.domain.model.Especialista;
import com.ddaaniel.queue.domain.model.Paciente;
import com.ddaaniel.queue.domain.model.enuns.Prioridade;
import com.ddaaniel.queue.domain.model.enuns.StatusAgendamento;
import com.ddaaniel.queue.domain.repository.AgendamentoRepository;
import com.ddaaniel.queue.domain.repository.EspecialistaRepository;
import com.ddaaniel.queue.domain.repository.PacienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Controller
public class FilaWebSocketController {

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private EspecialistaRepository especialistaRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    private int prioridadeContador = 0; // Contador para alternância entre prioridades

    @MessageMapping("/chamarPaciente") // Rota que os clientes usarão para enviar mensagens
    @SendTo("/topic/pacienteChamado") // Tópico para onde as respostas serão enviadas
    public CompletableFuture<Map<String, Object>> chamarPacientePorEspecialista(Long idEspecialista) {
        return CompletableFuture.supplyAsync(() -> {
            List<Agendamento> agendamentos = agendamentoRepository
                    .findAllByEspecialista_IdAndStatusAndPaciente_PresencaConfirmado(
                            idEspecialista, StatusAgendamento.EM_ESPERA, true);

            if (agendamentos.isEmpty()) {
                Map<String, Object> resposta = new HashMap<>();
                resposta.put("mensagem", "Nenhum paciente em espera para o especialista com ID " + idEspecialista);
                return resposta;
            }

            List<Agendamento> comPrioridade = agendamentos.stream()
                    .filter(a -> a.getPaciente().getPrioridade() == Prioridade.PESSOA_COM_ALGUMA_PRIORIDADE)
                    .sorted(Comparator.comparing(Agendamento::getDataHoraChegada))
                    .toList();

            List<Agendamento> semPrioridade = agendamentos.stream()
                    .filter(a -> a.getPaciente().getPrioridade() == Prioridade.NENHUM)
                    .sorted(Comparator.comparing(Agendamento::getDataHoraChegada))
                    .toList();

            Agendamento proximoAgendamento = null;

            if (!comPrioridade.isEmpty() && (prioridadeContador < 2 || semPrioridade.isEmpty())) {
                proximoAgendamento = comPrioridade.get(0);
                prioridadeContador++;
            } else if (!semPrioridade.isEmpty()) {
                proximoAgendamento = semPrioridade.get(0);
                prioridadeContador = 0;
            }

            if (proximoAgendamento != null) {
                proximoAgendamento.setStatus(StatusAgendamento.EM_ATENDIMENTO);
                agendamentoRepository.save(proximoAgendamento);

                Paciente paciente = proximoAgendamento.getPaciente();
                Map<String, Object> pacienteInfo = new HashMap<>();
                pacienteInfo.put("id", paciente.getId_paciente());
                pacienteInfo.put("nome", paciente.getNomeCompleto());
                pacienteInfo.put("sexo", paciente.getSexo());
                pacienteInfo.put("prioridade", paciente.getPrioridade().name());
                pacienteInfo.put("horaChegada", paciente.getDataHoraChegada());
                pacienteInfo.put("status", proximoAgendamento.getStatus());
                return pacienteInfo;
            } else {
                Map<String, Object> resposta = new HashMap<>();
                resposta.put("mensagem", "Nenhum paciente disponível para atendimento no momento.");
                return resposta;
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


    @MessageMapping("/primeirosPacientesPorEspecialistas")
    @SendTo("/topic/primeirosPacientesPorEspecialistaAtualizados")
    public List<Map<String, Object>> getPrimeirosPacientes() {
        return getPrimeirosPacientesPorEspecialistas();
    }

    private List<Map<String, Object>> getPrimeirosPacientesPorEspecialistas() {
        List<Map<String, Object>> especialistasPacientesInfo = new ArrayList<>();

        // Busca todos os especialistas cadastrados no sistema
        List<Especialista> especialistas = especialistaRepository.findAll();

        for (Especialista especialista : especialistas) {
            Map<String, Object> especialistaInfo = new HashMap<>();
            Map<String, Object> pacienteInfo = new HashMap<>();

            Long especialistaId = especialista.getId();

            // Buscar agendamentos com status EM_ESPERA para o especialista
            List<Agendamento> agendamentosEmEspera = agendamentoRepository
                    .findAllByEspecialista_IdAndStatusAndPaciente_PresencaConfirmado(
                            especialistaId, StatusAgendamento.EM_ESPERA, true);

            Optional<Agendamento> primeiroEmEspera = agendamentosEmEspera.stream()
                    .sorted(Comparator.comparingInt((Agendamento a) ->
                                    a.getPaciente().getPrioridade().getPrioridade())
                            .thenComparing(Agendamento::getDataAgendamento))
                    .findFirst();

            // Buscar agendamentos com status EM_ATENDIMENTO para o especialista
            List<Agendamento> agendamentosEmAtendimento = agendamentoRepository
                    .findAllByEspecialista_IdAndStatusAndPaciente_PresencaConfirmado(
                            especialistaId, StatusAgendamento.EM_ATENDIMENTO, true);

            Optional<Agendamento> primeiroEmAtendimento = agendamentosEmAtendimento.stream().findFirst();

            // Montar informações do paciente em espera
            pacienteInfo.put("PacienteEmEspera", primeiroEmEspera.map(agendamento -> {
                Map<String, String> esperaInfo = new HashMap<>();
                esperaInfo.put("Nome", agendamento.getPaciente().getNomeCompleto());
                esperaInfo.put("Status", agendamento.getStatus().name());
                return esperaInfo;
            }).orElse(null));

            // Montar informações do paciente em atendimento
            pacienteInfo.put("PacienteEmAtendimento", primeiroEmAtendimento.map(agendamento -> {
                Map<String, String> atendimentoInfo = new HashMap<>();
                atendimentoInfo.put("Nome", agendamento.getPaciente().getNomeCompleto());
                atendimentoInfo.put("Status", agendamento.getStatus().name());
                return atendimentoInfo;
            }).orElse(null));

            // Adicionar as informações do especialista ao mapa
            especialistaInfo.put("EspecialistaId", especialistaId);
            especialistaInfo.put("Nome", especialista.getNome());
            especialistaInfo.put("TipoEspecialista", especialista.getTipoEspecialista().name());
            especialistaInfo.put("Pacientes", pacienteInfo);

            especialistasPacientesInfo.add(especialistaInfo);
        }

        return especialistasPacientesInfo;
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
