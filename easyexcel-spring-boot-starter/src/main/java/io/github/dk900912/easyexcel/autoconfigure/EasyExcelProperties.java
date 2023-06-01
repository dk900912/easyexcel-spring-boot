package io.github.dk900912.easyexcel.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.core.io.ResourceLoader.CLASSPATH_URL_PREFIX;

/**
 * @author dukui
 */
@ConfigurationProperties(prefix = EasyExcelProperties.EASY_EXCEL_PREFIX)
public class EasyExcelProperties {

    public static final String EASY_EXCEL_PREFIX = "spring.easy-excel";

    private Boolean enabled = true;

    @NestedConfigurationProperty
    private Template template = new Template();

    @NestedConfigurationProperty
    private Converter converter = new Converter();

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
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
}
