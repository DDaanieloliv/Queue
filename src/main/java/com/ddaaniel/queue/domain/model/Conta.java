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

    public Conta(String login, String password){
        this.login = login;
        this.password = password;
    }

    public Conta(){}
}
