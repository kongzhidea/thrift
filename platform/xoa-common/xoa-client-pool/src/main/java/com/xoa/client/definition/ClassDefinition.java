package com.xoa.client.definition;

import java.lang.reflect.Constructor;

import com.xoa.client.annotion.XoaService;
import org.apache.thrift.protocol.TProtocol;


/**
 * 接口类相关信息
 */
public class ClassDefinition {

    private String serviceId;

    private Class<?> serviceClientClass;// thrift 真实client类

    private Constructor<?> serviceClientConstructor;

    public ClassDefinition(Class<?> serviceInterface) throws ClassNotFoundException, SecurityException, NoSuchMethodException {
        String clientClassName = resolveClientClassName(serviceInterface);
        this.serviceClientClass = Class.forName(clientClassName);
        this.serviceId = revolveServiceId(serviceInterface);

        //实例化client的构造函数   例如:new GameService.Client(protocol)
        this.serviceClientConstructor = serviceClientClass.getConstructor(TProtocol.class);
    }

    private String resolveClientClassName(Class<?> serviceClass) {
        String packageName = serviceClass.getPackage().getName();
        String simpleClassName = serviceClass.getSimpleName();
        simpleClassName = simpleClassName.substring(1); // remove heading I
        return packageName + "." + simpleClassName + "$Client";
    }

    private String revolveServiceId(Class<?> serviceClass) {
        XoaService xoaService = serviceClass.getAnnotation(XoaService.class);
        return xoaService != null ? xoaService.value().trim() : "";
    }


    public String getServiceId() {
        return serviceId;
    }

    public Class<?> getServiceClientClass() {
        return serviceClientClass;
    }

    public Constructor<?> getServiceClientConstructor() {
        return serviceClientConstructor;
    }

}
