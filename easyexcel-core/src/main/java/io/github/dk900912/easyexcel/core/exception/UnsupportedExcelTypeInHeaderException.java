package io.github.dk900912.easyexcel.core.exception;

/**
 * @author dukui
 */
public class UnsupportedExcelTypeInHeaderException extends EasyExcelException {

    public UnsupportedExcelTypeInHeaderException(String msg) {
        super(msg);
    }

    public UnsupportedExcelTypeInHeaderException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
