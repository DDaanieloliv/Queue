package com.ddaaniel.queue.service;

import com.ddaaniel.queue.domain.model.Conta;
import com.ddaaniel.queue.domain.model.Paciente;
import com.ddaaniel.queue.domain.model.enuns.Role;
import com.ddaaniel.queue.domain.repository.ContaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ContaService {

    @Autowired
    private ContaRepository contaRepository;

    public Conta criaContaPaciente(Paciente paciente) {

        //var pacienteWithCodigoCodigo = contaRepository
        //        .findByCodigoCodigoo(paciente.getCodigoCodigo());

        //var emailOf = paciente.getEmail();
        Conta contaPacinte = new Conta(
                paciente.getEmail(),
                paciente.getCodigoCodigo(),
                Role.ESPECIALISTA);

        return contaPacinte;
    }



}
