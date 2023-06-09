package io.github.dk900912.easyexcel.support;

import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.write.metadata.WriteSheet;
import io.github.dk900912.easyexcel.annotation.ResponseExcel;
import io.github.dk900912.easyexcel.model.ResponseExcelInfo;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpOutputMessage;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * @author dukui
 */
public class ExcelHttpOutputMessage implements HttpOutputMessage {

    private final MethodParameter parameter;

    private final HttpServletResponse servletResponse;

    public ExcelHttpOutputMessage(HttpServletResponse servletResponse, MethodParameter parameter) {
        this.parameter = parameter;
        this.servletResponse = servletResponse;
    }

    public MethodParameter getParameter() {
        return parameter;
    }

    public HttpServletResponse getServletResponse() {
        return servletResponse;
    }

    public ResponseExcelInfo getResponseExcelInfo() {
        return new ResponseExcelInfo(parameter.getMethodAnnotation(ResponseExcel.class));
    }

    public List<WriteSheet> getWriteSheets() {
        ResponseExcelInfo responseExcelInfo = getResponseExcelInfo();
        return responseExcelInfo.getSheetInfoList()
                .stream()
                .map(sheetInfo -> EasyExcelFactory.writerSheet(sheetInfo.getName())
                        .head(sheetInfo.getHeadClazz())
                        .build()
                )
                .toList();
    }

    @Override
    public OutputStream getBody() throws IOException {
        return servletResponse.getOutputStream();
    }

    @Override
    public HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        for (String name : servletResponse.getHeaderNames()) {
            String value = servletResponse.getHeader(name);
            headers.add(name, value);
        }
        return headers;
    }
}
