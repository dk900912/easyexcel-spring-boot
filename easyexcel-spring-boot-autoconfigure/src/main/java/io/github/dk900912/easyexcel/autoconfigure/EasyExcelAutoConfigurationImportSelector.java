package io.github.dk900912.easyexcel.autoconfigure;

import io.github.dk900912.easyexcel.core.config.RequestMappingHandlerAdapterPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @author dukui
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class EasyExcelAutoConfigurationImportSelector implements ImportAware {

    protected AnnotationAttributes enableEnableEasyExcel;

    @Override
    public void setImportMetadata(AnnotationMetadata importMetadata) {
        this.enableEnableEasyExcel = AnnotationAttributes.fromMap(
                importMetadata.getAnnotationAttributes(EnableEasyExcel.class.getName(), false));
        if (this.enableEnableEasyExcel == null) {
            throw new IllegalArgumentException(
                    "@EnableEasyExcel is not present on importing class " + importMetadata.getClassName());
        }
    }

    @Bean
    public RequestMappingHandlerAdapterPostProcessor requestMappingHandlerAdapterPostProcessor() {
        return new RequestMappingHandlerAdapterPostProcessor();
    }

}
