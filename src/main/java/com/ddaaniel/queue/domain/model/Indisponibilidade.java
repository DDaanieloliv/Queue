package com.ddaaniel.queue.domain.model;



import com.ddaaniel.queue.exception.validation.NotEmpty;
import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
public class Indisponibilidade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "especialista_id", nullable = false)
    private Especialista especialista;

    @NotEmpty(message = "{campo.indisponibilidade.obrigatorio}")
    private LocalDate data;  // Data de disponibilidade

}


