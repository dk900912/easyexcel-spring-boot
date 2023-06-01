package io.github.dk900912.easyexcel.converter.excel;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * @author dukui
 */
public class EnumerationNameStringConverter implements Converter<Enum<?>> {

    @Override
    public Class<?> supportJavaTypeKey() {
        return Enum.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Enum<?> convertToJavaData(ReadCellData<?> cellData,
                                     ExcelContentProperty contentProperty,
                                     GlobalConfiguration globalConfiguration) throws Exception {
        if (!Objects.equals(CellDataTypeEnum.STRING, cellData.getType())) {
            throw new UnsupportedOperationException(
                    "The cell data type is not supported by the current converter");
        }

        if (StringUtils.isBlank(cellData.getStringValue())) {
            return null;
        }

        return Enum.valueOf((Class<Enum>) contentProperty.getField().getType(), cellData.getStringValue());
    }

    @Override
    public WriteCellData<?> convertToExcelData(Enum<?> value,
                                               ExcelContentProperty contentProperty,
                                               GlobalConfiguration globalConfiguration) throws Exception {
        return new WriteCellData<>(value.name());
    }
}
