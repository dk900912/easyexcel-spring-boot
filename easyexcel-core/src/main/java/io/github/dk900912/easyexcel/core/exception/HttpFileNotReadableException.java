package io.github.dk900912.easyexcel.core.exception;

import org.springframework.web.util.NestedServletException;

/**
 * @author dukui
 */
public class HttpFileNotReadableException extends NestedServletException {

    public HttpFileNotReadableException(String msg) {
        super(msg);
    }

    public HttpFileNotReadableException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
