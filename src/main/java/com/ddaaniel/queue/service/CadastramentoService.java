package com.ddaaniel.queue.service;

import com.ddaaniel.queue.domain.model.Cadastramento;
import com.ddaaniel.queue.domain.model.Conta;
import com.ddaaniel.queue.domain.model.enuns.Role;
import com.ddaaniel.queue.domain.repository.CadastramentoRepository;
import com.ddaaniel.queue.domain.repository.ContaRepository;
import com.ddaaniel.queue.domain.repository.PacienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CadastramentoService {

    @Autowired
    private CadastramentoRepository cadastramentoRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private ContaRepository contaRepository;

    public void cadastrando(Cadastramento cadastramento){
        String codigo;
        do {

            // Gera um código aleatório
            cadastramento.gerarCodigoCodigo();
            codigo = cadastramento.getCodigoCodigo();

        } while (!isCodigoUnico(codigo) && !isCodigoUnicoInEntityPaciente(codigo));  // Verifica se é único
        cadastramentoRepository.save(cadastramento);

        Conta conta = new Conta(
                cadastramento.getEmail(),
                cadastramento.getCodigoCodigo(), Role.ESPECIALISTA);

        contaRepository.save(conta);
    }

    private boolean isCodigoUnico(String codigo) {
        return !cadastramentoRepository.existsByCodigoCodigo(codigo);
    }


    private boolean isCodigoUnicoInEntityPaciente(String codigo) {
        return !pacienteRepository.existsByCodigoCodigo(codigo);
    }


    public String obterEmailCadastramento(Long id_cadastramento) {
        Optional<Cadastramento> cadastramentoOpt = cadastramentoRepository.findById(id_cadastramento);
        return cadastramentoOpt.map(Cadastramento::getEmail).orElse(null);
    }


    public String obterCodigoCadastramento(Long id_cadastramento) {
        Optional<Cadastramento> cadastramentoOpt = cadastramentoRepository.findById(id_cadastramento);
        return cadastramentoOpt.map(Cadastramento::getCodigoCodigo).orElse(null);
    }
}
