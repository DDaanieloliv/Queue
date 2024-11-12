package com.ddaaniel.queue.controller;

import com.ddaaniel.queue.domain.model.*;
import com.ddaaniel.queue.domain.model.dto.EspecialistaRecordDtoResponce;
import com.ddaaniel.queue.domain.model.dto.RecordDtoAgendamento;
import com.ddaaniel.queue.domain.repository.ContaRepository;
import com.ddaaniel.queue.domain.repository.EspecialistaRepository;
import com.ddaaniel.queue.domain.repository.PacienteRepository;
import com.ddaaniel.queue.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@RequestMapping("/fila")
public class QueueController {

    @Autowired
    private FilaDePacientesService filaDePacientesService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private Executor asyncExecutor;

    @Autowired
    private EspecialistaRepository especialistaRepository;

    @Autowired
    private AgendamentoService agendamentoService;

    @Autowired
    private EspecialistaService especialistaService;

    @Autowired
    private ContaService contaService;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private CadastramentoService cadastramentoService;

    @PostMapping("/agendar")
    public CompletableFuture<ResponseEntity<String>>
    agendarPaciente(@RequestBody RecordDtoAgendamento record) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                /*
                var paciente = new Paciente(
                        record.nomeCompleto(),
                        record.dataNascimento(),
                        record.sexo(),
                        record.cpf(),
                        record.email(),
                        record.telefone()
                        );

                var agendamento = new Agendamento(
                        paciente,
                        record.especialista(), // Setando na entidade agendamento o id do especialista associado a esse agendamento.
                        record.dataDisponivel(),
                        record.horaDisponivel()
                );

                */

                Agendamento agendamento = agendamentoService.criarAgendamento(record);

                // Converte o DTO para uma entidade de Agendamento
                //Agendamento agendamento = record.fromDtoToEntitys();
                // O método fromDtoToEntitys não precisa de parâmetros porque ele já tem
                //  acesso aos dados do record RecordDtoAgendamento por ser um método de
                //   instância.

                // Adiciona o Agendamento à fila, que salva o Agendamento e o Paciente (por cascata)
                filaDePacientesService.adicionarAgendamento(agendamento);

                // Recupera o paciente associado ao agendamento para obter o ID
                var paciente = agendamento.getPaciente();

                // Pega email e codigoCondigo passodo e cria uma conta com a Role PACIENTE
                contaService.criaContaPaciente(paciente);

                // Recupera o código e o e-mail do paciente
                String codigoCodigo = filaDePacientesService.obterCodigoPaciente(paciente.getId_paciente());
                String emailPaciente = filaDePacientesService.obterEmailPaciente(paciente.getId_paciente());

                // Envia o e-mail com o código
                if (codigoCodigo != null && emailPaciente != null) {
                    emailService.enviarEmail(emailPaciente, codigoCodigo);
                }

                return ResponseEntity.ok("Paciente adicionado à fila com sucesso!");

            } catch (Exception e) {
                return ResponseEntity.status(500).body("Erro ao adicionar paciente: " + e.getMessage());
            }
        }, asyncExecutor);
    }


    @GetMapping("/findEspecialista")
    public CompletableFuture<List<EspecialistaRecordDtoResponce>> getAllEspecialista(
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {

        return CompletableFuture.supplyAsync(() -> {

            Page<Especialista> especialistas = especialistaService.findAllEspecialistas(page, pageSize);

            return especialistas
                    .stream()
                    .map(EspecialistaRecordDtoResponce::new)
                    .collect(Collectors.toList());
        }, asyncExecutor);
    }


    // Fazer GET retornar o usuario que possui conta no software.
    // Tal GET deve retornar a Role do usuario.
    @GetMapping("/login")
    public CompletableFuture<ResponseEntity<?>> getRoleByLogin(
            @RequestParam String email,
            @RequestParam String password ) {

        return CompletableFuture.supplyAsync( () -> {

            var pacienteWithCodigoCodigo = pacienteRepository
                    .findByCodigoCodigo(password);
            var emailOf = pacienteRepository
                    .findByEmail(email);

            Optional<Paciente> paciente = pacienteRepository
                    .findByCodigoCodigo(password);

            if (!pacienteWithCodigoCodigo.isEmpty() && !emailOf.isEmpty()) {

                Paciente objetoPaciente = paciente.get();
                return ResponseEntity.ok(objetoPaciente.getRole());
            } else {
                // Retorna mensagem de erro e status 401 Unauthorized
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Email ou senha incorretos.");
            }

        }, asyncExecutor);
    }


    @PostMapping("/cadastroFuncionario")
    public CompletableFuture<ResponseEntity<?>> cadastrarFuncionario(@RequestBody Cadastramento cadastramento){

        return CompletableFuture.supplyAsync( () -> {

            try {
                cadastramentoService.cadastrando(cadastramento);

                // Recupera o código e o e-mail do paciente
                String codigoCodigo = cadastramentoService.obterCodigoCadastramento(cadastramento.getId_cadastramento());
                String emailCadastramento = cadastramentoService.obterEmailCadastramento(cadastramento.getId_cadastramento());

                // Envia o e-mail com o código
                if (codigoCodigo != null && emailCadastramento != null) {
                    emailService.enviarEmail(emailCadastramento, codigoCodigo);
                }

                return ResponseEntity.status(HttpStatus.CREATED)
                        .body("Cadastro efetuado com Sucesso!");

            } catch (Exception e){
                return ResponseEntity.status(500).body("Erro ao realizar cadastro: " + e.getMessage());

            }

        }, asyncExecutor);

    }

}
