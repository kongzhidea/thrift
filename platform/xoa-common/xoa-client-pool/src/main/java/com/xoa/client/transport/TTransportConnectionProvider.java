/**
 * 连接池
 */
package com.xoa.client.transport;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.xoa.client.registry.Node;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.thrift.transport.TTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 对连接的管理类,有以下职责:<br>
 * 1. 连接池职责: 建立和唯一标识符(ip+port)对应的连接池信息<br>
 * 2. 继承自接口职责: 获取,归还连接
 */
public class TTransportConnectionProvider implements ConnectionProvider {
    private Logger logger = LoggerFactory
            .getLogger(TTransportConnectionProvider.class);

    public final static ConnectionPoolConfig defaultConfig = new ConnectionPoolConfig();

    static {
        defaultConfig.setTimeout(500);// 超时时间
        defaultConfig.setMaxActive(300); // 可以从缓存池中分配对象的最大数量
        defaultConfig.setMaxIdle(50); // 缓存池中最大空闲对象数量
        defaultConfig.setMinIdle(30); // 缓存池中最小空闲对象数量
        defaultConfig.setMaxWait(10); // 阻塞的最大数量
        defaultConfig.setTestOnBorrow(false); // 从缓存池中分配对象，是否执行PoolableObjectFactory.validateObject方法
        defaultConfig.setTestOnReturn(false);
        defaultConfig.setTestWhileIdle(false);
        // 连接池耗尽，borrowObject方法锁等待
        defaultConfig.setWhenExhaustedAction(GenericObjectPool.WHEN_EXHAUSTED_FAIL);
    }


    private static HashMap<String, GenericObjectPool<TTransport>> servicePoolMap = new HashMap<String, GenericObjectPool<TTransport>>();

    public TTransportConnectionProvider() {
    }

    public static void setPoolParam(int maxActive, int maxIdle, int minIdle,
                                    int maxWait) {
        defaultConfig.setMaxActive(maxActive);
        defaultConfig.setMaxActive(maxActive);
        defaultConfig.setMaxIdle(maxIdle);
        defaultConfig.setMinIdle(minIdle);
        defaultConfig.setMaxWait(maxWait);
    }


    /**
     * 设置连接的超时时间，如果需要针对独立的 IP+Port 设定超时时间，则需要调整 servicePoolMap 的数据结构
     *
     * @param connTimeout
     */
    public static void setTimeout(int connTimeout) {
        defaultConfig.setTimeout(connTimeout);
    }

    private GenericObjectPool<TTransport> createPool(Node node, int timeout) {
        // 设置factory
        ThriftPoolableObjectFactory factory = new ThriftPoolableObjectFactory(
                node.getHost(), node.getPort(), timeout);

        GenericObjectPool<TTransport> objectPool = new GenericObjectPool<TTransport>(factory, defaultConfig);

        servicePoolMap.put(node.getIdentity(), objectPool);

        logger.info("create pool:" + node.getIdentity() + ",timeout=" + timeout);

        return objectPool;
    }

    public static String getConnStatus() {
        Iterator iter = servicePoolMap.entrySet().iterator();

        StringBuffer message = new StringBuffer();
        while (iter.hasNext()) {
            Map.Entry<String, GenericObjectPool<TTransport>> entry = (Map.Entry) iter.next();
            String keyString = (String) entry.getKey();
            GenericObjectPool<TTransport> pool = entry.getValue();

            message.append("Status of connection [" + keyString + "] is:"
                    + "\n pool using size: " + pool.getNumActive()
                    + "\n pool idle size:" + pool.getNumIdle() + '\n');
        }

        return message.toString();
    }

    @Override
    public TTransport getConnection(Node node, int timeout) throws Exception {
        TTransport transport = null;
        String key = node.getIdentity();

        try {
            if (!servicePoolMap.containsKey(key)) {
                synchronized (this) {
                    if (!servicePoolMap.containsKey(key)) {
                        createPool(Node.getNodeFromIdentity(key), timeout);
                    }
                }
            }
            GenericObjectPool<TTransport> pool = servicePoolMap.get(key);
            transport = pool.borrowObject();

            if (logger.isDebugEnabled()) {
                logger.debug("pool-stat: alloc " + transport + ",active="
                        + pool.getNumActive() + ",idle=" + pool.getNumIdle());
            }
            return transport;
        } catch (Exception e) {
            logger.error("client pool other exception : " + key + ",ex="
                    + e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void returnConnection(XoaTransport xoaTransport) throws Exception {
        String key = xoaTransport.getNode().getIdentity();
        GenericObjectPool<TTransport> pool = null;
        try {
            pool = servicePoolMap.get(key);
            if (pool != null) {
                pool.returnObject(xoaTransport.getTransport());
                if (logger.isDebugEnabled()) {
                    logger.debug("pool-stat: dealloc "
                            + xoaTransport.getTransport() + ",active="
                            + pool.getNumActive() + ",idle=" + pool.getNumIdle());
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("pool-stat: dealloc "
                            + xoaTransport.getTransport() + ", pool not exist.");
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void invalidateConnection(XoaTransport xoaTransport) {

        String key = xoaTransport.getNode().getIdentity();
        TTransport trans = xoaTransport.getTransport();
        GenericObjectPool<TTransport> pool = null;
        try {
            pool = servicePoolMap.get(key);
            if (pool != null) {
                pool.invalidateObject(trans);
                if (logger.isDebugEnabled()) {
                    logger.debug("pool-stat: invalidate " + trans + ",active="
                            + pool.getNumActive() + ",idle=" + pool.getNumIdle());
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void clearConnections(Node node) {
        final String key = node.getIdentity();
        try {
            if (servicePoolMap.containsKey(key)) {
                synchronized (servicePoolMap) {
                    if (servicePoolMap.containsKey(key)) {
                        GenericObjectPool<TTransport> pool = servicePoolMap.get(key);
                        if (pool != null) {
                            pool.clear();
                        }
                        if (logger.isInfoEnabled()) {
                            logger.info("POOL[" + key + "] destroyed with identity:" + key);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        logger.info("pool-stat: pool destruction " + key);
    }
}
