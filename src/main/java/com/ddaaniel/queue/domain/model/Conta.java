package com.ddaaniel.queue.domain.model;

import com.ddaaniel.queue.domain.model.enuns.Role;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Conta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_conta;

    private String login;

    private String password;

    @Enumerated(EnumType.STRING)
    private Role roleEnum;

    @OneToOne(mappedBy = "conta", cascade = CascadeType.ALL, orphanRemoval = true)
    private Especialista especialista; // Relacionamento com Especialista


    public Conta(String login, String password, Role roleEnum){
        this.login = login;
        this.password = password;
        this.roleEnum = roleEnum;
    }

    public Conta(){}
}
