package com.ddaaniel.queue.Services.UnitaryTests;

import com.ddaaniel.queue.domain.model.Especialista;
import com.ddaaniel.queue.domain.repository.EspecialistaRepository;
import com.ddaaniel.queue.service.EspecialistaService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class EspecialistaServiceUnitaryTest {

    @InjectMocks
    private EspecialistaService especialistaService;

    @Mock
    private EspecialistaRepository especialistaRepository;

    @Test
    void deveRetornarEspecialistaQuandoEncontrado() {
        Especialista especialistaMock = new Especialista();
        especialistaMock.setId(1L);
        especialistaMock.setNome("Dr. João");
        //  Um mock do objeto Especialista é criado com ID e nome configurados para simular
        //  um retorno esperado.

        Mockito.when(especialistaRepository.findById(1L))
                .thenReturn(Optional.of(especialistaMock));
        //  Quando o método findById(1L) for chamado, ele retornará oque configuramos no mock.

        Especialista resultado = especialistaService.findByIdEspecialista(1L);
        //  Executamos o metodo a ser testado com os mocks.

        Assertions.assertNotNull(resultado);
        Assertions.assertEquals("Dr. João", resultado.getNome());
    }

    @Test
    void deveLancarExcecaoQuandoEspecialistaNaoEncontrado() {
        Mockito.when(especialistaRepository.findById(1L))
                .thenReturn(Optional.empty());
        //  Configuramos o comportamento do metodo interno do findByIdEspecialista.

        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> {
            especialistaService.findByIdEspecialista(1L);
        });
        //  Verifica se o método findByIdEspecialista lança uma RuntimeException ao não encontrar o
        //  especialista.

        Assertions.assertEquals("Especialista não encontrado para o ID: 1", exception.getMessage());
    }
}