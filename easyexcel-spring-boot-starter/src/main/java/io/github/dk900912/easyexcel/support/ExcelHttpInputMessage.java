package io.github.dk900912.easyexcel.support;

import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.read.metadata.ReadSheet;
import io.github.dk900912.easyexcel.annotation.RequestExcel;
import io.github.dk900912.easyexcel.model.RequestExcelInfo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.web.multipart.MultipartRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

/**
 * @author dukui
 */
public class ExcelHttpInputMessage implements HttpInputMessage {

    private final MethodParameter parameter;

    private final HttpServletRequest servletRequest;

    public ExcelHttpInputMessage(HttpServletRequest servletRequest, MethodParameter parameter) {
        this.servletRequest = servletRequest;
        this.parameter = parameter;
    }

    public MethodParameter getParameter() {
        return parameter;
    }

    public HttpServletRequest getServletRequest() {
        return servletRequest;
    }

    public List<ReadSheet> getReadSheets(SheetDataCollector sheetDataCollector) {
        RequestExcelInfo requestExcelInfo =
                new RequestExcelInfo(parameter.getParameterAnnotation(RequestExcel.class));
        return  requestExcelInfo.getSheetInfoList()
                .stream()
                .map(sheetInfo -> EasyExcelFactory.readSheet(sheetInfo.getIndex())
                        .head(sheetInfo.getHeadClazz())
                        .registerReadListener(sheetDataCollector)
                        .build()
                )
                .toList();
    }

    @Override
    public InputStream getBody() throws IOException {
        InputStream inputStream = null;
        if (servletRequest instanceof MultipartRequest multipartRequest) {
            inputStream = multipartRequest.getMultiFileMap()
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
                    .orElse(InputStream.nullInputStream());
        } else {
            inputStream = servletRequest.getInputStream();
        }
        return inputStream;
    }

    @Override
    public HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        for (Enumeration<?> names = servletRequest.getHeaderNames(); names.hasMoreElements();) {
            String headerName = (String) names.nextElement();
            for (Enumeration<?> headerValues = servletRequest.getHeaders(headerName); headerValues.hasMoreElements();) {
                String headerValue = (String) headerValues.nextElement();
                headers.add(headerName, headerValue);
            }
        }
        return headers;
    }
}
