package com.ddaaniel.queue.service;

import com.ddaaniel.queue.domain.model.Agendamento;
import com.ddaaniel.queue.domain.model.Especialista;
import com.ddaaniel.queue.domain.model.dto.RecordDtoAgendamento;
import com.ddaaniel.queue.domain.repository.EspecialistaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AgendamentoService {

    @Autowired
    private EspecialistaRepository especialistaRepository;

    @Autowired
    private EspecialistaService especialistaService;

    public Agendamento criarAgendamento(RecordDtoAgendamento recordDto) {
        // Busca o especialista pelo ID

        Especialista especialista = especialistaService.findByIdEspecialista(recordDto.especialista());

        //Especialista especialista = especialistaRepository.findById(recordDto.especialista())
        //        .orElseThrow(() -> new RuntimeException("Especialista não encontrado para o ID: " + recordDto.especialista()));

        // Cria e retorna o agendamento
        return recordDto.fromDtoToEntitys(especialista);
    }


}
