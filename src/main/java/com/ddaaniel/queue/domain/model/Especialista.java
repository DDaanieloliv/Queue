package com.ddaaniel.queue.domain.model;


import com.ddaaniel.queue.domain.model.enuns.TipoEspecialista;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Especialista {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty
    private String nome;

    @Enumerated(EnumType.STRING)
    @NotNull
    private TipoEspecialista tipoEspecialista;

    @OneToMany(mappedBy = "especialista", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Disponibilidade> disponibilidades = new ArrayList<>();  // Relacionamento com Disponibilidade

    @OneToMany(mappedBy = "especialista", cascade = CascadeType.ALL)
    private List<Agendamento> agendamentos = new ArrayList<>();

    // Getters e Setters
}
