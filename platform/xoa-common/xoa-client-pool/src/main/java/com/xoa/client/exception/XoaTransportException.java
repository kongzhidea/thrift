package com.xoa.client.exception;


/**
 * 获得transport为null或者不可用抛出这个异常
 */
public class XoaTransportException extends XoaRuntimeException {

    private static final long serialVersionUID = 1L;

    public XoaTransportException(String msg) {
        super(msg);
    }

}
