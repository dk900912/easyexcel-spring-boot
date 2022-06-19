package io.github.dk900912.easyexcel.core.exception;

import org.springframework.web.util.NestedServletException;

/**
 * @author dukui
 */
public class UnsatisfiedMethodSignatureException extends NestedServletException {

    public UnsatisfiedMethodSignatureException(String msg) {
        super(msg);
    }

    public UnsatisfiedMethodSignatureException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
