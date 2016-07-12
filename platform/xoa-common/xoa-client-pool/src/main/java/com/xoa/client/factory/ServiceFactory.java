package com.xoa.client.factory;


import com.xoa.client.router.CommonServiceRouter;

/**
 * 一个静态工厂类（工具类），根据接口生成代理服务类。
 * 使用IServiceFactory的默认实现创建代理服务类。
 */
public class ServiceFactory {

    /**
     * 真正的工厂类,抽象IServiceFactory，目的在于封装创建Service实例的逻辑，与静态工厂类ServiceFactory解耦。
     * <p/>
     */
    private static IServiceFactory factory = new DefaultServiceFactory(CommonServiceRouter.getInstance());

    /**
     * API 必须以I开头，ClassDefinition会找到真实的client类
     * 绑环境 以xoa.hosts.开头  AbstractXoaRegistry 配置
     *
     * @param serviceClass
     * @param <T>
     * @return
     */
    public static <T> T getService(Class<T> serviceClass) {
        return getService(serviceClass, 500);
    }

    public static <T> T getService(Class<T> serviceClass, int timeout) {
        return factory.getService(serviceClass, timeout);
    }
}
