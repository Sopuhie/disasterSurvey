package com.santech.framework.exception;

/**
 * 运行时异常，可不捕获
 * 
 * @author L
 *
 */
public class SystemException extends RuntimeException {

    private static final long serialVersionUID = 9147222893441503818L;

    public SystemException() {
        super();
    }

    public SystemException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public SystemException(String message, Throwable cause) {
        super(message, cause);
    }

    public SystemException(String message) {
        super(message);
    }

    public SystemException(Throwable cause) {
        super(cause);
    }
}
