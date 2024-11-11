package com.ddaaniel.queue.domain.model;



import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
public class Disponibilidade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "especialista_id", nullable = false)
    @JsonIgnore
    private Especialista especialista;

    private LocalDate data;  // Data de disponibilidade

}


