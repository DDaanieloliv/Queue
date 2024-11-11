package com.ddaaniel.queue.domain.model;

import com.ddaaniel.queue.domain.model.enuns.Role;
import com.ddaaniel.queue.domain.model.enuns.Sexo;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Random;

@Entity
@Data
public class Cadastramento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_cadastramento;

    private String nomeCompleto;

    private String dataNascimento;

    @Enumerated(EnumType.STRING)
    private Sexo sexo;

    private String telefone;

    private String email;

    @Enumerated(EnumType.STRING)
    private Role role;


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

