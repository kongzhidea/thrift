package com.xoa.client.router;

import com.xoa.client.transport.XoaTransport;
import org.apache.thrift.transport.TSocket;


/**
 * 服务路由器
 */
public interface ServiceRouter {

    /**
     * 获取到指定service的路由
     *
     * @param serviceId
     * @return
     */
    public XoaTransport routeService(String serviceId, int timeout) throws Exception;

    /**
     * 将transport连接归还到连接池
     **/
    public void returnConn(XoaTransport xoaTransport) throws Exception;

    /**
     * 客户端调用出现异常情况
     **/
    public void serviceException(String serviceId, Throwable e, XoaTransport xoaTransport);

    /**
     * 路由Service，建立连接和传输数据时。
     * <p/>
     * routeService 应该 关心timeout，因为路由服务、建立连接的时候才有超时这一说。
     *
     * @param
     * @see
     */
    public void setTimeout(int timeout);
}
