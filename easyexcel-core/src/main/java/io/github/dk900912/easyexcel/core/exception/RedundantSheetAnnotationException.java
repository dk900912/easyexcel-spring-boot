package io.github.dk900912.easyexcel.core.exception;

/**
 * @author dukui
 */
public class RedundantSheetAnnotationException extends EasyExcelException {

    public RedundantSheetAnnotationException(String msg) {
        super(msg);
    }

    public RedundantSheetAnnotationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
