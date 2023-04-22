package io.github.dk900912.easyexcel.core.support;

import com.alibaba.excel.support.ExcelTypeEnum;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author dukui
 */
public class Constants {

    private Constants() {}

    static final String RESPONSE_EXCEL_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    static final String RESPONSE_EXCEL_CONTENT_DISPOSITION = "Content-disposition";

    static final String RESPONSE_EXCEL_ATTACHMENT = "attachment;filename=";

    static final String EXCEL_TYPE_HEADER = "excel-type";

    static final Set<String> LEGAL_EXCEL_TYPE = Arrays.stream(ExcelTypeEnum.values())
            .map(ExcelTypeEnum::name)
            .collect(Collectors.toSet());

}
