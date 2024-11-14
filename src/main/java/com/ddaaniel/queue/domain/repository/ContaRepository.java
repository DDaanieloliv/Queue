package com.ddaaniel.queue.domain.repository;


import com.ddaaniel.queue.domain.model.Conta;
import com.ddaaniel.queue.domain.model.enuns.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContaRepository extends JpaRepository<Conta, Long> {

    Optional<Conta> findByRoleEnum(Role role);

    Optional<Conta> findByPassword(String password);

    //Optional<Conta> findByCodigoCodigoo(String codigoCodigo);

    //Optional<Conta> findByEmail(String email);
}
