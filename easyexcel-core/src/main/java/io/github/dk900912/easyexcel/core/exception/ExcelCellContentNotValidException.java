package io.github.dk900912.easyexcel.core.exception;

import org.springframework.web.util.NestedServletException;

/**
 * @author dukui
 */
public class ExcelCellContentNotValidException extends NestedServletException {

    public ExcelCellContentNotValidException(String msg) {
        super(msg);
    }

    public ExcelCellContentNotValidException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
