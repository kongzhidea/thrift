package com.rr.publik.client.zk.pool;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LoadBalancer的轮叫调度实现<br>
 * 当无可用节点时，抛出XoaNoneAvaliableNodeException
 *
 * @author Li Weibo (weibo.leo@gmail.com) //I believe spring-brother
 * @since 2011-12-22 下午04:30:21
 */
public class RoundRobinLoadBalancer implements LoadBalancer {

    private Logger logger = LoggerFactory
            .getLogger(RoundRobinLoadBalancer.class);

    private AtomicInteger counter = new AtomicInteger();

    @Override
    public Node getNode(List<Node> nodes) {
        if (counter.get() > Integer.MAX_VALUE - 100000) { // 防上溢，打出点提前量
            // 归零
            counter.set(0);
        }
        Node node = null;
        int steps = 0;
        // *2 是因为高并发下， 下掉一个节点时候先设置healty
        while (steps++ < nodes.size() * 2) {
            node = nodes.get(counter.incrementAndGet() % nodes.size());
            if (node.isHealthy()) {
                return node;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("node " + node.getHost() + ":" + node.getPort()
                        + " neglected" + ",healthy=" + node.isHealthy());
            }
        }

        if (node == null && nodes.size() > 0) {
            node = nodes.get(0);
        }

        if (node == null) {
            logger.error("None available node for service : "
                    + GameServiceClientPool.serviceId);
        }
        return node;
    }

}
