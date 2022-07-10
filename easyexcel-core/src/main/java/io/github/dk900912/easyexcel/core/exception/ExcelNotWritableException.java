package io.github.dk900912.easyexcel.core.exception;

/**
 * @author dukui
 */
public class ExcelNotWritableException extends EasyExcelException {

    public ExcelNotWritableException(String msg) {
        super(msg);
    }

    public ExcelNotWritableException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
