package io.github.dk900912.easyexcel.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author dukui
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestExcel {

    /**
     * Whether excel file is required.
     * <p>Default is {@code true}.
     */
    boolean required() default true;

    /**
     * The sheet array of the uploaded excel.
     *
     * @return The sheet array of the uploaded excel
     */
    Sheet[] sheets();
}
