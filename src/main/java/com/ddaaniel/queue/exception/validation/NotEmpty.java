package com.ddaaniel.queue.exception.validation;

import com.ddaaniel.queue.exception.validation.constraint.NotEmptyValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(value = RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Constraint(validatedBy = NotEmptyValidator.class)
public @interface NotEmpty {

    String message() default "A lista não pode ser vazia.";

    Class<?>[] groups() default { };
    Class<? extends Payload>[] payload() default { };

}
