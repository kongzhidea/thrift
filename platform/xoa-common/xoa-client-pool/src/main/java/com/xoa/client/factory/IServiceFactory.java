package com.xoa.client.factory;

/**
 * 抽象IServiceFactory。
 */
public interface IServiceFactory {

    public <T> T getService(Class<T> serviceInterface);

    //精确到s
    public <T> T getService(Class<T> serviceInterface, int timeout);

}
