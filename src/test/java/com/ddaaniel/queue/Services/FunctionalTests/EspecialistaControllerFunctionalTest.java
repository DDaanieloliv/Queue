package com.ddaaniel.queue.Services.FunctionalTests;

import com.ddaaniel.queue.controller.EspecialistaController;
import com.ddaaniel.queue.domain.Exception.EspecialistaNotFoundException;
import com.ddaaniel.queue.domain.model.Especialista;
import com.ddaaniel.queue.domain.model.enuns.Sexo;
import com.ddaaniel.queue.domain.model.enuns.TipoEspecialista;
import com.ddaaniel.queue.service.EspecialistaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.mockito.Mockito.*;

@WebMvcTest(EspecialistaController.class)  // Testa apenas o controlador
public class EspecialistaControllerFunctionalTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EspecialistaService especialistaService;

    private Especialista especialista;

    @BeforeEach
    void setUp() {
        // Inicializa um Especialista para o teste
        especialista = new Especialista();
        especialista.setId(1L);
        especialista.setNome("Dr. João");
        especialista.setEmail("joao@exemplo.com");
        especialista.setTipoEspecialista(TipoEspecialista.CARDIOLOGISTA);
        especialista.setSexo(Sexo.MASC);
    }

    @Test
    void testGetEspecialistaById_Success() throws Exception {
        // Configura o comportamento do serviço
        when(especialistaService.findByIdEspecialista(1L)).thenReturn(especialista);

        // Faz uma requisição GET para /especialistas/1
        mockMvc.perform(get("/especialistas/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())  // Verifica se o status HTTP é 200 (OK)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))  // Verifica se o tipo de conteúdo é JSON
                .andExpect(jsonPath("$.id").value(1L))  // Verifica se o campo "id" é 1
                .andExpect(jsonPath("$.nome").value("Dr. João"))  // Verifica o campo "nome"
                .andExpect(jsonPath("$.email").value("joao@exemplo.com"))  // Verifica o campo "email"
                .andExpect(jsonPath("$.tipoEspecialista").value("CARDIOLOGISTA"))  // Verifica o campo "tipoEspecialista"
                .andExpect(jsonPath("$.sexo").value("MASC"));  // Verifica o campo "sexo"

        // Verifica se o serviço foi chamado corretamente
        verify(especialistaService, times(1)).findByIdEspecialista(1L);
    }

    @Test
    void testGetEspecialistaById_NotFound() throws Exception {
        // Configura o serviço para lançar a exceção EspecialistaNotFoundException
        when(especialistaService.findByIdEspecialista(999L)).thenThrow(new EspecialistaNotFoundException("Especialista não encontrado para o ID: 999"));

        // Faz uma requisição GET para /especialistas/999
        mockMvc.perform(get("/especialistas/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())  // Verifica se o status HTTP é 404 (Not Found)
                .andExpect(content().string("Especialista não encontrado para o ID: 999"));  // Verifica a mensagem de erro

        // Verifica se o serviço foi chamado corretamente
        verify(especialistaService, times(1)).findByIdEspecialista(999L);
    }
}
