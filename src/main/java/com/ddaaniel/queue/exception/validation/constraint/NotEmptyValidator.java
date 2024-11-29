package com.ddaaniel.queue.exception.validation.constraint;

import com.ddaaniel.queue.exception.validation.NotEmpty;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class NotEmptyValidator implements ConstraintValidator<NotEmpty, Object> {

    @Override
    public void initialize(NotEmpty constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }


    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return false; // Não permite nulo
        }

        if (value instanceof String) {
            return !((String) value).trim().isEmpty(); // Verifica se a string não está vazia
        } else if (value instanceof List) {
            return !((List<?>) value).isEmpty(); // Verifica se a lista não está vazia
        } else if (value instanceof Enum<?>) {
            return true; // Enum não precisa de verificação adicional, pois só pode ser não nulo
        }

        return false; // Retorna falso se o tipo não for suportado
    }

    //@Override
    //public boolean isValid(List list, ConstraintValidatorContext constraintValidatorContext) {
    //    return list != null && !list.isEmpty();
    //}
}