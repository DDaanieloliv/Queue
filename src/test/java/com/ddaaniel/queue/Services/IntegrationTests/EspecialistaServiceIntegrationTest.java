package com.ddaaniel.queue.Services.IntegrationTests;

import com.ddaaniel.queue.domain.model.Especialista;
import com.ddaaniel.queue.domain.model.enuns.Sexo;
import com.ddaaniel.queue.domain.model.enuns.TipoEspecialista;
import com.ddaaniel.queue.domain.repository.EspecialistaRepository;
import com.ddaaniel.queue.service.EspecialistaService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest  // Carrega o contexto completo do Spring Boot
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)  // Usa o banco de dados H2 em memória
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)  // Limpa o contexto após cada teste
public class EspecialistaServiceIntegrationTest {

    @Autowired
    private EspecialistaService especialistaService;

    @Autowired
    private EspecialistaRepository especialistaRepository;

    private Especialista especialista;

    @BeforeEach
    void setUp() {
        // Inicializa um Especialista para o teste
        especialista = new Especialista();
        especialista.setNome("Dr. João");
        especialista.setEmail("joao@exemplo.com");
        especialista.setTipoEspecialista(TipoEspecialista.CARDIOLOGISTA);
        especialista.setSexo(Sexo.MASC);

        // Salva o Especialista no banco de dados em memória (H2)
        especialistaRepository.save(especialista);
    }

    @Test
    void testFindByIdEspecialista_Success() {
        // Busca o especialista salvo pelo ID
        Especialista foundEspecialista = especialistaService.findByIdEspecialista(especialista.getId());

        // Verifica se o especialista foi encontrado e se os valores estão corretos
        assertNotNull(foundEspecialista);
        assertEquals(especialista.getId(), foundEspecialista.getId());
        assertEquals("Dr. João", foundEspecialista.getNome());
        assertEquals("joao@exemplo.com", foundEspecialista.getEmail());
        assertEquals(TipoEspecialista.CARDIOLOGISTA, foundEspecialista.getTipoEspecialista());
        assertEquals(Sexo.MASC, foundEspecialista.getSexo());
    }

    @Test
    void testFindByIdEspecialista_NotFound() {
        // Verifica se o método lança uma exceção quando o especialista não é encontrado
        Exception exception = assertThrows(RuntimeException.class, () -> {
            especialistaService.findByIdEspecialista(999L);  // ID inexistente
        });

        String expectedMessage = "Especialista não encontrado para o ID: 999";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }
}