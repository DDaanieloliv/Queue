package com.ddaaniel.queue.service;

import com.ddaaniel.queue.domain.Exception.EspecialistaNotFoundException;
import com.ddaaniel.queue.domain.model.Conta;
import com.ddaaniel.queue.domain.model.Indisponibilidade;
import com.ddaaniel.queue.domain.model.Especialista;
import com.ddaaniel.queue.domain.model.enuns.Role;
import com.ddaaniel.queue.domain.repository.ContaRepository;
import com.ddaaniel.queue.domain.repository.DisponibilidadeRepository;
import com.ddaaniel.queue.domain.repository.EspecialistaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class EspecialistaService {

    @Autowired
    private EspecialistaRepository especialistaRepository;

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private DisponibilidadeRepository disponibilidadeRepository;

    public Especialista findByIdEspecialista(Long id_especialista){

        Especialista especialista = especialistaRepository.findById(id_especialista)
                .orElseThrow(() -> new EspecialistaNotFoundException("Especialista não encontrado para o ID: " + id_especialista));

        return especialista;
    }


    // No especialistaService
    public Page<Especialista> findAllEspecialistas(int page, int pageSize) {
        Page<Especialista> especialistas = especialistaRepository.findAll(PageRequest.of(page, pageSize));
        especialistas.forEach(especialista -> especialista.getIndisponibilidades().size()); // Inicializa a coleção
        return especialistas;
    }

    public void criarEspecialista(Especialista especialista) {
        // Gera o código de acesso
        especialista.gerarCodigoCodigo();

        // Cria a Conta associada ao Especialista
        Conta conta = new Conta(
                especialista.getEmail(),         // Login da conta será o email do especialista
                especialista.getCodigoCodigo(),  // Senha da conta será o código gerado
                Role.ESPECIALISTA                // Define a role como ESPECIALISTA
        );
        contaRepository.save(conta);

        // Associa a Conta criada ao Especialista
        especialista.setConta(conta);

        // Configura o relacionamento entre Especialista e Indisponibilidades
        if (especialista.getIndisponibilidades() != null && !especialista.getIndisponibilidades().isEmpty()) {
            for (Indisponibilidade indisponibilidade : especialista.getIndisponibilidades()) {
                indisponibilidade.setEspecialista(especialista);
            }
        }

        // Salva o Especialista no banco (o Hibernate cuidará das indisponibilidades por cascade)
        especialistaRepository.save(especialista);

        // Envia o e-mail com as credenciais de acesso
        emailService.enviarEmail(especialista.getEmail(), especialista.getCodigoCodigo());
    }
}
