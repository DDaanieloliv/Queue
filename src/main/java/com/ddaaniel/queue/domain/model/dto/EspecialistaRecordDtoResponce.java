package com.ddaaniel.queue.domain.model.dto;

import com.ddaaniel.queue.domain.model.Especialista;
import com.ddaaniel.queue.domain.model.Indisponibilidade;
import com.ddaaniel.queue.domain.model.enuns.TipoEspecialista;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public record EspecialistaRecordDtoResponce(Long id,
                                            String nome,
                                            TipoEspecialista tipoEspecialista,
                                            List<IndisponibilidadeDto> indisponibilidades) {

    public record IndisponibilidadeDto(Long id, LocalDate data) {
        public IndisponibilidadeDto(Indisponibilidade indisponibilidade) {
            this(indisponibilidade.getId(), indisponibilidade.getData());
        }
    }

    public EspecialistaRecordDtoResponce(Especialista especialista) {
        this(especialista.getId(),
                especialista.getNome(),
                especialista.getTipoEspecialista(),
                especialista.getIndisponibilidades()
                        .stream()
                        .map(IndisponibilidadeDto::new)
                        .collect(Collectors.toList()));
    }
}