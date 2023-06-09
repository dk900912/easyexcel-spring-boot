package io.github.dk900912.easyexcel.validation;

import jakarta.validation.Configuration;
import org.springframework.boot.autoconfigure.validation.ValidationConfigurationCustomizer;

/**
 * @author dukui
 */
public class FailFastValidationConfigurationCustomizer implements ValidationConfigurationCustomizer {
    @Override
    public void customize(Configuration<?> configuration) {
        configuration.addProperty("hibernate.validator.fail_fast", "true");
    }
}
