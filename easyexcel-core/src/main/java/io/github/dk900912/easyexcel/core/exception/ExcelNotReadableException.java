package io.github.dk900912.easyexcel.core.exception;

/**
 * @author dukui
 */
public class ExcelNotReadableException extends EasyExcelException {

    public ExcelNotReadableException(String msg) {
        super(msg);
    }

    public ExcelNotReadableException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
