package io.github.dk900912.easyexcel.core.exception;

/**
 * @author dukui
 */
public class MissingExcelTypeInHeaderException extends EasyExcelException {

    public MissingExcelTypeInHeaderException(String msg) {
        super(msg);
    }

    public MissingExcelTypeInHeaderException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
