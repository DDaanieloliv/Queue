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
public class EspecialistaServiceTest {

    @InjectMocks
    private EspecialistaService especialistaService;

    @Mock
    private EspecialistaRepository especialistaRepository;

    @Test
    void deveRetornarEspecialistaQuandoEncontrado() {
        Especialista especialistaMock = new Especialista();
        especialistaMock.setId(1L);
        especialistaMock.setNome("Dr. João");

        Mockito.when(especialistaRepository.findById(1L))
                .thenReturn(Optional.of(especialistaMock));

        Especialista resultado = especialistaService.findByIdEspecialista(1L);

        Assertions.assertNotNull(resultado);
        Assertions.assertEquals("Dr. João", resultado.getNome());
    }

    @Test
    void deveLancarExcecaoQuandoEspecialistaNaoEncontrado() {
        Mockito.when(especialistaRepository.findById(1L))
                .thenReturn(Optional.empty());

        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> {
            especialistaService.findByIdEspecialista(1L);
        });

        Assertions.assertEquals("Especialista não encontrado para o ID: 1", exception.getMessage());
    }
}