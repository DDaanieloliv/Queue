package com.ddaaniel.queue.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ValidationExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String fieldName = error.getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }


}


/*
A ValidationExceptionHandler é responsável por capturar exceções de validação e formatar as
respostas de erro antes de enviá-las para o cliente. Vamos examinar o que essa classe faz:

    @RestControllerAdvice:
        indica que essa classe lida com exceções em todos os controladores.

Método handleValidationExceptions:

    Este método lida especificamente com a exceção MethodArgumentNotValidException, que é
    disparada quando um campo não passa nas validações declaradas, por exemplo, um @NotEmpty
    aplicado a um campo que foi enviado vazio.

    @ExceptionHandler(MethodArgumentNotValidException.class):
        captura a exceção MethodArgumentNotValidException.

    @ResponseStatus(HttpStatus.BAD_REQUEST):
        define que a resposta HTTP será 400 Bad Request.

    Lógica do método:
        percorre todos os erros de validação (ex.getBindingResult().getAllErrors()) e extrai:

            - fieldName: o nome do campo com erro.

            - errorMessage: a mensagem de erro (por exemplo, "O campo nome é obrigatório").

            - Retorno: devolve uma Map com os nomes dos campos como chaves e as mensagens de
                erro como valores, para que o cliente receba todas as mensagens em uma
                única resposta.


Se removêssemos ValidationExceptionHandler:

A resposta de erro se tornaria uma estrutura mais complexa e menos amigável, pois o Spring
devolveria uma resposta padrão em JSON que não é formatada.

O Spring MVC traria um JSON de erro com informações técnicas adicionais e desnecessárias
para o usuário, o que dificultaria a interpretação das mensagens.
*/