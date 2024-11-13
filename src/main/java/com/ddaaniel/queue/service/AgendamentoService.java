package com.ddaaniel.queue.service;

import com.ddaaniel.queue.domain.model.Agendamento;
import com.ddaaniel.queue.domain.model.Especialista;
import com.ddaaniel.queue.domain.model.Paciente;
import com.ddaaniel.queue.domain.model.dto.AgendamentoDTO;
import com.ddaaniel.queue.domain.model.dto.RecordDtoAgendamento;
import com.ddaaniel.queue.domain.repository.AgendamentoRepositry;
import com.ddaaniel.queue.domain.repository.EspecialistaRepository;
import com.ddaaniel.queue.domain.repository.PacienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AgendamentoService {

    @Autowired
    private EspecialistaRepository especialistaRepository;

    @Autowired
    private EspecialistaService especialistaService;

    @Autowired
    private AgendamentoRepositry agendamentoRepositry;

    @Autowired
    private PacienteRepository pacienteRepository;

    public Agendamento criarAgendamento(RecordDtoAgendamento recordDto) {
        // Busca o especialista pelo ID

        Especialista especialista = especialistaService.findByIdEspecialista(recordDto.especialista());

        //Especialista especialista = especialistaRepository.findById(recordDto.especialista())
        //        .orElseThrow(() -> new RuntimeException("Especialista não encontrado para o ID: " + recordDto.especialista()));

        // Cria e retorna o agendamento
        return recordDto.fromDtoToEntitys(especialista);
    }


    public List<AgendamentoDTO> getAllAgendamentosByCodigoCodigo(String codigoCodigo) {
        Optional<Paciente> pacienteOpt = pacienteRepository.findByCodigoCodigo(codigoCodigo);

        if (pacienteOpt.isEmpty()) {
            throw new RuntimeException("Paciente com o código fornecido não encontrado.");
        }

        Paciente paciente = pacienteOpt.get();
        LocalDate today = LocalDate.now();

        /*
        // Filtra os agendamentos pela data
        List<Agendamento> agendamentosDeHoje = paciente.getAgendamentos().stream()
                .filter(agendamento -> agendamento.getDataAgendamento().equals(today))
                .collect(Collectors.toList());


        return agendamentosDeHoje;
        */

        return paciente.getAgendamentos().stream()
                .filter(agendamento -> agendamento.getDataAgendamento().equals(today))
                .map(agendamento -> new AgendamentoDTO(
                        agendamento.getId(),
                        agendamento.getPaciente().getNomeCompleto(),
                        agendamento.getEspecialista().getNome(),
                        agendamento.getDataAgendamento(),
                        agendamento.getHoraAgendamento(),
                        agendamento.getStatus().name()
                ))
                .collect(Collectors.toList());
    }

}
