package com.xoa.client.loader;


import com.xoa.client.registry.Node;

/**
 * 用于封装负载均衡逻辑
 */
public interface LoadBalancer {

    /**
     * 给定serviceId，返回一个负载均衡后的节点
     *
     * @param serviceId
     * @return
     */
    public Node getNode(String serviceId);
}
