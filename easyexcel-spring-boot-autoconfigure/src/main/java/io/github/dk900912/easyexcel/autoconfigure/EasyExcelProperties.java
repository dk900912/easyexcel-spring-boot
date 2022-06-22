package io.github.dk900912.easyexcel.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author dukui
 */
@ConfigurationProperties(prefix = EasyExcelProperties.EASY_EXCEL_PREFIX)
public class EasyExcelProperties {

    public static final String EASY_EXCEL_PREFIX = "spring.easyexcel";

    private Boolean enabled = true;

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
