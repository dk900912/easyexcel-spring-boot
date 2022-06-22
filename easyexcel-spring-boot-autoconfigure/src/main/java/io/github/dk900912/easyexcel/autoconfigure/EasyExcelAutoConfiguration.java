package io.github.dk900912.easyexcel.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author dukui
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(EasyExcelProperties.class)
@AutoConfigureAfter(WebMvcAutoConfiguration.class)
public class EasyExcelAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @EnableEasyExcel
    @ConditionalOnProperty(prefix = EasyExcelProperties.EASY_EXCEL_PREFIX, name = "enabled",
            havingValue = "true", matchIfMissing = true)
    public static class EnableEasyExcelConfiguration {

    }

}
