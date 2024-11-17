package com.ddaaniel.queue.service;

import com.ddaaniel.queue.domain.model.Agendamento;
import com.ddaaniel.queue.domain.model.Conta;
import com.ddaaniel.queue.domain.model.Especialista;
import com.ddaaniel.queue.domain.model.Paciente;
import com.ddaaniel.queue.domain.model.dto.AgendamentoDTO;
import com.ddaaniel.queue.domain.model.dto.RecordDtoAgendamento;
import com.ddaaniel.queue.domain.model.enuns.Role;
import com.ddaaniel.queue.domain.repository.AgendamentoRepository;
import com.ddaaniel.queue.domain.repository.ContaRepository;
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
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ContaRepository contaRepository;


    public void adicionarAgendamento(Agendamento agendamento) {
        Paciente paciente = agendamento.getPaciente();
        Optional<Paciente> pacienteExistente = pacienteRepository.findByCpf(paciente.getCpf());

        if (pacienteExistente.isPresent()) {
            // Paciente já existe: Atualiza as informações do paciente
            Paciente pacienteAtualizado = pacienteExistente.get();
            pacienteAtualizado.setNomeCompleto(paciente.getNomeCompleto());
            pacienteAtualizado.setEmail(paciente.getEmail());
            pacienteAtualizado.setTelefone(paciente.getTelefone());
            pacienteAtualizado.setDataNascimento(paciente.getDataNascimento());

            agendamento.setPaciente(pacienteAtualizado);

            // Envia o código existente por e-mail
            emailService.enviarEmail(pacienteAtualizado.getEmail(), pacienteAtualizado.getCodigoCodigo());
        } else {
            // Paciente novo: Cria paciente e código
            paciente.gerarCodigoCodigo();

            // Cria uma conta para o paciente
            Conta conta = new Conta(paciente.getCpf(), paciente.getCodigoCodigo(), Role.ESPECIALISTA);
            conta.setRoleEnum(Role.PACIENTE);

            // Salva a conta no banco de dados
            contaRepository.save(conta);

            // Salva o novo paciente no banco de dados
            pacienteRepository.save(paciente);

            // Define o paciente no agendamento
            agendamento.setPaciente(paciente);

            // Envia o código gerado por e-mail
            emailService.enviarEmail(paciente.getEmail(), paciente.getCodigoCodigo());
        }

        // Salva o agendamento
        agendamentoRepository.save(agendamento);
    }




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
