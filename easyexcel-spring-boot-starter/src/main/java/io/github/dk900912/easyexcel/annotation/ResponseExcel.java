package io.github.dk900912.easyexcel.annotation;

import com.alibaba.excel.support.ExcelTypeEnum;
import io.github.dk900912.easyexcel.support.Scene;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static io.github.dk900912.easyexcel.support.Scene.NORMAL;

/**
 * @author dukui
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ResponseExcel {

    /**
     * The name of the exported file.
     * <p>In general, exporting files with UUID naming strategy is used by
     * default in normal mode without explicitly specifying the exported file name.
     */
    String name() default "";

    /**
     * The format of the exported file.
     */
    ExcelTypeEnum suffix() default ExcelTypeEnum.XLSX;

    /**
     * The sheet array of the exported file.
     */
    Sheet[] sheets() default {};

    /**
     * The exporting scene.
     * <p>Default is {@code NORMAL}.
     */
    Scene scene() default NORMAL;
}
