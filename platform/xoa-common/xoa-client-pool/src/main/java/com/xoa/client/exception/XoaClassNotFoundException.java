package com.xoa.client.exception;

/**
 * runtime异常，用于代替{@link ClassNotFoundException}
 */
public class XoaClassNotFoundException extends XoaRuntimeException {

    private static final long serialVersionUID = 1L;

    public XoaClassNotFoundException(Throwable cause) {
        super(cause);
    }

}
