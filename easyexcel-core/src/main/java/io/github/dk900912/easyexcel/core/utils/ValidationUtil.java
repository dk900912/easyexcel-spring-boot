package io.github.dk900912.easyexcel.core.utils;


import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import java.util.Set;

/**
 * @author dukui
 */
public class ValidationUtil {

    private ValidationUtil() {
    }

    public static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    public static <T> Set<ConstraintViolation<T>> validate(T target) {
        return VALIDATOR.validate(target);
    }
}
