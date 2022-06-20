package io.github.dk900912.easyexcel.core.support;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import io.github.dk900912.easyexcel.core.annotation.RequestExcel;
import io.github.dk900912.easyexcel.core.annotation.ResponseExcel;
import io.github.dk900912.easyexcel.core.annotation.Sheet;
import io.github.dk900912.easyexcel.core.enumeration.Scene;
import io.github.dk900912.easyexcel.core.exception.UnsatisfiedMethodSignatureException;
import io.github.dk900912.easyexcel.core.listener.EmptyReadListener;
import org.springframework.core.Conventions;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.ValidationAnnotationUtils;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.multipart.MultipartRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;

import static io.github.dk900912.easyexcel.core.enumeration.Scene.TEMPLATE;
import static org.springframework.util.ResourceUtils.CLASSPATH_URL_PREFIX;

/**
 * @author dukui
 */
public class RequestResponseExcelMethodProcessor implements HandlerMethodArgumentResolver,
        HandlerMethodReturnValueHandler {

    private final ResourceLoader resourceLoader;

    public RequestResponseExcelMethodProcessor(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
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
        parameter = parameter.nestedIfOptional();
        Object data = readWithMessageConverters(webRequest, parameter);
        String name = Conventions.getVariableNameForParameter(parameter);

        if (binderFactory != null) {
            WebDataBinder binder = binderFactory.createBinder(webRequest, data, name);
            if (data != null) {
                validateIfApplicable(binder, parameter);
                if (binder.getBindingResult().hasErrors()) {
                    throw new MethodArgumentNotValidException(parameter, binder.getBindingResult());
                }
            }
            if (mavContainer != null) {
                mavContainer.addAttribute(BindingResult.MODEL_KEY_PREFIX + name, binder.getBindingResult());
            }
        }

        return data;
    }

    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType,
                                  ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws Exception {
        // There is no need to render view
        mavContainer.setRequestHandled(true);
        writeWithMessageConverters(returnValue, returnType, webRequest);
    }

    // +----------------------------------------------------------------------------+
    // |                            private method for read                         |
    // +----------------------------------------------------------------------------+

    protected <T> Object readWithMessageConverters(NativeWebRequest webRequest, MethodParameter parameter)
            throws IOException, UnsatisfiedMethodSignatureException {
        HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
        Assert.state(servletRequest != null, "No HttpServletRequest");

        return readWithMessageConverters(servletRequest, parameter);
    }

    protected Object readWithMessageConverters(HttpServletRequest servletRequest, MethodParameter parameter)
            throws IOException, UnsatisfiedMethodSignatureException {
        Class<?> targetClass = getArgParamOrReturnValueClass(parameter);

        RequestExcel requestExcel = parameter.getParameterAnnotation(RequestExcel.class);
        EmptyReadListener<?> emptyReadListener = new EmptyReadListener<>();
        InputStream inputStream;
        if (servletRequest instanceof MultipartRequest) {
            inputStream = ((MultipartRequest) servletRequest)
                    .getMultiFileMap()
                    .values()
                    .stream()
                    .flatMap(Collection::stream)
                    .findFirst()
                    .map(multipartFile -> {
                        try {
                            return multipartFile.getInputStream();
                        } catch (IOException e) {
                            return null;
                        }
                    })
                    .get();
        } else {
            inputStream = servletRequest.getInputStream();
        }
        EasyExcel.read(inputStream, targetClass, emptyReadListener)
                .headRowNumber(requestExcel.headRowNumber())
                .sheet()
                .doRead();

        return emptyReadListener.getData();
    }

    protected void validateIfApplicable(WebDataBinder binder, MethodParameter parameter) {
        Annotation[] annotations = parameter.getParameterAnnotations();
        for (Annotation ann : annotations) {
            Object[] validationHints = ValidationAnnotationUtils.determineValidationHints(ann);
            if (validationHints != null) {
                binder.validate(validationHints);
                break;
            }
        }
    }

    // +----------------------------------------------------------------------------+
    // |                            private method for write                        |
    // +----------------------------------------------------------------------------+

    protected <T> void writeWithMessageConverters(Object value, MethodParameter returnType, NativeWebRequest webRequest)
            throws IOException, HttpMessageNotWritableException, UnsatisfiedMethodSignatureException {

        HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);
        Assert.state(response != null, "No HttpServletResponse");

        Class<?> targetClass = getArgParamOrReturnValueClass(returnType);

        ResponseExcel responseExcel = returnType.getMethodAnnotation(ResponseExcel.class);
        ExcelTypeEnum excelType = responseExcel.suffix();
        String name = responseExcel.name();
        Sheet sheet = responseExcel.sheetName();
        Scene scene = responseExcel.scene();

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        if (TEMPLATE.equals(scene)) {
            response.setHeader("Content-disposition",
                    "attachment;filename=" + URLEncoder.encode(name.substring(name.indexOf("/") + 1), StandardCharsets.UTF_8.name()));
            BufferedInputStream bufferedInputStream =
                    new BufferedInputStream(resourceLoader.getResource(CLASSPATH_URL_PREFIX + name).getInputStream());
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(response.getOutputStream());
            FileCopyUtils.copy(bufferedInputStream, bufferedOutputStream);
        } else {
            response.setHeader("Content-disposition",
                    "attachment;filename=" + URLEncoder.encode(name, StandardCharsets.UTF_8.name()) + excelType.getValue());
            EasyExcel.write(response.getOutputStream(), targetClass)
                    .excelType(excelType)
                    .sheet(sheet.name())
                    .doWrite((Collection<?>) value);
        }
    }

    private Class<?> getArgParamOrReturnValueClass(MethodParameter target) throws UnsatisfiedMethodSignatureException {
        ResolvableType resolvableType = ResolvableType.forMethodParameter(target);
        if (!Collection.class.isAssignableFrom(resolvableType.resolve())) {
            throw new UnsatisfiedMethodSignatureException("Unsatisfied Method Signature");
        }
        ResolvableType generic = resolvableType.getGeneric(0);
        if (Collection.class.isAssignableFrom(generic.resolve()) || Map.class.isAssignableFrom(generic.resolve())) {
            throw new UnsatisfiedMethodSignatureException("Unsatisfied Method Signature");
        }

        return generic.resolve();
    }
}
