package io.github.dk900912.easyexcel.core.exception;

/**
 * @author dukui
 */
public class ExcelCellContentNotValidException extends EasyExcelException {

    public ExcelCellContentNotValidException(String msg) {
        super(msg);
    }

    public ExcelCellContentNotValidException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
