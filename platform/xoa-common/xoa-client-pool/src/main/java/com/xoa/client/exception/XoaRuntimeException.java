package com.xoa.client.exception;


/**
 * xoa的根异常
 */
public class XoaRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public XoaRuntimeException(String msg) {
        super(msg);
    }

    public XoaRuntimeException(Throwable cause) {
        super(cause);
    }

    public XoaRuntimeException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
