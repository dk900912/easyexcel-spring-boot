package io.github.dk900912.easyexcel.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.ArrayList;
import java.util.List;

import static io.github.dk900912.easyexcel.support.Constants.DEFAULT_FILE_NAME_GENERATOR;
import static org.springframework.core.io.ResourceLoader.CLASSPATH_URL_PREFIX;

/**
 * @author dukui
 */
@ConfigurationProperties(prefix = EasyExcelProperties.EASY_EXCEL_PREFIX)
public class EasyExcelProperties {

    public static final String EASY_EXCEL_PREFIX = "spring.easy-excel";

    private Boolean enabled = true;

    @NestedConfigurationProperty
    private Name name = new Name();

    @NestedConfigurationProperty
    private Template template = new Template();

    @NestedConfigurationProperty
    private Converter converter = new Converter();

    @NestedConfigurationProperty
    private Validation validation = new Validation();

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Name getName() {
        return name;
    }

    public void setName(Name name) {
        this.name = name;
    }

    public Template getTemplate() {
        return template;
    }

    public void setTemplate(Template template) {
        this.template = template;
    }

    public Converter getConverter() {
        return converter;
    }

    public void setConverter(Converter converter) {
        this.converter = converter;
    }

    public Validation getValidation() {
        return validation;
    }

    public void setValidation(Validation validation) {
        this.validation = validation;
    }

    public static class Name {

        private String generator = DEFAULT_FILE_NAME_GENERATOR;

        public String getGenerator() {
            return generator;
        }

        public void setGenerator(String generator) {
            this.generator = generator;
        }
    }

    public static class Template {

        private String location = CLASSPATH_URL_PREFIX;

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }
    }

    public static class Converter {

        private List<String> mediaTypes = new ArrayList<>();

        public List<String> getMediaTypes() {
            return mediaTypes;
        }

        public void setMediaTypes(List<String> mediaTypes) {
            this.mediaTypes = mediaTypes;
        }
    }

    public static class Validation {

        private Boolean failFast = true;

        public Boolean getFailFast() {
            return failFast;
        }

        public void setFailFast(Boolean failFast) {
            this.failFast = failFast;
        }
    }

}
