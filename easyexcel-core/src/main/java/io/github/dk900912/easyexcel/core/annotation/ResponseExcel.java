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
     * The name of the exported excel.
     *
     * @return The name of the exported excel
     */
    String name();

    /**
     * The format of the exported excel.
     *
     * @return The format of the exported excel
     */
    ExcelTypeEnum suffix() default ExcelTypeEnum.XLSX;

    /**
     * The sheet array of the exported excel.
     *
     * @return The sheet array of the exported excel
     */
    Sheet[] sheets() default {};

    /**
     * The exporting scene.
     *
     * @return The exporting scene
     */
    Scene scene() default NORMAL;
}
