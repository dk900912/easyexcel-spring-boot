package io.github.dk900912.easyexcel.core.support;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.metadata.WriteSheet;
import io.github.dk900912.easyexcel.core.annotation.RequestExcel;
import io.github.dk900912.easyexcel.core.annotation.ResponseExcel;
import io.github.dk900912.easyexcel.core.exception.ExcelCellContentNotValidException;
import io.github.dk900912.easyexcel.core.exception.ExcelNotReadableException;
import io.github.dk900912.easyexcel.core.exception.ExcelNotWritableException;
import io.github.dk900912.easyexcel.core.exception.MissingExcelTypeInHeaderException;
import io.github.dk900912.easyexcel.core.exception.UnsatisfiedMethodSignatureException;
import io.github.dk900912.easyexcel.core.exception.UnsupportedExcelTypeInHeaderException;
import io.github.dk900912.easyexcel.core.listener.CollectorReadListener;
import io.github.dk900912.easyexcel.core.model.RequestExcelInfo;
import io.github.dk900912.easyexcel.core.model.ResponseExcelInfo;
import io.github.dk900912.easyexcel.core.utils.ValidationUtil;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.multipart.MultipartRequest;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.alibaba.excel.support.ExcelTypeEnum.CSV;
import static io.github.dk900912.easyexcel.core.enumeration.Scene.TEMPLATE;
import static io.github.dk900912.easyexcel.core.support.Constants.EXCEL_TYPE_HEADER;
import static io.github.dk900912.easyexcel.core.support.Constants.LEGAL_EXCEL_TYPE;
import static io.github.dk900912.easyexcel.core.support.Constants.RESPONSE_EXCEL_ATTACHMENT;
import static io.github.dk900912.easyexcel.core.support.Constants.RESPONSE_EXCEL_CONTENT_DISPOSITION;
import static io.github.dk900912.easyexcel.core.support.Constants.RESPONSE_EXCEL_CONTENT_TYPE;
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

    protected Object readWithMessageConverters(NativeWebRequest webRequest, MethodParameter parameter)
            throws IOException, UnsatisfiedMethodSignatureException, ExcelNotReadableException,
            UnsupportedExcelTypeInHeaderException, MissingExcelTypeInHeaderException {

        validateArgParamOrReturnValueType(parameter);
        HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
        Assert.state(servletRequest != null, "No HttpServletRequest");

        return readWithMessageConverters(servletRequest, parameter);
    }

    protected Object readWithMessageConverters(HttpServletRequest servletRequest, MethodParameter parameter)
            throws IOException, ExcelNotReadableException, UnsupportedExcelTypeInHeaderException, MissingExcelTypeInHeaderException {

        ExcelTypeEnum excelTypeEnum = getExcelTypeFromHeader(servletRequest);

        RequestExcelInfo requestExcelInfo =
                new RequestExcelInfo(parameter.getParameterAnnotation(RequestExcel.class));
        InputStream inputStream;
        if (servletRequest instanceof MultipartRequest) {
            // Since Java 11, you could construct an empty InputStream by InputStream.nullInputStream()
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
                    .orElse(new ByteArrayInputStream(new byte[0]));
        } else {
            inputStream = servletRequest.getInputStream();
        }

        CollectorReadListener collectorReadListener = new CollectorReadListener();
        try (ExcelReader excelReader = EasyExcel.read(inputStream).excelType(excelTypeEnum).build()) {
            List<ReadSheet> readSheetList = requestExcelInfo.getSheetInfoList()
                    .stream()
                    .map(sheetInfo -> EasyExcel.readSheet(sheetInfo.getIndex())
                            .head(sheetInfo.getHeadClazz())
                            .registerReadListener(collectorReadListener)
                            .build()
                    )
                    .collect(Collectors.toList());
            excelReader.read(readSheetList);
        } catch (Exception exception) {
            throw new ExcelNotReadableException(exception.getMessage());
        }

        return collectorReadListener.sheetPartition();
    }

    protected void validateIfNecessary(Object data, MethodParameter parameter) throws ExcelCellContentNotValidException {
        if (parameter.hasParameterAnnotation(Validated.class)
                || parameter.hasParameterAnnotation(Valid.class)) {
            @SuppressWarnings("unchecked")
            List<Object> flattenData = ((List<List<Object>>) data).stream()
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
            for (Object target : flattenData) {
                Set<ConstraintViolation<Object>> constraintViolationSet = ValidationUtil.validate(target);
                if (CollectionUtils.isNotEmpty(constraintViolationSet)) {
                    String errorMsg = constraintViolationSet.stream()
                            .map(ConstraintViolation::getMessage)
                            .distinct()
                            .iterator()
                            .next();
                    throw new ExcelCellContentNotValidException(errorMsg);
                }
            }
        }
    }

    // +----------------------------------------------------------------------------+
    // |                            private method for write                        |
    // +----------------------------------------------------------------------------+

    protected void writeWithMessageConverters(Object value, MethodParameter returnType, NativeWebRequest webRequest)
            throws IOException, UnsatisfiedMethodSignatureException, ExcelNotWritableException {

        HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);
        Assert.state(response != null, "No HttpServletResponse");
        ServletOutputStream servletOutputStream = response.getOutputStream();

        ResponseExcelInfo responseExcelInfo =
                new ResponseExcelInfo(returnType.getMethodAnnotation(ResponseExcel.class));
        final String fileName = responseExcelInfo.getName();
        response.setContentType(RESPONSE_EXCEL_CONTENT_TYPE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        if (TEMPLATE.equals(responseExcelInfo.getScene())) {
            response.setHeader(RESPONSE_EXCEL_CONTENT_DISPOSITION,
                    RESPONSE_EXCEL_ATTACHMENT + URLEncoder.encode(
                            fileName.substring(fileName.indexOf("/") + 1), StandardCharsets.UTF_8.name()));
            BufferedInputStream bufferedInputStream =
                    new BufferedInputStream(resourceLoader.getResource(CLASSPATH_URL_PREFIX + fileName).getInputStream());
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(response.getOutputStream());
            FileCopyUtils.copy(bufferedInputStream, bufferedOutputStream);
        } else {
            Assert.notNull(value, "The method annotated with @ResponseExcel can not return null in non-template mode");
            validateArgParamOrReturnValueType(returnType);

            response.setHeader(RESPONSE_EXCEL_CONTENT_DISPOSITION,
                    RESPONSE_EXCEL_ATTACHMENT + URLEncoder.encode(
                            fileName, StandardCharsets.UTF_8.name()) + responseExcelInfo.getSuffix().getValue());

            if (CSV == responseExcelInfo.getSuffix()) {
                servletOutputStream.write(new byte[]{(byte) 0xef, (byte) 0xbb, (byte) 0xbf});
            }

            try (ExcelWriter excelWriter = EasyExcel.write(servletOutputStream)
                    .charset(StandardCharsets.UTF_8).excelType(responseExcelInfo.getSuffix()).build()) {
                List<WriteSheet> writeSheetList = responseExcelInfo.getSheetInfoList()
                        .stream()
                        .map(sheetInfo -> EasyExcel.writerSheet(sheetInfo.getName())
                                .head(sheetInfo.getHeadClazz())
                                .build()
                        )
                        .collect(Collectors.toList());
                @SuppressWarnings("unchecked")
                List<List<Object>> multiSheetData = (List<List<Object>>) value;

                Assert.isTrue(writeSheetList.size() <= multiSheetData.size(), "Redundant @Sheet annotation in @ResponseExcel");

                for (int i = 0; i < writeSheetList.size(); i++) {
                    WriteSheet writeSheet = writeSheetList.get(i);
                    List<Object> singleSheetData = multiSheetData.get(i);
                    excelWriter.write(singleSheetData, writeSheet);
                }
            } catch (Exception exception) {
                throw new ExcelNotWritableException(exception.getMessage());
            }
        }
    }

    // +----------------------------------------------------------------------------+
    // |                            common private method                           |
    // +----------------------------------------------------------------------------+

    private void validateArgParamOrReturnValueType(MethodParameter target) throws UnsatisfiedMethodSignatureException {
        ResolvableType resolvableType = ResolvableType.forMethodParameter(target);
        boolean outerList = Optional.<Class<?>>ofNullable(resolvableType.resolve())
                .map(List.class::isAssignableFrom)
                .orElse(false);
        if (!outerList) {
            throw new UnsatisfiedMethodSignatureException(
                    "@RequestExcel or @ResponseExcel must be annotated with List<List<>>");
        }
        boolean innerList = Optional.<ResolvableType>of(resolvableType.getGeneric(0))
                .map(ResolvableType::resolve)
                .map(List.class::isAssignableFrom)
                .orElse(false);
        if (!innerList) {
            throw new UnsatisfiedMethodSignatureException(
                    "@RequestExcel or @ResponseExcel must be annotated with List<List<>>");
        }
    }

    private ExcelTypeEnum getExcelTypeFromHeader(HttpServletRequest servletRequest)
            throws MissingExcelTypeInHeaderException, UnsupportedExcelTypeInHeaderException {
        String excelType = servletRequest.getHeader(EXCEL_TYPE_HEADER);
        if (StringUtils.isEmpty(excelType)) {
            throw new MissingExcelTypeInHeaderException("The excel-type was absent in request header");
        }
        if (!LEGAL_EXCEL_TYPE.contains(excelType.toUpperCase())) {
            throw new UnsupportedExcelTypeInHeaderException("Illegal excel-type in request header");
        }
        return ExcelTypeEnum.valueOf(excelType.toUpperCase());
    }
}
