package com.ddaaniel.queue.service;

import com.ddaaniel.queue.domain.model.Especialista;
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

    public Especialista findByIdEspecialista(Long id_especialista){

        Especialista especialista = especialistaRepository.findById(id_especialista)
                .orElseThrow(() -> new RuntimeException("Especialista não encontrado para o ID: " + id_especialista));

        return especialista;
    }


    public Page<Especialista> findAllEspecialistas(int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        return especialistaRepository.findAll(pageable);
    }

}
