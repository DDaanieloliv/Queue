package com.ddaaniel.queue.config;


import com.ddaaniel.queue.domain.model.Conta;
import com.ddaaniel.queue.domain.model.enuns.Role;
import com.ddaaniel.queue.domain.repository.ContaRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class AdminConfig implements CommandLineRunner {

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate; // Adicione o JdbcTemplate


    @Override
    @Transactional
    public void run(String... args) throws Exception {

        // Verificação e inserção de especialistas apenas se não existirem
        if (!especialistaExiste(1)) {
            jdbcTemplate.update("INSERT INTO especialista (id, nome, tipo_especialista) VALUES (1, 'Dr. João da Silva', 'ODONTOLOGO');");
        }

        if (!especialistaExiste(2)) {
            jdbcTemplate.update("INSERT INTO especialista (id, nome, tipo_especialista) VALUES (2, 'Dra. Maria Oliveira', 'ORTOPEDISTA');");
        }

        if (!especialistaExiste(3)) {
            jdbcTemplate.update("INSERT INTO especialista (id, nome, tipo_especialista) VALUES (3, 'Dr. Pedro Santos', 'CARDIOLOGISTA');");
        }

        // Inserção de disponibilidade apenas se não existirem para os especialistas
        if (!disponibilidadeExiste(1, "2023-12-01")) {
            jdbcTemplate.update("INSERT INTO disponibilidade (especialista_id, data) VALUES (1, '2023-12-01');");
        }
        if (!disponibilidadeExiste(1, "2023-12-15")) {
            jdbcTemplate.update("INSERT INTO disponibilidade (especialista_id, data) VALUES (1, '2023-12-15');");
        }
        if (!disponibilidadeExiste(2, "2023-12-10")) {
            jdbcTemplate.update("INSERT INTO disponibilidade (especialista_id, data) VALUES (2, '2023-12-10');");
        }
        if (!disponibilidadeExiste(2, "2023-12-20")) {
            jdbcTemplate.update("INSERT INTO disponibilidade (especialista_id, data) VALUES (2, '2023-12-20');");
        }
        if (!disponibilidadeExiste(3, "2023-12-05")) {
            jdbcTemplate.update("INSERT INTO disponibilidade (especialista_id, data) VALUES (3, '2023-12-05');");
        }
        if (!disponibilidadeExiste(3, "2023-12-25")) {
            jdbcTemplate.update("INSERT INTO disponibilidade (especialista_id, data) VALUES (3, '2023-12-25');");
        }

        /*
        jdbcTemplate.update("INSERT INTO especialista (id, nome, tipo_especialista)\n" +
                "VALUES \n" +
                "    (1, 'Dr. João da Silva', 'ODONTOLOGO'),\n" +
                "    (2, 'Dra. Maria Oliveira', 'ORTOPEDISTA'),\n" +
                "    (3, 'Dr. Pedro Santos', 'CARDIOLOGISTA');");
        jdbcTemplate.update("INSERT INTO disponibilidade (especialista_id, data)\n" +
                "VALUES \n" +
                "    (1, '2023-12-01'),\n" +
                "    (1, '2023-12-15'),\n" +
                "    (2, '2023-12-10'),\n" +
                "    (2, '2023-12-20'),\n" +
                "    (3, '2023-12-05'),\n" +
                "    (3, '2023-12-25');");
         */

        boolean adminExists = contaRepository.findByRoleEnum(Role.ADMIN).isPresent();

        if (!adminExists) {
            // Cria nova conta de administrador
            Conta adminConta = new Conta();
            adminConta.setLogin("admin"); // Defina o login desejado
            adminConta.setPassword("admin"); // Criptografa a senha
            adminConta.setRoleEnum(Role.ADMIN);

            contaRepository.save(adminConta); // Salva a conta no banco de dados

            System.out.println("Conta de administrador criada com sucesso.");
        } else {
            System.out.println("Conta de administrador já existe.");
        }
    }


    // Método para verificar se o especialista já existe
    private boolean especialistaExiste(int id) {
        String sql = "SELECT COUNT(*) FROM especialista WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    // Método para verificar se a disponibilidade já existe
    private boolean disponibilidadeExiste(int especialistaId, String data) {
        String sql = "SELECT COUNT(*) FROM disponibilidade WHERE especialista_id = ? AND data = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, especialistaId, data);
        return count != null && count > 0;
    }
}