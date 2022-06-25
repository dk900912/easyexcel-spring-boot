package io.github.dk900912.easyexcel.core.support;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.alibaba.excel.write.metadata.WriteSheet;
import io.github.dk900912.easyexcel.core.annotation.RequestExcel;
import io.github.dk900912.easyexcel.core.annotation.ResponseExcel;
import io.github.dk900912.easyexcel.core.exception.ExcelCellContentNotValidException;
import io.github.dk900912.easyexcel.core.exception.UnsatisfiedMethodSignatureException;
import io.github.dk900912.easyexcel.core.listener.CollectorReadListener;
import io.github.dk900912.easyexcel.core.model.RequestExcelInfo;
import io.github.dk900912.easyexcel.core.model.ResponseExcelInfo;
import io.github.dk900912.easyexcel.core.utils.ValidationUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.multipart.MultipartRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

        validateIfNecessary(data, parameter);

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
        validateArgParamOrReturnValueType(parameter);
        HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
        Assert.state(servletRequest != null, "No HttpServletRequest");

        return readWithMessageConverters(servletRequest, parameter);
    }

    protected Object readWithMessageConverters(HttpServletRequest servletRequest, MethodParameter parameter)
            throws IOException {
        RequestExcelInfo requestExcelInfo =
                new RequestExcelInfo(parameter.getParameterAnnotation(RequestExcel.class));
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

        CollectorReadListener collectorReadListener = new CollectorReadListener();
        try (ExcelReader excelReader = EasyExcel.read(inputStream).build()) {
            List<ReadSheet> readSheetList = requestExcelInfo.getSheetInfoList()
                    .stream()
                    .map(sheetInfo -> EasyExcel.readSheet(sheetInfo.getIndex())
                            .head(sheetInfo.getHeadClazz())
                            .registerReadListener(collectorReadListener)
                            .build()
                    )
                    .collect(Collectors.toList());
            excelReader.read(readSheetList);
        }

        return collectorReadListener.groupByHeadClazz();
    }

    protected void validateIfNecessary(Object data, MethodParameter parameter) throws ExcelCellContentNotValidException {
        if (parameter.hasParameterAnnotation(Validated.class)
                || parameter.hasParameterAnnotation(Valid.class)) {
            List<Object> flattenData = ((List<List<Object>>) data).stream()
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
            for (Object target : flattenData) {
                Set<ConstraintViolation<Object>> constraintViolationSet = ValidationUtil.validate(target);
                if (CollectionUtils.isNotEmpty(constraintViolationSet)) {
                    String errorMsg = constraintViolationSet.stream()
                            .map(ConstraintViolation::getMessage)
                            .distinct()
                            .findFirst()
                            .get();
                    throw new ExcelCellContentNotValidException(errorMsg);
                }
            }
        }
    }

    // +----------------------------------------------------------------------------+
    // |                            private method for write                        |
    // +----------------------------------------------------------------------------+

    protected void writeWithMessageConverters(Object value, MethodParameter returnType, NativeWebRequest webRequest)
            throws IOException, HttpMessageNotWritableException, UnsatisfiedMethodSignatureException {

        validateArgParamOrReturnValueType(returnType);

        HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);
        Assert.state(response != null, "No HttpServletResponse");

        ResponseExcelInfo responseExcelInfo =
                new ResponseExcelInfo(returnType.getMethodAnnotation(ResponseExcel.class));
        final String fileName = responseExcelInfo.getName();
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        if (TEMPLATE.equals(responseExcelInfo.getScene())) {
            response.setHeader("Content-disposition",
                    "attachment;filename=" + URLEncoder.encode(
                            fileName.substring(fileName.indexOf("/") + 1), StandardCharsets.UTF_8.name()));
            BufferedInputStream bufferedInputStream =
                    new BufferedInputStream(resourceLoader.getResource(CLASSPATH_URL_PREFIX + fileName).getInputStream());
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(response.getOutputStream());
            FileCopyUtils.copy(bufferedInputStream, bufferedOutputStream);
        } else {
            response.setHeader("Content-disposition",
                    "attachment;filename=" + URLEncoder.encode(
                            fileName, StandardCharsets.UTF_8.name()) + responseExcelInfo.getSuffix().getValue());
            try (ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream()).build()) {
                List<WriteSheet> writeSheetList = responseExcelInfo.getSheetInfoList()
                        .stream()
                        .map(sheetInfo -> EasyExcel.writerSheet(sheetInfo.getName())
                                .head(sheetInfo.getHeadClazz())
                                .build()
                        )
                        .collect(Collectors.toList());
                List<List<Object>> multiSheetData = (List<List<Object>>) value;
                for (int i = 0; i < writeSheetList.size(); i++) {
                    WriteSheet writeSheet = writeSheetList.get(i);
                    List<Object> singleSheetData = multiSheetData.get(i);
                    excelWriter.write(singleSheetData, writeSheet);
                }
            }
        }
    }

    // +----------------------------------------------------------------------------+
    // |                            common private method                           |
    // +----------------------------------------------------------------------------+

    private void validateArgParamOrReturnValueType(MethodParameter target) throws UnsatisfiedMethodSignatureException {
        try {
            ResolvableType resolvableType = ResolvableType.forMethodParameter(target);
            if (!List.class.isAssignableFrom(resolvableType.resolve())) {
                throw new UnsatisfiedMethodSignatureException(
                        "@RequestExcel or @ResponseExcel must be annotated with List<List<>>");
            }
            if (!List.class.isAssignableFrom(resolvableType.getGeneric(0).resolve())) {
                throw new UnsatisfiedMethodSignatureException(
                        "@RequestExcel or @ResponseExcel must be annotated with List<List<>>");
            }
        } catch (Exception exception) {
            throw new UnsatisfiedMethodSignatureException(
                    "@RequestExcel or @ResponseExcel must be annotated with List<List<>>");
        }
    }
}
