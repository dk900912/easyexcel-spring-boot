package io.github.dk900912.easyexcel.support;

import io.github.dk900912.easyexcel.annotation.RequestExcel;
import io.github.dk900912.easyexcel.annotation.ResponseExcel;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author dukui
 */
public class RequestResponseExcelMethodProcessor implements HandlerMethodArgumentResolver, HandlerMethodReturnValueHandler {

    private static final Object NO_VALUE = new Object();

    private static final MediaType NO_MEDIA_TYPE = null;

    private final List<HttpMessageConverter<?>> messageConverters;

    public RequestResponseExcelMethodProcessor(List<HttpMessageConverter<?>> messageConverters) {
        this.messageConverters = messageConverters;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(RequestExcel.class);
    }

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        return returnType.hasMethodAnnotation(ResponseExcel.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        ExcelHttpInputMessage inputMessage = createInputMessage(webRequest, parameter);
        Object arg = readWithMessageConverters(inputMessage, parameter);
        if (arg == null && checkRequired(parameter)) {
            throw new HttpMessageNotReadableException("Required request excel is missing: " +
                    parameter.getExecutable().toGenericString(), inputMessage);
        }
        return adaptArgumentIfNecessary(arg, parameter);
    }

    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType,
                                  ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws Exception {
        // There is no need to render view
        mavContainer.setRequestHandled(true);
        ExcelHttpOutputMessage outputMessage = createOutputMessage(webRequest, returnType);
        writeWithMessageConverters(returnValue, returnType, outputMessage);
    }

    @SuppressWarnings({"unchecked"})
    protected <T> Object readWithMessageConverters(HttpInputMessage inputMessage, MethodParameter parameter) throws HttpMediaTypeNotSupportedException, HttpMessageNotReadableException {
        ResolvableType resolvableType = ResolvableType.forMethodParameter(parameter);
        Class<T> targetClass = (Class<T>) resolvableType.resolve();

        MediaType contentType;
        boolean noContentType = false;
        try {
            contentType = inputMessage.getHeaders().getContentType();
        } catch (InvalidMediaTypeException ex) {
            throw new HttpMediaTypeNotSupportedException(ex.getMessage());
        }
        if (contentType == null) {
            noContentType = true;
            contentType = MediaType.APPLICATION_OCTET_STREAM;
        }

        Object body = NO_VALUE;
        try {
            for (HttpMessageConverter<?> converter : this.messageConverters) {
                if (converter.canRead(targetClass, contentType)) {
                    body = ((HttpMessageConverter<T>) converter).read(targetClass, inputMessage);
                    break;
                }
            }
        } catch (IOException ex) {
            throw new HttpMessageNotReadableException("I/O error while reading input message", ex, inputMessage);
        } finally {
            // No-op by default: A standard HttpInputMessage exposes the HTTP request stream
            // (ServletRequest#getInputStream), with its lifecycle managed by the container.
        }

        if (body == NO_VALUE) {
            if (noContentType) {
                return null;
            }
            throw new HttpMediaTypeNotSupportedException(contentType,
                    getSupportedMediaTypes(targetClass != null ? targetClass : Object.class));
        }

        return body;
    }

    @SuppressWarnings({"unchecked"})
    protected  <T> void writeWithMessageConverters(Object value, MethodParameter returnType, ExcelHttpOutputMessage outputMessage) {
        Class<?> returnValueType = (value != null ? value.getClass() : returnType.getParameterType());
        MediaType contentType = NO_MEDIA_TYPE;
        try {
            for (HttpMessageConverter<?> converter : this.messageConverters) {
                if (converter.canWrite(returnValueType, contentType)) {
                    ((HttpMessageConverter<T>) converter).write(((T) value), contentType, outputMessage);
                }
            }
        } catch (IOException ex) {
            throw new HttpMessageNotWritableException("I/O error while writing output message", ex);
        }
    }

    protected ExcelHttpInputMessage createInputMessage(NativeWebRequest webRequest, MethodParameter parameter) {
        HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
        Assert.state(servletRequest != null, "No HttpServletRequest");
        return new ExcelHttpInputMessage(servletRequest, parameter);
    }

    protected ExcelHttpOutputMessage createOutputMessage(NativeWebRequest webRequest, MethodParameter parameter) {
        HttpServletResponse servletResponse = webRequest.getNativeResponse(HttpServletResponse.class);
        Assert.state(servletResponse != null, "No HttpServletResponse");
        return new ExcelHttpOutputMessage(servletResponse, parameter);
    }

    protected Object adaptArgumentIfNecessary(@Nullable Object arg, MethodParameter parameter) {
        if (parameter.getParameterType() == Optional.class) {
            if (arg == null || (arg instanceof Collection && ((Collection<?>) arg).isEmpty()) ||
                    (arg instanceof Object[] && ((Object[]) arg).length == 0)) {
                return Optional.empty();
            }
            else {
                return Optional.of(arg);
            }
        }
        return arg;
    }

    protected boolean checkRequired(MethodParameter parameter) {
        RequestExcel requestExcel = parameter.getParameterAnnotation(RequestExcel.class);
        return (requestExcel != null && requestExcel.required() && !parameter.isOptional());
    }

    protected List<MediaType> getSupportedMediaTypes(Class<?> clazz) {
        Set<MediaType> mediaTypeSet = new LinkedHashSet<>();
        for (HttpMessageConverter<?> converter : this.messageConverters) {
            mediaTypeSet.addAll(converter.getSupportedMediaTypes(clazz));
        }
        List<MediaType> result = new ArrayList<>(mediaTypeSet);
        MimeTypeUtils.sortBySpecificity(result);
        return result;
    }
}