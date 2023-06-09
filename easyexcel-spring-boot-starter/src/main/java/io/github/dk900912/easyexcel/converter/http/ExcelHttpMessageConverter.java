package io.github.dk900912.easyexcel.converter.http;

import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import io.github.dk900912.easyexcel.model.ResponseExcelInfo;
import io.github.dk900912.easyexcel.support.Constants;
import io.github.dk900912.easyexcel.support.ExcelHttpInputMessage;
import io.github.dk900912.easyexcel.support.ExcelHttpOutputMessage;
import io.github.dk900912.easyexcel.support.FileNameGenerator;
import io.github.dk900912.easyexcel.support.SheetDataCollector;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.util.FileCopyUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.alibaba.excel.support.ExcelTypeEnum.CSV;
import static io.github.dk900912.easyexcel.support.Constants.APPLICATION_OCTET_STREAM;
import static io.github.dk900912.easyexcel.support.Constants.APPLICATION_VND_EXCEL;
import static io.github.dk900912.easyexcel.support.Constants.APPLICATION_VND_OFFICE_DOC;
import static io.github.dk900912.easyexcel.support.Constants.MULTIPART_FORM_DATA;
import static io.github.dk900912.easyexcel.support.Constants.TEXT_CSV;
import static io.github.dk900912.easyexcel.support.Scene.NORMAL;
import static io.github.dk900912.easyexcel.support.Scene.TEMPLATE;

/**
 * @author dukui
 */
public class ExcelHttpMessageConverter extends AbstractHttpMessageConverter<List<List<Object>>> {

    private final ResourceLoader resourceLoader;

    private final String templateLocation;

    private final FileNameGenerator fileNameGenerator;

    private static final MediaType[] DEFAULT_MEDIATYPE = new MediaType[]{
            MediaType.valueOf(APPLICATION_OCTET_STREAM),
            MediaType.valueOf(MULTIPART_FORM_DATA),
            MediaType.valueOf(APPLICATION_VND_EXCEL),
            MediaType.valueOf(APPLICATION_VND_OFFICE_DOC),
            MediaType.valueOf(TEXT_CSV)
    };

    public ExcelHttpMessageConverter(ResourceLoader resourceLoader,
                                     String templateLocation,
                                     FileNameGenerator fileNameGenerator,
                                     MediaType... mediaTypes) {
        super(StandardCharsets.UTF_8,
                Stream.of(DEFAULT_MEDIATYPE, mediaTypes)
                        .flatMap(Stream::of)
                        .distinct()
                        .toList()
                        .toArray(new MediaType[]{})
        );
        this.resourceLoader = resourceLoader;
        this.templateLocation = templateLocation;
        this.fileNameGenerator = fileNameGenerator;
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return List.class.isAssignableFrom(clazz)
                || void.class.isAssignableFrom(clazz);
    }

    @Override
    protected List<List<Object>> readInternal(Class<? extends List<List<Object>>> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        ExcelHttpInputMessage excelHttpInputMessage = (ExcelHttpInputMessage) inputMessage;
        if (!acceptable(excelHttpInputMessage.getParameter())) {
            throw new HttpMessageNotReadableException("@RequestExcel method's parameter type must be List<List<>>", inputMessage);
        }
        SheetDataCollector sheetDataCollector = new SheetDataCollector();
        try (ExcelReader excelReader = EasyExcelFactory.read(excelHttpInputMessage.getBody()).build()) {
            excelReader.read(excelHttpInputMessage.getReadSheets(sheetDataCollector));
        }
        return sheetDataCollector.sheetPartition();
    }

    @Override
    protected void writeInternal(List<List<Object>> data, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        ExcelHttpOutputMessage excelHttpOutputMessage = (ExcelHttpOutputMessage) outputMessage;
        ResponseExcelInfo responseExcelInfo = excelHttpOutputMessage.getResponseExcelInfo();
        HttpServletResponse servletResponse = excelHttpOutputMessage.getServletResponse();
        OutputStream outputStream = excelHttpOutputMessage.getBody();
        final String fileName = shouldGenerateFileName(responseExcelInfo) ? doGenerateFileName() : responseExcelInfo.getName();
        servletResponse.setContentType(Constants.RESPONSE_EXCEL_CONTENT_TYPE);
        servletResponse.setCharacterEncoding(StandardCharsets.UTF_8.name());
        if (TEMPLATE.equals(responseExcelInfo.getScene())) {
            servletResponse.setHeader(Constants.RESPONSE_EXCEL_CONTENT_DISPOSITION,
                    Constants.RESPONSE_EXCEL_ATTACHMENT + URLEncoder.encode(
                            fileName.substring(fileName.indexOf("/") + 1), StandardCharsets.UTF_8));
            BufferedInputStream bufferedInputStream =
                    new BufferedInputStream(resourceLoader.getResource(templateLocation + fileName).getInputStream());
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
            FileCopyUtils.copy(bufferedInputStream, bufferedOutputStream);
        } else {
            if (!acceptable(excelHttpOutputMessage.getParameter())) {
                throw new HttpMessageNotWritableException("@ResponseExcel method's returning type must be List<List<>> in normal export mode");
            }
            servletResponse.setHeader(Constants.RESPONSE_EXCEL_CONTENT_DISPOSITION,
                    Constants.RESPONSE_EXCEL_ATTACHMENT + URLEncoder.encode(
                            fileName, StandardCharsets.UTF_8) + responseExcelInfo.getSuffix().getValue());
            List<WriteSheet> writeSheetList = excelHttpOutputMessage.getWriteSheets();
            if (hasRedundantSheet(writeSheetList, data)) {
                throw new HttpMessageNotWritableException("Redundant @Sheet annotation in @ResponseExcel");
            }
            if (CSV == responseExcelInfo.getSuffix()) {
                outputStream.write(new byte[]{(byte) 0xef, (byte) 0xbb, (byte) 0xbf});
            }
            try (ExcelWriter excelWriter = EasyExcelFactory.write(outputStream)
                    .charset(StandardCharsets.UTF_8).excelType(responseExcelInfo.getSuffix()).build()) {
                for (int i = 0; i < writeSheetList.size(); i++) {
                    WriteSheet writeSheet = writeSheetList.get(i);
                    List<Object> singleSheetData = data.get(i);
                    excelWriter.write(singleSheetData, writeSheet);
                }
            }
        }
    }

    private boolean acceptable(MethodParameter parameter) {
        ResolvableType resolvableType = ResolvableType.forMethodParameter(parameter);
        return Optional.<Class<?>>ofNullable(resolvableType.resolve())
                .map(List.class::isAssignableFrom)
                .orElse(false)
                &&
                Optional.<ResolvableType>of(resolvableType.getGeneric(0))
                .map(ResolvableType::resolve)
                .map(List.class::isAssignableFrom)
                .orElse(false);
    }

    private boolean shouldGenerateFileName(ResponseExcelInfo responseExcelInfo) {
        return NORMAL == responseExcelInfo.getScene()
                && StringUtils.isEmpty(responseExcelInfo.getName());
    }

    private String doGenerateFileName() {
        return fileNameGenerator.generateFileName();
    }

    private boolean hasRedundantSheet(List<WriteSheet> writeSheetList, List<List<Object>> data) {
        return writeSheetList.size() > data.size();
    }

}
