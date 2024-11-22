package com.ddaaniel.queue.Services.PerformanceTest;

import com.ddaaniel.queue.domain.model.Especialista;
import com.ddaaniel.queue.domain.repository.EspecialistaRepository;
import com.ddaaniel.queue.service.EspecialistaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

public class EspecialistaServicePerformanceTest {

    @Mock
    private EspecialistaRepository especialistaRepository;

    @InjectMocks
    private EspecialistaService especialistaService;

    private List<Especialista> especialistaList;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Simula uma lista de especialistas para o teste
        especialistaList = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {  // Simula 1000 especialistas para testar desempenho
            Especialista especialista = new Especialista();
            especialista.setId((long) i);
            especialista.setNome("Especialista " + i);
            especialistaList.add(especialista);
        }
    }

    @Test
    void testFindAllEspecialistasPerformance() {
        // Simula o retorno da página a partir do repositório
        Pageable pageable = PageRequest.of(0, 100);
        Page<Especialista> page = new PageImpl<>(especialistaList, pageable, especialistaList.size());
        when(especialistaRepository.findAll(pageable)).thenReturn(page);

        // Medir o tempo de início
        long start = System.nanoTime();

        // Executa o método a ser testado
        especialistaService.findAllEspecialistas(0, 100);

        // Medir o tempo de término
        long end = System.nanoTime();

        // Calcula o tempo de execução em milissegundos
        long durationInMilliseconds = (end - start) / 1_000_000;

        System.out.println("Tempo de execução: " + durationInMilliseconds + " ms");

        // Verifica se o método foi chamado uma vez
        verify(especialistaRepository, times(1)).findAll(pageable);
    }
}
