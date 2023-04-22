package io.github.dk900912.easyexcel.core.exception;

import jakarta.servlet.ServletException;

/**
 * @author dukui
 */
public abstract class EasyExcelException extends ServletException {

    protected EasyExcelException(String msg) {
        super(msg);
    }

    protected EasyExcelException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
