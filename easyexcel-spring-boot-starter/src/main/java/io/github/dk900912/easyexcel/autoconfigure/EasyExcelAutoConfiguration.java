package io.github.dk900912.easyexcel.autoconfigure;

import com.alibaba.excel.EasyExcelFactory;
import io.github.dk900912.easyexcel.validation.FailFastValidationConfigurationCustomizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import static io.github.dk900912.easyexcel.autoconfigure.EasyExcelProperties.EASY_EXCEL_PREFIX;

/**
 * @author dukui
 */
@AutoConfiguration
@EnableConfigurationProperties({EasyExcelProperties.class})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({EasyExcelFactory.class})
@ConditionalOnProperty(prefix = EASY_EXCEL_PREFIX, name = "enabled",
        havingValue = "true", matchIfMissing = true)
public class EasyExcelAutoConfiguration {

    @Bean
    public RequestMappingHandlerAdapterCustomizer requestMappingHandlerAdapterPostCustomizer() {
        return new RequestMappingHandlerAdapterCustomizer();
    }

    @ConditionalOnProperty(prefix = EASY_EXCEL_PREFIX, name = "validation.fail-fast",
            havingValue = "true", matchIfMissing = true)
    @Bean
    public FailFastValidationConfigurationCustomizer failFastValidationConfigurationCustomizer() {
        return new FailFastValidationConfigurationCustomizer();
    }

}