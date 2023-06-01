package io.github.dk900912.easyexcel.converter.excel;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * @author dukui
 */
public class LocalDateStringConverter implements Converter<LocalDate> {

    /**
     * Thread-safe
     */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public Class<?> supportJavaTypeKey() {
        return LocalDate.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    @Override
    public LocalDate convertToJavaData(ReadCellData<?> cellData,
                                       ExcelContentProperty contentProperty,
                                       GlobalConfiguration globalConfiguration) throws Exception {
        if (!Objects.equals(CellDataTypeEnum.STRING, cellData.getType())) {
            throw new UnsupportedOperationException(
                    "The cell data type is not supported by the current converter");
        }
        return DATE_TIME_FORMATTER.parse(cellData.getStringValue(), LocalDate::from);
    }

    @Override
    public WriteCellData<?> convertToExcelData(LocalDate value,
                                               ExcelContentProperty contentProperty,
                                               GlobalConfiguration globalConfiguration) throws Exception {
        return new WriteCellData<>(DATE_TIME_FORMATTER.format(value));
    }
}
