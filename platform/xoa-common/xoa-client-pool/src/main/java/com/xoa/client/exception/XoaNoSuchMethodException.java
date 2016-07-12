package com.xoa.client.exception;


/**
 * runtime异常，用于代替{@link NoSuchMethodException}
 */
public class XoaNoSuchMethodException extends XoaRuntimeException {

    private static final long serialVersionUID = 1L;

    public XoaNoSuchMethodException(Throwable cause) {
        super(cause);
    }

}
