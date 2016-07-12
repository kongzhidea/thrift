package com.xoa.client.loader;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.xoa.client.registry.Node;
import com.xoa.client.registry.XoaRegistry;
import com.xoa.client.registry.XoaRegistryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RoundRobinLoadBalancer implements LoadBalancer {
    private Logger logger = LoggerFactory.getLogger(RoundRobinLoadBalancer.class);

    private Map<String, AtomicInteger> counterMap = new ConcurrentHashMap<String, AtomicInteger>();

    private XoaRegistry xoaRegistry = XoaRegistryFactory.getInstance().getDefaultRegistry();

    @Override
    public Node getNode(String serviceId) {
        List<Node> nodes = xoaRegistry.queryService(serviceId);
        return selectNode(serviceId, nodes);
    }

    private Node selectNode(String serviceId, List<Node> nodes) {
        AtomicInteger counter = counterMap.get(serviceId);
        if (counter == null) {
            synchronized (serviceId.intern()) {
                counter = counterMap.get(serviceId);
                if (counter == null) {
                    counter = new AtomicInteger();
                    counterMap.put(serviceId, counter);
                }
            }
        }

        int count = counter.incrementAndGet();
        if (count > Integer.MAX_VALUE - 100000) {   //防上溢，打出点提前量
            //归零
            counter.set(0);
        }
        Node node = null;
        int steps = 0;
        while (steps++ < nodes.size()) {
            node = nodes.get(count++ % nodes.size());
            if (!node.isDisabled()) {
                return node;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("node " + node.getHost() + ":" + node.getPort()
                        + " neglected, disabled=" + node.isDisabled());
            }
            counter.incrementAndGet();
        }

        return null;
    }

}
