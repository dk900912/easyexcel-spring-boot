package io.github.dk900912.easyexcel.core.exception;

import org.springframework.web.util.NestedServletException;

/**
 * @author dukui
 */
public class EasyExcelException extends NestedServletException {

    public EasyExcelException(String msg) {
        super(msg);
    }

    public EasyExcelException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
