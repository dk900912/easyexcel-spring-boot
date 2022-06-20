package io.github.dk900912.easyexcel.core.annotation;

import com.alibaba.excel.support.ExcelTypeEnum;
import io.github.dk900912.easyexcel.core.enumeration.Scene;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static io.github.dk900912.easyexcel.core.enumeration.Scene.NORMAL;

/**
 * @author dukui
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ResponseExcel {

    /**
     * 导出文件名称
     */
    String name();

    /**
     * 导出文件格式
     */
    ExcelTypeEnum suffix() default ExcelTypeEnum.XLSX;

    /**
     * 导出文件中表格名称
     */
    Sheet sheetName() default @Sheet;

    /**
     * 导出模板或者常规导出
     */
    Scene scene() default NORMAL;
}
