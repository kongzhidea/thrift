/**
 * @ ConnectionProvider.java Create on 2011-9-15 上午11:19:58
 */
package com.xoa.client.transport;

import com.xoa.client.registry.Node;
import org.apache.thrift.transport.TTransport;


/**
 * 获取的是 TTransport, 返回的却是 XoaTransport
 */
public interface ConnectionProvider {
    /**
     * 获取一个链接
     *
     * @param node 连接描述信息 {@link Node}
     * @return
     */
    public TTransport getConnection(Node node, int timeout) throws Exception;

    /**
     * 返回链接<br>
     * <p/>
     * 如果链接不是由 getConnection 返回的，则会抛出异常
     *
     * @param xoaTransport
     */
    public void returnConnection(XoaTransport xoaTransport) throws Exception;


    public void invalidateConnection(XoaTransport xoaTransport);

    public void clearConnections(Node node);

}
  