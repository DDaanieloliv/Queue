package com.ddaaniel.queue.controller;

import com.ddaaniel.queue.domain.Exception.EspecialistaNotFoundException;
import com.ddaaniel.queue.domain.model.Especialista;
import com.ddaaniel.queue.service.EspecialistaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/especialistas")
public class EspecialistaController {

    @Autowired
    private EspecialistaService especialistaService;

    @GetMapping("/{id}")
    public ResponseEntity<Especialista> getEspecialistaById(@PathVariable Long id) {
        Especialista especialista = especialistaService.findByIdEspecialista(id);
        return ResponseEntity.ok(especialista);
    }


    // Adiciona o tratamento da exceção
    @ExceptionHandler(EspecialistaNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<String> handleEspecialistaNotFound(EspecialistaNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}