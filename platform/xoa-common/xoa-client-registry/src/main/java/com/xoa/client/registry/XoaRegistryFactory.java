package com.xoa.client.registry;


import com.xoa.client.registry.impl.zookeeper.ZookeeperBasedRegistry;

/**
 * XoaRegistry工厂
 */
public class XoaRegistryFactory {

    private static XoaRegistryFactory instance = new XoaRegistryFactory();

    public static XoaRegistryFactory getInstance() {
        return instance;
    }

    private XoaRegistry registry;

    private XoaRegistryFactory() {
        try {
            registry = new ZookeeperBasedRegistry();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @return 默认的XoaRegistry
     */
    public XoaRegistry getDefaultRegistry() {
        return registry;
    }
}
