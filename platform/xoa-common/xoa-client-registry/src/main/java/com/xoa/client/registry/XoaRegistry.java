package com.xoa.client.registry;

import java.util.List;


/**
 * XoaRegistryFactory  单例
 */
public interface XoaRegistry {

    public List<Node> queryService(String serviceId);

    public List<String> queryAlarmEmails(String serviceId);

    public void disableNode(String serviceId, String identify);
}
