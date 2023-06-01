package io.github.dk900912.easyexcel.support;

import io.github.dk900912.easyexcel.autoconfigure.EasyExcelProperties;
import io.github.dk900912.easyexcel.converter.http.ExcelHttpMessageConverter;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author dukui
 */
public class RequestMappingHandlerAdapterCustomizer implements BeanPostProcessor, PriorityOrdered, ResourceLoaderAware, ApplicationContextAware {

    private ResourceLoader resourceLoader;

    private ApplicationContext applicationContext;

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (!supports(bean)) {
            return bean;
        }
        return customizeRequestMappingHandlerAdapter(getEasyExcelProperties(), bean);
    }

    private boolean supports(Object bean) {
        return bean instanceof RequestMappingHandlerAdapter;
    }

    private RequestMappingHandlerAdapter customizeRequestMappingHandlerAdapter(EasyExcelProperties easyExcelProperties, Object bean) {
        String templateLocation = getTemplateLocation(easyExcelProperties);
        List<MediaType> extensibleMediaTypeList = getExtensibleMediaTypeList(easyExcelProperties);
        return doCustomizeRequestMappingHandlerAdapter(bean, templateLocation, extensibleMediaTypeList);
    }

    private EasyExcelProperties getEasyExcelProperties() {
        return applicationContext.getBean(EasyExcelProperties.class);
    }

    private String getTemplateLocation(EasyExcelProperties easyExcelProperties) {
        return easyExcelProperties.getTemplate().getLocation();
    }

    private RequestMappingHandlerAdapter doCustomizeRequestMappingHandlerAdapter(Object bean, String templateLocation, List<MediaType> extensibleMediaTypeList) {
        RequestMappingHandlerAdapter requestMappingHandlerAdapter = (RequestMappingHandlerAdapter) bean;
        List<HandlerMethodArgumentResolver> argumentResolvers = requestMappingHandlerAdapter.getArgumentResolvers();
        List<HandlerMethodReturnValueHandler> returnValueHandlers = requestMappingHandlerAdapter.getReturnValueHandlers();

        MediaType[] supportedMediaTypes = CollectionUtils.isEmpty(extensibleMediaTypeList) ? new MediaType[]{} : extensibleMediaTypeList.toArray(new MediaType[]{});
        ExcelHttpMessageConverter excelHttpMessageConverter =
                new ExcelHttpMessageConverter(resourceLoader, templateLocation, supportedMediaTypes);
        RequestResponseExcelMethodProcessor requestResponseExcelMethodProcessor
                = new RequestResponseExcelMethodProcessor(List.of(excelHttpMessageConverter));

        List<HandlerMethodArgumentResolver> copyArgumentResolvers = new ArrayList<>(argumentResolvers);
        copyArgumentResolvers.add(0, requestResponseExcelMethodProcessor);
        requestMappingHandlerAdapter.setArgumentResolvers(Collections.unmodifiableList(copyArgumentResolvers));

        List<HandlerMethodReturnValueHandler> copyReturnValueHandlers = new ArrayList<>(returnValueHandlers);
        copyReturnValueHandlers.add(0, requestResponseExcelMethodProcessor);
        requestMappingHandlerAdapter.setReturnValueHandlers(Collections.unmodifiableList(copyReturnValueHandlers));

        return requestMappingHandlerAdapter;
    }

    private List<MediaType> getExtensibleMediaTypeList(EasyExcelProperties easyExcelProperties) {
        return easyExcelProperties.getConverter()
                .getMediaTypes()
                .stream()
                .map(MediaType::valueOf)
                .toList();
    }
}
