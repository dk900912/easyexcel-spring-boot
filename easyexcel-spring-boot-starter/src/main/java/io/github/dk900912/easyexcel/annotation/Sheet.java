package io.github.dk900912.easyexcel.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author dukui
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Sheet {

    /**
     * The name of the sheet.
     *
     * @return The name of the sheet
     */
    String name() default "";

    /**
     * The index of the sheet.
     *
     * @return The index of the sheet
     */
    int index() default 0;

    /**
     * The model of the sheet header.
     *
     * @return The model of the sheet header
     */
    Class<?> headClazz();

    /**
     * The row number of the sheet header.
     *
     * @return The row number of the sheet header
     */
    int headRowNumber() default 1;
}
