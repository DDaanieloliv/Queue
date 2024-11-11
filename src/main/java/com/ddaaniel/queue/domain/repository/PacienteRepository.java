package com.ddaaniel.queue.domain.repository;


import com.ddaaniel.queue.domain.model.Conta;
import com.ddaaniel.queue.domain.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {
    Optional<Paciente> findByCpf(String cpf); // Podemos buscar pacientes pelo CPF também
    // Verifica se já existe um paciente com o código específico
    boolean existsByCodigoCodigo(String codigoCodigo);

    Optional<Paciente> findByCodigoCodigo(String codigoCodigo);


    Optional<Paciente> findByEmail(String email);

}
