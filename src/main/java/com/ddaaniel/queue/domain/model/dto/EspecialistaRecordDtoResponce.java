package com.ddaaniel.queue.domain.model.dto;

import com.ddaaniel.queue.domain.model.Especialista;
import com.ddaaniel.queue.domain.model.enuns.TipoEspecialista;

public record EspecialistaRecordDtoResponce(Long id, String nome, TipoEspecialista tipoEspecialista) {

    public EspecialistaRecordDtoResponce(Especialista especialista) {
        this(especialista.getId(), especialista.getNome(), especialista.getTipoEspecialista());
    }
}
