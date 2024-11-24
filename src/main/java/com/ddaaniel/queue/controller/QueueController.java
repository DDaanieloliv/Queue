package com.ddaaniel.queue.controller;

import com.ddaaniel.queue.domain.model.*;
import com.ddaaniel.queue.domain.model.dto.AgendamentoDTO;
import com.ddaaniel.queue.domain.model.dto.EspecialistaRecordDtoResponce;
import com.ddaaniel.queue.domain.model.enuns.Role;
import com.ddaaniel.queue.domain.model.enuns.StatusAgendamento;
import com.ddaaniel.queue.domain.model.enuns.TipoEspecialista;
import com.ddaaniel.queue.domain.repository.*;
import com.ddaaniel.queue.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@RequestMapping("/fila")
@Tag(name = "Fila")
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
    private CadastramentoRepository cadastramentoRepository;

    @Autowired
    private CadastramentoService cadastramentoService;

    @Autowired
    private AgendamentoRepository agendamentoRepository;


    @PostMapping("/criarEspecialista")
    @Operation(summary = "Create new Especialista / Médico.")
    public CompletableFuture<ResponseEntity<?>> criarEspecialista(@RequestBody Especialista especialistaRequest) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Chama o serviço para criar o especialista
                especialistaService.criarEspecialista(especialistaRequest);

                return ResponseEntity.status(HttpStatus.CREATED)
                        .body("Especialista criado com sucesso! Verifique o e-mail para as credenciais de acesso.");
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Erro ao criar especialista: " + e.getMessage());
            }
        }, asyncExecutor);
    }

    @PostMapping("/agendar")
    @Operation(summary = "Creating scheduling and registering patient.")
    public ResponseEntity<?> adicionarAgendamento(@RequestBody Agendamento agendamento) {
        try {
            // Chama o serviço para processar o agendamento
            agendamentoService.adicionarAgendamento(agendamento);
            return ResponseEntity.status(HttpStatus.CREATED).body("Agendamento feito com sucesso.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }




    @GetMapping("/findEspecialista")
    @Operation(summary = "Seeking all registered specialists.")
    public CompletableFuture<List<EspecialistaRecordDtoResponce>> getAllEspecialista(
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {

        return CompletableFuture.supplyAsync(() -> {
            Page<Especialista> especialistas = especialistaService.findAllEspecialistas(page, pageSize);

            return especialistas.stream()
                    .map(EspecialistaRecordDtoResponce::new)
                    .collect(Collectors.toList());
        }, asyncExecutor);
    }





    @GetMapping("/pegarAgendamentos")
    @Operation(summary = "Taking all the scheduled schedules for today.")
    public List<AgendamentoDTO> getAgendaamentosByCodigoCodigo(@RequestParam String codigoCodigo) {

        return agendamentoService.getAllAgendamentosByCodigoCodigo(codigoCodigo);

    }



    @GetMapping("/login")
    @Operation(summary = "Seeking authorization according to credentials.")
    public CompletableFuture<ResponseEntity<?>> getRoleByLogin(
            @RequestParam String emailOrCpf,
            @RequestParam String password) {

        return CompletableFuture.supplyAsync(() -> {

            Optional<Paciente> pacienteOpt = pacienteRepository.findByCodigoCodigo(password);
            Optional<Cadastramento> cadastramentoOpt = cadastramentoRepository.findByCodigoCodigo(password);
            Optional<Conta> contaOpt = contaRepository.findByPassword(password);

            if (pacienteOpt.isPresent() && emailOrCpf.equals(pacienteOpt.get().getEmail())) {
                Paciente paciente = pacienteOpt.get();
                Map<String, Object> response = new HashMap<>();
                response.put("role", paciente.getRole());
                response.put("idPaciente", paciente.getId_paciente());
                return ResponseEntity.ok(response);
            } else if (cadastramentoOpt.isPresent() && emailOrCpf.equals(cadastramentoOpt.get().getEmail())) {
                return ResponseEntity.ok(cadastramentoOpt.get().getRole());
            } else if (contaOpt.isPresent() && emailOrCpf.equals(contaOpt.get().getLogin())) {
                Conta conta = contaOpt.get();
                Role role = conta.getRoleEnum();

                if (role == Role.ESPECIALISTA) {
                    Especialista especialista = conta.getEspecialista();
                    if (especialista != null) {
                        Map<String, Object> response = new HashMap<>();
                        response.put("role", role);
                        response.put("idEspecialista", especialista.getId());
                        return ResponseEntity.ok(response);
                    } else {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Erro: Conta do especialista não associada a nenhum especialista.");
                    }
                }

                if (role == Role.PACIENTE) {
                    Paciente paciente = conta.getPaciente();
                    if (paciente != null) {
                        Map<String, Object> response = new HashMap<>();
                        response.put("role", role);
                        response.put("idPaciente", paciente.getId_paciente());
                        return ResponseEntity.ok(response);
                    } else {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Erro: Conta do paciente não associada a nenhum paciente.");
                    }
                }

                return ResponseEntity.ok(role);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Email ou senha incorretos.");
            }

        }, asyncExecutor);
    }

    @PutMapping("/marcarPresenca")
    @Operation(summary = "Confirming the presence of the patient for a certain appointment.")
    public CompletableFuture<ResponseEntity<?>> marcandoPresenca(
            @RequestParam String codigoCodigo,
            @RequestParam Long id_agendamento) {

        return CompletableFuture.supplyAsync(() -> {

            var pacienteOpt = pacienteRepository.findByCodigoCodigo(codigoCodigo);

            if (pacienteOpt.isPresent()) {
                Paciente objPaciente = pacienteOpt.get();

                // Verifica se já existe um agendamento com status EM_ESPERA para o paciente
                boolean jaEmEspera = agendamentoRepository.existsByPacienteAndStatus(objPaciente, StatusAgendamento.EM_ESPERA);
                if (jaEmEspera) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body("O paciente já possui um agendamento com o status EM_ESPERA.");
                }

                Optional<Agendamento> agendamentoOpt = agendamentoRepository.findById(id_agendamento);
                if (agendamentoOpt.isPresent()) {
                    Agendamento objAgendamento = agendamentoOpt.get();

                    // Atualiza o status e data/hora de chegada
                    objAgendamento.setStatus(StatusAgendamento.EM_ESPERA);
                    objAgendamento.setDataHoraChegada(LocalDateTime.now());
                    agendamentoRepository.save(objAgendamento);

                    // Confirma a presença do paciente, caso ainda não confirmada
                    if (!objPaciente.getPresencaConfirmado()) {
                        objPaciente.setPresencaConfirmado(true);
                        objPaciente.setDataHoraChegada(LocalDateTime.now());
                        pacienteRepository.save(objPaciente);

                        return ResponseEntity.status(HttpStatus.OK)
                                .body("Sua presença foi confirmada com Sucesso!");
                    } else {
                        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                                .body("Sua presença na fila já foi confirmada.");
                    }
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body("Agendamento não encontrado.");
                }
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Paciente não encontrado.");
            }

        }, asyncExecutor);
    }


    @GetMapping("/primeiroPacienteEspecialista/{especialistaId}")
    @Operation(summary = "Seeking the first patient on hold and the patient being cared for now.")
    public ResponseEntity<Map<String, Object>> getPrimeiroPacientePorEspecialista(
            @PathVariable Long especialistaId) {
        return ResponseEntity.ok(getPrimeiroPacientePorEspecialistaId(especialistaId));
    }

    // Método auxiliar para obter os pacientes por ID do especialista
    private Map<String, Object> getPrimeiroPacientePorEspecialistaId(Long especialistaId) {
        Map<String, Object> pacienteInfo = new HashMap<>();

        // Paciente em espera
        List<Agendamento> agendamentosEmEspera = agendamentoRepository
                .findAllByEspecialista_IdAndStatusAndPaciente_PresencaConfirmado(
                        especialistaId, StatusAgendamento.EM_ESPERA, true);

        Optional<Agendamento> primeiroEmEspera = agendamentosEmEspera.stream()
                .sorted(Comparator.comparingInt((Agendamento a) ->
                                a.getPaciente().getPrioridade().getPrioridade())
                        .thenComparing(Agendamento::getDataAgendamento))
                .findFirst();

        // Paciente em atendimento
        List<Agendamento> agendamentosEmAtendimento = agendamentoRepository
                .findAllByEspecialista_IdAndStatusAndPaciente_PresencaConfirmado(
                        especialistaId, StatusAgendamento.EM_ATENDIMENTO, true);

        Optional<Agendamento> primeiroEmAtendimento = agendamentosEmAtendimento.stream()
                .findFirst();

        // Dados do paciente em espera (ou null se não houver)
        pacienteInfo.put("PacienteEmEspera", primeiroEmEspera.map(agendamento -> {
            Map<String, String> esperaInfo = new HashMap<>();
            esperaInfo.put("Nome", agendamento.getPaciente().getNomeCompleto());
            esperaInfo.put("Status", agendamento.getStatus().name());
            return esperaInfo;
        }).orElse(null));

        // Dados do paciente em atendimento (ou null se não houver)
        pacienteInfo.put("PacienteEmAtendimento", primeiroEmAtendimento.map(agendamento -> {
            Map<String, String> atendimentoInfo = new HashMap<>();
            atendimentoInfo.put("Nome", agendamento.getPaciente().getNomeCompleto());
            atendimentoInfo.put("Status", agendamento.getStatus().name());
            return atendimentoInfo;
        }).orElse(null));

        return pacienteInfo;
    }


    @GetMapping("/contagemEspecialista/{especialistaId}")
    @Operation(summary = "Counts the amount of patient waiting for consultation.")
    public ResponseEntity<Map<String, Integer>> getContagemPacientesPorEspecialista(
            @PathVariable Long especialistaId) {
        return ResponseEntity.ok(contarPacientesPorEspecialistaId(especialistaId));
    }

    // Método auxiliar para contar os pacientes em espera por ID do especialista
    private Map<String, Integer> contarPacientesPorEspecialistaId(Long especialistaId) {
        int contagem = agendamentoRepository.countByEspecialista_IdAndStatus(
                especialistaId, StatusAgendamento.EM_ESPERA);

        // Criar JSON de resposta com a contagem
        Map<String, Integer> contagemResponse = new HashMap<>();
        contagemResponse.put("QuantidadePacientesEmEspera", contagem);

        return contagemResponse;
    }



    /**
     * Endpoint para chamar o próximo paciente baseado no ID do especialista.
     */
    @PutMapping("/chamarPaciente")
    @Operation(summary = "Call the first patient in line with the staus EM_ESPERA.")
    public CompletableFuture<ResponseEntity<?>> chamarPaciente(@RequestParam Long idEspecialista) {
        return chamarPrimeiroPacientePorEspecialista(idEspecialista);
    }

    /**
     * Método auxiliar para buscar o próximo paciente para um especialista específico.
     */
    private CompletableFuture<ResponseEntity<?>> chamarPrimeiroPacientePorEspecialista(Long idEspecialista) {
        return CompletableFuture.supplyAsync(() -> {
            // Busca os agendamentos em espera para o especialista específico e com presença confirmada
            List<Agendamento> agendamentos = agendamentoRepository
                    .findAllByEspecialista_IdAndStatusAndPaciente_PresencaConfirmado(
                            idEspecialista, StatusAgendamento.EM_ESPERA, true);

            // Ordena por prioridade e data de chegada, e pega o primeiro da lista
            Optional<Agendamento> primeiroAgendamento = agendamentos.stream()
                    .sorted(Comparator
                            .comparingInt((Agendamento a) -> a.getPaciente().getPrioridade().getPrioridade())
                            .thenComparing(Agendamento::getDataHoraChegada))
                    .findFirst();

            if (primeiroAgendamento.isPresent()) {
                // Muda o status do agendamento para EM_ATENDIMENTO
                Agendamento agendamento = primeiroAgendamento.get();
                agendamento.setStatus(StatusAgendamento.EM_ATENDIMENTO);
                agendamentoRepository.save(agendamento);

                // Retorna as informações do paciente
                Paciente paciente = agendamento.getPaciente();
                Map<String, Object> pacienteInfo = new HashMap<>();
                pacienteInfo.put("id", paciente.getId_paciente());
                pacienteInfo.put("nome", paciente.getNomeCompleto());
                pacienteInfo.put("sexo", paciente.getSexo());
                pacienteInfo.put("prioridade", paciente.getPrioridade().getPrioridade());
                pacienteInfo.put("horaChegada", paciente.getDataHoraChegada());
                pacienteInfo.put("status", agendamento.getStatus());

                return ResponseEntity.ok(pacienteInfo);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Nenhum paciente em espera para o especialista com ID " + idEspecialista);
            }
        }, asyncExecutor);
    }



    /**
     * Endpoint para adicionar uma observação ao prontuário de um paciente e marcar o agendamento como concluído.
     *
     * @param pacienteId ID do paciente cujo prontuário será atualizado.
     * @param novaObservacao A nova observação a ser adicionada ao prontuário.
     * @return ResponseEntity indicando o sucesso ou erro da operação.
     */
    @PutMapping("/adicionarObservacaoProntuario")
    @Operation(summary = "Edits the patient's report that is in attendance.")
    public ResponseEntity<String> adicionarObservacaoProntuario(
            @RequestParam Long pacienteId,
            @RequestParam String novaObservacao) {

        // Busca o paciente pelo ID
        Optional<Paciente> pacienteOpt = pacienteRepository.findById(pacienteId);

        if (pacienteOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Paciente não encontrado.");
        }

        Paciente paciente = pacienteOpt.get();

        // Atualiza o campo prontuario com a nova observação
        String prontuarioAtualizado = paciente.getProntuario() == null
                ? novaObservacao
                : paciente.getProntuario() + "\n" + novaObservacao;

        paciente.setProntuario(prontuarioAtualizado);

        // Busca o agendamento do paciente que está em atendimento
        Optional<Agendamento> agendamentoOpt = agendamentoRepository
                .findByPacienteAndStatus(paciente, StatusAgendamento.EM_ATENDIMENTO);

        if (agendamentoOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Nenhum agendamento em atendimento encontrado para este paciente.");
        }

        Agendamento agendamento = agendamentoOpt.get();

        // Atualiza o status do agendamento para CONCLUIDO
        agendamento.setStatus(StatusAgendamento.CONCLUIDO);

        // Salva as alterações no banco de dados
        pacienteRepository.save(paciente);
        agendamentoRepository.save(agendamento);

        return ResponseEntity.ok("Observação adicionada ao prontuário e agendamento concluído com sucesso.");
    }




    // Fazer GET retornar o usuario que possui conta no software.
    // Tal GET deve retornar a Role do usuario.
/*
    @GetMapping("/login")
    public CompletableFuture<ResponseEntity<?>> getRoleByLogin(
            @RequestParam String emailOrCpf,
            @RequestParam String password ) {

        return CompletableFuture.supplyAsync(() -> {

            // Busca nas três entidades pelo códigoCodigo
            Optional<Paciente> pacienteOpt = pacienteRepository.findByCodigoCodigo(password);
            Optional<Cadastramento> cadastramentoOpt = cadastramentoRepository.findByCodigoCodigo(password);
            Optional<Conta> contaOpt = contaRepository.findByPassword(password);

            // Verifica se encontrou o registro e se o emailOrCpf corresponde
            if (pacienteOpt.isPresent() && emailOrCpf.equals(pacienteOpt.get().getEmail())) {
                return ResponseEntity.ok(pacienteOpt.get().getRole());
            } else if (cadastramentoOpt.isPresent() && emailOrCpf.equals(cadastramentoOpt.get().getEmail())) {
                return ResponseEntity.ok(cadastramentoOpt.get().getRole());
            } else if (contaOpt.isPresent() && emailOrCpf.equals(contaOpt.get().getLogin())) {
                return ResponseEntity.ok(contaOpt.get().getRoleEnum());
            } else {
                // Se nenhum dos registros corresponder, retorna uma mensagem de erro e status 401 Unauthorized
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Email ou senha incorretos.");
            }

        }, asyncExecutor);
    }

*/










    /*
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

            Paciente objetoPaciente = paciente.get();

            if (!pacienteWithCodigoCodigo.isEmpty() && email == objetoPaciente.getEmail()) {

                //Paciente objetoPaciente = paciente.get();
                return ResponseEntity.ok(objetoPaciente.getRole());
            } else {
                // Retorna mensagem de erro e status 401 Unauthorized
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Email ou senha incorretos.");
            }

        }, asyncExecutor);
    }
    */


    /*
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


                //Conta conta = new Conta(
                //        cadastramento.getNomeCompleto(),
                //        cadastramento.getCodigoCodigo(),
                //        Role.ESPECIALISTA
                //);

                //contaRepository.save(conta);

                return ResponseEntity.status(HttpStatus.CREATED)
                        .body("Cadastro efetuado com Sucesso!");

            } catch (Exception e){
                return ResponseEntity.status(500).body("Erro ao realizar cadastro: " + e.getMessage());

            }

        }, asyncExecutor);


    }

    */






    // EndPoint que irá trazer todos os Agendamento/Pacientes na fila que estao
    // sendo atendidos, ou seja possuem irão ser mostrados os que possuem Status EM_ESPERA.

/*
    @GetMapping("/primeiroPacientePorEspecialidade")
    public ResponseEntity<Map<TipoEspecialista, String>> getPrimeiroPacientePorEspecialidade() {
        Map<TipoEspecialista, String> primeiroPacientePorEspecialidade = new HashMap<>();

        for (TipoEspecialista tipoEspecialista : TipoEspecialista.values()) {
            List<Agendamento> agendamentosPorEspecialidade = agendamentoRepositry.findAllByEspecialista_TipoEspecialistaAndStatusAndPaciente_PresencaConfirmado(
                    tipoEspecialista, StatusAgendamento.EM_ESPERA, true);

            Optional<Agendamento> primeiroAgendamento = agendamentosPorEspecialidade.stream()
                    .sorted(Comparator.comparingInt((Agendamento a) -> a.getPaciente().getPrioridade().getPrioridade())
                            .thenComparing(a -> a.getDataAgendamento()))  // Ajuste para campo correto
                    .findFirst();

            primeiroAgendamento.ifPresent(agendamento ->
                    primeiroPacientePorEspecialidade.put(tipoEspecialista, agendamento.getPaciente().getNomeCompleto())
            );
        }

        return ResponseEntity.ok(primeiroPacientePorEspecialidade);
    }
*/

/*
    @GetMapping("/primeiroPacienteOdontologo")
    public ResponseEntity<Map<String, Object>> getPrimeiroPacienteOdontologo() {
        return ResponseEntity.ok(getPrimeiroPacientePorEspecialidade(TipoEspecialista.ODONTOLOGO));
    }

    @GetMapping("/primeiroPacienteOrtopedista")
    public ResponseEntity<Map<String, Object>> getPrimeiroPacienteOrtopedista() {
        return ResponseEntity.ok(getPrimeiroPacientePorEspecialidade(TipoEspecialista.ORTOPEDISTA));
    }

    @GetMapping("/primeiroPacienteCardiologista")
    public ResponseEntity<Map<String, Object>> getPrimeiroPacienteCardiologista() {
        return ResponseEntity.ok(getPrimeiroPacientePorEspecialidade(TipoEspecialista.CARDIOLOGISTA));
    }



    // Método auxiliar para obter os pacientes por especialidade
    private Map<String, Object> getPrimeiroPacientePorEspecialidade(TipoEspecialista tipoEspecialista) {
        Map<String, Object> pacienteInfo = new HashMap<>();

        // Paciente em espera
        List<Agendamento> agendamentosEmEspera = agendamentoRepository
                .findAllByEspecialista_TipoEspecialistaAndStatusAndPaciente_PresencaConfirmado(
                        tipoEspecialista, StatusAgendamento.EM_ESPERA, true);

        Optional<Agendamento> primeiroEmEspera = agendamentosEmEspera.stream()
                .sorted(Comparator.comparingInt((Agendamento a) ->
                                a.getPaciente().getPrioridade().getPrioridade())
                        .thenComparing(Agendamento::getDataAgendamento))
                .findFirst();

        // Paciente em atendimento
        List<Agendamento> agendamentosEmAtendimento = agendamentoRepository
                .findAllByEspecialista_TipoEspecialistaAndStatusAndPaciente_PresencaConfirmado(
                        tipoEspecialista, StatusAgendamento.EM_ATENDIMENTO, true);

        Optional<Agendamento> primeiroEmAtendimento = agendamentosEmAtendimento.stream()
                .findFirst();

        // Dados do paciente em espera (ou null se não houver)
        pacienteInfo.put("PacienteEmEspera", primeiroEmEspera.map(agendamento -> {
            Map<String, String> esperaInfo = new HashMap<>();
            esperaInfo.put("Nome", agendamento.getPaciente().getNomeCompleto());
            esperaInfo.put("Status", agendamento.getStatus().name());
            return esperaInfo;
        }).orElse(null));

        // Dados do paciente em atendimento (ou null se não houver)
        pacienteInfo.put("PacienteEmAtendimento", primeiroEmAtendimento.map(agendamento -> {
            Map<String, String> atendimentoInfo = new HashMap<>();
            atendimentoInfo.put("Nome", agendamento.getPaciente().getNomeCompleto());
            atendimentoInfo.put("Status", agendamento.getStatus().name());
            return atendimentoInfo;
        }).orElse(null));

        return pacienteInfo;
    }



    @GetMapping("/contagemOdontologo")
    public ResponseEntity<Map<String, Integer>> getContagemOdontologoo() {
        return ResponseEntity.ok(contarPacientesPorEspecialidade(TipoEspecialista.ODONTOLOGO));
    }

    @GetMapping("/contagemOrtopedista")
    public ResponseEntity<Map<String, Integer>> getContagemOrtopedistaa() {
        return ResponseEntity.ok(contarPacientesPorEspecialidade(TipoEspecialista.ORTOPEDISTA));
    }

    @GetMapping("/contagemCardiologista")
    public ResponseEntity<Map<String, Integer>> getContagemCardiologistaa() {
        return ResponseEntity.ok(contarPacientesPorEspecialidade(TipoEspecialista.CARDIOLOGISTA));
    }

    // Método auxiliar para contar os pacientes em espera por especialidade
    private Map<String, Integer> contarPacientesPorEspecialidade(TipoEspecialista tipoEspecialista) {
        int contagem = agendamentoRepository.countByEspecialista_TipoEspecialistaAndStatus(
                tipoEspecialista, StatusAgendamento.EM_ESPERA);

        // Criar JSON de resposta com a contagem
        Map<String, Integer> contagemResponse = new HashMap<>();
        contagemResponse.put("QuantidadePacientesEmEspera", contagem);

        return contagemResponse;
    }



 */


    /**
     * Método auxiliar para obter o primeiro paciente para o tipo de especialista.
     */
    /*
    private CompletableFuture<ResponseEntity<?>> getPrimeiroPacientePorEspecialidadee(TipoEspecialista tipoEspecialista) {
        return CompletableFuture.supplyAsync(() -> {
            // Busca os agendamentos em espera para o tipo de especialista e com presença confirmada
            List<Agendamento> agendamentos = agendamentoRepository
                    .findAllByEspecialista_TipoEspecialistaAndStatusAndPaciente_PresencaConfirmado(
                            tipoEspecialista, StatusAgendamento.EM_ESPERA, true);

            // Ordena por prioridade e data de chegada, e pega o primeiro da lista
            Optional<Agendamento> primeiroAgendamento = agendamentos.stream()
                    .sorted(Comparator
                            .comparingInt((Agendamento a) -> a.getPaciente().getPrioridade().getPrioridade())
                            .thenComparing(Agendamento::getDataHoraChegada))
                    .findFirst();

            if (primeiroAgendamento.isPresent()) {
                // Muda o status do agendamento para EM_ATENDIMENTO
                Agendamento agendamento = primeiroAgendamento.get();
                agendamento.setStatus(StatusAgendamento.EM_ATENDIMENTO);
                agendamentoRepository.save(agendamento);

                // Retorna as informações do paciente
                Paciente paciente = agendamento.getPaciente();
                Map<String, Object> pacienteInfo = new HashMap<>();
                pacienteInfo.put("id", paciente.getId_paciente());
                pacienteInfo.put("nome", paciente.getNomeCompleto());
                pacienteInfo.put("prioridade", paciente.getPrioridade().getPrioridade());
                pacienteInfo.put("horaChegada", paciente.getDataHoraChegada());
                pacienteInfo.put("status", agendamento.getStatus());

                return ResponseEntity.ok(pacienteInfo);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Nenhum paciente em espera para " + tipoEspecialista);
            }
        }, asyncExecutor);
    }



     */



    /**
     * Endpoint para buscar o primeiro paciente para Odontologia.
     */
    //@PutMapping("/chamarPacienteOdontologia")
    //public CompletableFuture<ResponseEntity<?>> chamarPrimeiroPacienteOdontologia() {
    //    return chamarPrimeiroPacientePorEspecialidade(TipoEspecialista.ODONTOLOGO);
    //}

    /**
     * Endpoint para buscar o primeiro paciente para Ortopedia.
     */
    //@PutMapping("/chamarPacienteOrtopedia")
    //public CompletableFuture<ResponseEntity<?>> chamarPrimeiroPacienteOrtopedia() {
    //    return chamarPrimeiroPacientePorEspecialidade(TipoEspecialista.ORTOPEDISTA);
    //}

    /**
     * Endpoint para buscar o primeiro paciente para Cardiologia.
     */
    //@PutMapping("/chamarPacienteCardiologia")
    //public CompletableFuture<ResponseEntity<?>> chamarPrimeiroPacienteCardiologia() {
    //    return chamarPrimeiroPacientePorEspecialidade(TipoEspecialista.CARDIOLOGISTA);
    //}

    /**
     * Método auxiliar para obter o primeiro paciente para o tipo de especialista.
     */
    /*
    private CompletableFuture<ResponseEntity<?>> chamarPrimeiroPacientePorEspecialidade(TipoEspecialista tipoEspecialista) {
        return CompletableFuture.supplyAsync(() -> {
            // Busca os agendamentos em espera para o tipo de especialista e com presença confirmada
            List<Agendamento> agendamentos = agendamentoRepository
                    .findAllByEspecialista_TipoEspecialistaAndStatusAndPaciente_PresencaConfirmado(
                            tipoEspecialista, StatusAgendamento.EM_ESPERA, true);

            // Ordena por prioridade e data de chegada, e pega o primeiro da lista
            Optional<Agendamento> primeiroAgendamento = agendamentos.stream()
                    .sorted(Comparator
                            .comparingInt((Agendamento a) -> a.getPaciente().getPrioridade().getPrioridade())
                            .thenComparing(Agendamento::getDataHoraChegada))
                    .findFirst();

            if (primeiroAgendamento.isPresent()) {
                // Muda o status do agendamento para EM_ATENDIMENTO
                Agendamento agendamento = primeiroAgendamento.get();
                agendamento.setStatus(StatusAgendamento.EM_ATENDIMENTO);
                agendamentoRepository.save(agendamento);

                // Retorna as informações do paciente
                Paciente paciente = agendamento.getPaciente();
                Map<String, Object> pacienteInfo = new HashMap<>();
                pacienteInfo.put("id", paciente.getId_paciente());
                pacienteInfo.put("nome", paciente.getNomeCompleto());
                pacienteInfo.put("sexo", paciente.getSexo());
                pacienteInfo.put("prioridade", paciente.getPrioridade().getPrioridade());
                pacienteInfo.put("horaChegada", paciente.getDataHoraChegada());
                pacienteInfo.put("status", agendamento.getStatus());

                return ResponseEntity.ok(pacienteInfo);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Nenhum paciente em espera para " + tipoEspecialista);
            }
        }, asyncExecutor);
    }

     */







}
