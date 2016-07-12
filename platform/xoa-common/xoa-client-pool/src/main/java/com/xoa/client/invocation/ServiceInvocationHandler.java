package com.xoa.client.invocation;

import com.xoa.client.definition.ClassDefinition;
import com.xoa.client.exception.XoaClientPoolException;
import com.xoa.client.exception.XoaTransportException;
import com.xoa.client.router.CommonServiceRouter;
import com.xoa.client.router.ServiceRouter;
import com.xoa.client.transport.XoaTransport;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * proxy的InvocationHandler
 */
public class ServiceInvocationHandler implements InvocationHandler {

    private static Logger logger = LoggerFactory.getLogger(ServiceInvocationHandler.class);
    private ClassDefinition serviceDefinition;

    private ConcurrentMap<Method, Method> methodCache = new ConcurrentHashMap<Method, Method>();

    private ServiceRouter serviceRouter;

    private int timeout;

    public ServiceInvocationHandler(ServiceRouter serviceRouter, ClassDefinition serviceDefinition, int timeout) {
        if (serviceDefinition == null || serviceRouter == null) {
            throw new NullPointerException();
        }
        this.serviceRouter = serviceRouter;
        this.serviceDefinition = serviceDefinition;
        this.timeout = timeout;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        Method methodDef = null;
        try {
            // 获取 thrift中client的方法
            methodDef = getRealMethod(method);
        } catch (SecurityException e) {
            logger.error(e.getMessage(), e);
        } catch (NoSuchMethodException e) {
            logger.error(e.getMessage(), e);
        }

        String serviceId = serviceDefinition.getServiceId();
        XoaTransport xoaTransport = null;
        try {
            xoaTransport = serviceRouter.routeService(serviceId, timeout);

            TProtocol protocol = new TCompactProtocol(xoaTransport.getTransport());
            Object client = serviceDefinition.getServiceClientConstructor().newInstance(protocol);

            Object ret = methodDef.invoke(client, args);

            serviceRouter.returnConn(xoaTransport);
            return ret;
        } catch (XoaClientPoolException e) {
            logger.error("XoaClientPoolException:" + serviceId + "," + e.getMessage(), e);
            serviceRouter.serviceException(serviceId, e, xoaTransport);
            throw e;
        } catch (XoaTransportException e) {
            logger.error("XoaTransportException:" + serviceId + "," + e.getMessage(), e);
            serviceRouter.serviceException(serviceId, e, xoaTransport);
            throw e;
        } catch (InvocationTargetException e) {
            logger.error("InvocationTargetException:" + serviceId + "," + e.getMessage(), e);
            Throwable cause = e.getCause();
            if (cause instanceof org.apache.thrift.TBase) {
                serviceRouter.returnConn(xoaTransport);
                logger.error("service returned ex");
            } else {
                // server重启后 第一次连接会报这个错误 TTransportException.END_OF_FILE
                serviceRouter.serviceException(serviceId, cause, xoaTransport);
            }
            throw cause;
        } catch (Exception e) {
            Throwable cause = e.getCause();
            if (cause == null) {
                serviceRouter.serviceException(serviceId, e, xoaTransport);
                throw e;
            } else {
                serviceRouter.serviceException(serviceId, cause, xoaTransport);
                throw cause;
            }
        }
    }

    /**
     * 获得方法定义，首先会从缓存中取数据，如果缓存中没有则通过反射的方式获得
     *
     * @param method
     * @return
     * @throws SecurityException
     * @throws NoSuchMethodException
     */
    private Method getRealMethod(Method method) throws SecurityException,
            NoSuchMethodException {
        Method realMethod = methodCache.get(method);

        if (realMethod != null) { // 先从缓存中找方法定义，缓存中有数据直接返回
            return realMethod;
        }

        realMethod = serviceDefinition.getServiceClientClass().getMethod(method.getName(),
                method.getParameterTypes());
        methodCache.put(method, realMethod);
        return realMethod;
    }
}
