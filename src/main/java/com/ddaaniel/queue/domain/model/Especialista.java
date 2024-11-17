package com.ddaaniel.queue.domain.model;


import com.ddaaniel.queue.domain.model.enuns.Sexo;
import com.ddaaniel.queue.domain.model.enuns.TipoEspecialista;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

    private String email;

    @Enumerated(EnumType.STRING)
    private Sexo sexo;

    @OneToMany(mappedBy = "especialista", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Indisponibilidade> indisponibilidades = new ArrayList<>();  // Relacionamento com Indisponibilidade

    @OneToMany(mappedBy = "especialista", cascade = CascadeType.ALL)
    private List<Agendamento> agendamentos = new ArrayList<>();


    @OneToOne
    @JoinColumn(name = "conta_id", unique = true) // Relacionamento com Conta
    private Conta conta;


    private String codigoCodigo;

    // Gera o código aleatório de 5 caracteres (letras e números)
    public void gerarCodigoCodigo() {
        this.codigoCodigo = gerarCodigo();
    }




    // Função privada para gerar um código de 5 caracteres
    private String gerarCodigo() {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder codigo = new StringBuilder();
        for (int i = 0; i < 8; i++) { // Alterado para gerar 8 caracteres
            codigo.append(caracteres.charAt(random.nextInt(caracteres.length())));
        }
        return codigo.toString();
    }

}
