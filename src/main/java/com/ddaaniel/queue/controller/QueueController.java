package com.ddaaniel.queue.controller;

import com.ddaaniel.queue.domain.model.*;
import com.ddaaniel.queue.domain.model.dto.AgendamentoDTO;
import com.ddaaniel.queue.domain.model.dto.EspecialistaRecordDtoResponce;
import com.ddaaniel.queue.domain.model.dto.RecordDtoAgendamento;
import com.ddaaniel.queue.domain.model.enuns.StatusAgendamento;
import com.ddaaniel.queue.domain.model.enuns.TipoEspecialista;
import com.ddaaniel.queue.domain.repository.*;
import com.ddaaniel.queue.service.*;
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
    private AgendamentoRepositry agendamentoRepositry;

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

        return CompletableFuture.supplyAsync(() -> {

            // Busca nas três entidades pelo códigoCodigo
            Optional<Paciente> pacienteOpt = pacienteRepository.findByCodigoCodigo(password);
            Optional<Cadastramento> cadastramentoOpt = cadastramentoRepository.findByCodigoCodigo(password);
            Optional<Conta> contaOpt = contaRepository.findByPassword(password);

            // Verifica se encontrou o registro e se o email corresponde
            if (pacienteOpt.isPresent() && email.equals(pacienteOpt.get().getEmail())) {
                return ResponseEntity.ok(pacienteOpt.get().getRole());
            } else if (cadastramentoOpt.isPresent() && email.equals(cadastramentoOpt.get().getEmail())) {
                return ResponseEntity.ok(cadastramentoOpt.get().getRole());
            } else if (contaOpt.isPresent() && email.equals(contaOpt.get().getLogin())) {
                return ResponseEntity.ok(contaOpt.get().getRoleEnum());
            } else {
                // Se nenhum dos registros corresponder, retorna uma mensagem de erro e status 401 Unauthorized
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Email ou senha incorretos.");
            }

        }, asyncExecutor);
    }
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

    @GetMapping("/pegarAgendamentos")
    public List<AgendamentoDTO> getAgendaamentosByCodigoCodigo(@RequestParam String codigoCodigo) {

        return agendamentoService.getAllAgendamentosByCodigoCodigo(codigoCodigo);

    }

    @PutMapping("/marcarPresenca")
    public CompletableFuture<ResponseEntity<?>> marcandoPresenca(
            @RequestParam String codigoCodigo,
           @RequestParam Long id_agendamento /*DEVE RECEBER O VALOR ESCOLHIDO DE /pegarAgendamentos*/){

        return CompletableFuture.supplyAsync( () -> {

            var paciente = pacienteRepository.findByCodigoCodigo(codigoCodigo);

            if (paciente.isPresent()) {
                Paciente objPaciente = paciente.get();

                Optional<Agendamento> agendamento = agendamentoRepositry.findById(id_agendamento);
                Agendamento objAgendamento = agendamento.get();

                objAgendamento.setStatus(StatusAgendamento.EM_ESPERA);
                objAgendamento.setDataHoraChegada(LocalDateTime.now());
                agendamentoRepositry.save(objAgendamento);

                if (!objPaciente.getPresencaConfirmado()) {
                    objPaciente.setPresencaConfirmado(true);
                    objPaciente.setDataHoraChegada(LocalDateTime.now());

                    // Salva a alteração no banco de dados
                    pacienteRepository.save(objPaciente);

                    return ResponseEntity.status(HttpStatus.OK)
                            .body("Sua presença foi confirmada com Sucesso!");
                } else {
                    return ResponseEntity.status(HttpStatus.NO_CONTENT)
                            .body("Sua presença na fila já foi confirmada.");
                }
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Paciente não encontrado.");
            }

        }, asyncExecutor);

    }


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
        List<Agendamento> agendamentosEmEspera = agendamentoRepositry
                .findAllByEspecialista_TipoEspecialistaAndStatusAndPaciente_PresencaConfirmado(
                        tipoEspecialista, StatusAgendamento.EM_ESPERA, true);

        Optional<Agendamento> primeiroEmEspera = agendamentosEmEspera.stream()
                .sorted(Comparator.comparingInt((Agendamento a) ->
                                a.getPaciente().getPrioridade().getPrioridade())
                        .thenComparing(Agendamento::getDataAgendamento))
                .findFirst();

        // Paciente em atendimento
        List<Agendamento> agendamentosEmAtendimento = agendamentoRepositry
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
        int contagem = agendamentoRepositry.countByEspecialista_TipoEspecialistaAndStatus(
                tipoEspecialista, StatusAgendamento.EM_ESPERA);

        // Criar JSON de resposta com a contagem
        Map<String, Integer> contagemResponse = new HashMap<>();
        contagemResponse.put("QuantidadePacientesEmEspera", contagem);

        return contagemResponse;
    }


    /**
     * Endpoint para buscar o primeiro paciente para Odontologia.
     */
    @PutMapping("/primeiroPacienteOdontologia")
    public CompletableFuture<ResponseEntity<?>> getPrimeiroPacienteOdontologia() {
        return getPrimeiroPacientePorEspecialidadee(TipoEspecialista.ODONTOLOGO);
    }

    /**
     * Endpoint para buscar o primeiro paciente para Ortopedia.
     */
    @PutMapping("/primeiroPacienteOrtopedia")
    public CompletableFuture<ResponseEntity<?>> getPrimeiroPacienteOrtopedia() {
        return getPrimeiroPacientePorEspecialidadee(TipoEspecialista.ORTOPEDISTA);
    }

    /**
     * Endpoint para buscar o primeiro paciente para Cardiologia.
     */
    @PutMapping("/primeiroPacienteCardiologia")
    public CompletableFuture<ResponseEntity<?>> getPrimeiroPacienteCardiologia() {
        return getPrimeiroPacientePorEspecialidadee(TipoEspecialista.CARDIOLOGISTA);
    }

    /**
     * Método auxiliar para obter o primeiro paciente para o tipo de especialista.
     */
    private CompletableFuture<ResponseEntity<?>> getPrimeiroPacientePorEspecialidadee(TipoEspecialista tipoEspecialista) {
        return CompletableFuture.supplyAsync(() -> {
            // Busca os agendamentos em espera para o tipo de especialista e com presença confirmada
            List<Agendamento> agendamentos = agendamentoRepositry
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
                agendamentoRepositry.save(agendamento);

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





    /**
     * Endpoint para buscar o primeiro paciente para Odontologia.
     */
    @PutMapping("/chamarPacienteOdontologia")
    public CompletableFuture<ResponseEntity<?>> chamarPrimeiroPacienteOdontologia() {
        return chamarPrimeiroPacientePorEspecialidade(TipoEspecialista.ODONTOLOGO);
    }

    /**
     * Endpoint para buscar o primeiro paciente para Ortopedia.
     */
    @PutMapping("/chamarPacienteOrtopedia")
    public CompletableFuture<ResponseEntity<?>> chamarPrimeiroPacienteOrtopedia() {
        return chamarPrimeiroPacientePorEspecialidade(TipoEspecialista.ORTOPEDISTA);
    }

    /**
     * Endpoint para buscar o primeiro paciente para Cardiologia.
     */
    @PutMapping("/chamarPacienteCardiologia")
    public CompletableFuture<ResponseEntity<?>> chamarPrimeiroPacienteCardiologia() {
        return chamarPrimeiroPacientePorEspecialidade(TipoEspecialista.CARDIOLOGISTA);
    }

    /**
     * Método auxiliar para obter o primeiro paciente para o tipo de especialista.
     */
    private CompletableFuture<ResponseEntity<?>> chamarPrimeiroPacientePorEspecialidade(TipoEspecialista tipoEspecialista) {
        return CompletableFuture.supplyAsync(() -> {
            // Busca os agendamentos em espera para o tipo de especialista e com presença confirmada
            List<Agendamento> agendamentos = agendamentoRepositry
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
                agendamentoRepositry.save(agendamento);

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



    /**
     * Endpoint para adicionar uma observação ao prontuário de um paciente e marcar o agendamento como concluído.
     *
     * @param pacienteId ID do paciente cujo prontuário será atualizado.
     * @param novaObservacao A nova observação a ser adicionada ao prontuário.
     * @return ResponseEntity indicando o sucesso ou erro da operação.
     */
    @PutMapping("/adicionarObservacaoProntuario")
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
        Optional<Agendamento> agendamentoOpt = agendamentoRepositry
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
        agendamentoRepositry.save(agendamento);

        return ResponseEntity.ok("Observação adicionada ao prontuário e agendamento concluído com sucesso.");
    }
}
