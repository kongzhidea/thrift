package com.rr.publik.client.zk.pool;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.impl.GenericObjectPool;

/**
 * @author po.xu
 * @version 1.0
 * @mail po.xu@renren-inc.com
 */
public class GameConnectionProvider implements ConnectionProvider {
    private Log logger = LogFactory.getLog(this.getClass().getName());
    public final static GameConnectionPoolConfig defaultConfig = new GameConnectionPoolConfig();

    static {
        defaultConfig.setTimeout(1000);// 超时时间
        defaultConfig.setMaxActive(300); // 可以从缓存池中分配对象的最大数量
        defaultConfig.setMaxIdle(50); // 缓存池中最大空闲对象数量
        defaultConfig.setMinIdle(30); // 缓存池中最小空闲对象数量
        defaultConfig.setMaxWait(10); // 阻塞的最大数量
        defaultConfig.setTestOnBorrow(false); // 从缓存池中分配对象，是否执行PoolableObjectFactory.validateObject方法
        defaultConfig.setTestOnReturn(false);
        defaultConfig.setTestWhileIdle(false);
    }

    public GameConnectionProvider() {
		String identify = getSystemProperty();
        if (!StringUtils.isBlank(identify)) {
            return;
        }
        ZookeeperService.getInstance().setConnectionProvider(this);
        // 在zk上监听节点变化 有变化时候 调用updateNodeList方法
        ZookeeperService.getInstance().regWatcherOnLineRserver();

        setNodeListFirst();
    }
	private String getSystemProperty() {
        return System.getProperty("xoa.hosts." + GameServiceClientPool.serviceId);
    }

    /**
     * 每个服务对应一个pool
     */
    private final static Map<String, GenericObjectPool<GameConnection>> pools = new ConcurrentHashMap<String, GenericObjectPool<GameConnection>>(
            defaultConfig.maxActive);

    public void createPool(Node node, GameConnectionPoolConfig config) {
        GameConnectionPoolableObjectFactory factory = new GameConnectionPoolableObjectFactory(
                node, config.getTimeout(), config.isKeeepAlive());
        GenericObjectPool<GameConnection> pool = new GenericObjectPool<GameConnection>(
                factory, config);
        pools.put(node.getIdentity(), pool);
        logger.info("create pool:" + node.getIdentity());
    }

    private LoadBalancer balancer = new RoundRobinLoadBalancer();
    private List<Node> nodes = new ArrayList<Node>();

    public List<Node> getNodes() {
        return nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    /**
     * 销毁连接池
     **/
    @Override
    public void destroy(String identity) {
        try {
            if (StringUtils.isEmpty(identity)) {
                logger.error("destroy pool with emtpy node.");
                return;
            }
            if (pools.containsKey(identity)) {
                pools.get(identity).close();
                pools.remove(identity);
                if (logger.isInfoEnabled()) {
                    logger.info("POOL[" + identity
                            + "] destroyed with identity:" + identity);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Destroy pool by identity:" + identity
                    + " ERROR.", e);
        }
    }

    public static String getConnectionStatus() {
        Iterator<Map.Entry<String, GenericObjectPool<GameConnection>>> iter = pools
                .entrySet().iterator();

        StringBuilder message = new StringBuilder();
        while (iter.hasNext()) {
            Map.Entry<String, GenericObjectPool<GameConnection>> entry = (Map.Entry<String, GenericObjectPool<GameConnection>>) iter
                    .next();
            String identity = entry.getKey();
            GenericObjectPool<GameConnection> pool = entry.getValue();
            message.append("Status of GameConnection in POOL[" + identity
                    + "] is:" + "\n GameConnection USING: "
                    + pool.getNumActive() + "\n GameConnection IDLE:"
                    + pool.getNumIdle() + '\n');
        }
        return message.toString();
    }

    /**
     * 得到某个指定服务的连接池
     *
     * @throws ConnectionException
     */
    @Override
    public GameConnection getConnection(String identity)
            throws ConnectionException {
        try {
            if (!pools.containsKey(identity)) {
                synchronized (GameConnectionProvider.class) {
                    if (!pools.containsKey(identity)) {
                        createPool(Node.getNodeFromIdentity(identity),
                                defaultConfig);
                    }
                }
            }
            return pools.get(identity).borrowObject();
        } catch (Exception e) {
            logger.error(
                    "getGameConnection from POOL[" + identity
                            + "] ERROR. with identity:" + identity + "..->"
                            + e.getMessage(), e);
            throw new ConnectionException(identity, getConnectionStatus()
                    + "getGameConnection by identity:" + identity + " ERROR.",
                    e);
        }
    }

    /**
     * 从zk上得到所有enable服务的连接池,如果绑了测试环境，则使用绑定测试环境的服务，
     * <p/>
     * 例如:System.setProperty( "com.rr.publik.service", "10.2.45.39:9301");
     *
     * @throws ConnectionException
     * @throws Exception
     */
    @Override
    public GameConnection getConnection() throws ConnectionException {
        String identify = getSystemProperty();
        if (!StringUtils.isBlank(identify)) {
            return getConnection(identify);
        }

        Node node = balancer.getNode(this.nodes);

        return getConnection(node.getIdentity());
    }

    @Override
    public void returnConnection(GameConnection connection) {
        try {
            if (connection != null) {
                GenericObjectPool<GameConnection> pool = pools.get(connection
                        .getNode().getIdentity());
                if (pool != null) {
                    pool.returnObject(connection);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("returnGameConnection Error.", e);
        }
    }

    /**
     * 首次设置 node列表
     */
    public void setNodeListFirst() {
        List<String> nodeList = ZookeeperService.getInstance().getNodeList(
                ZookeeperService.ENABLE_PATH);
        if ((this.nodes == null || this.nodes.size() == 0)) {
            synchronized (nodes) {
                if ((this.nodes == null || this.nodes.size() == 0)) {
                    nodes = Node.getNodeFromIdentity(nodeList);
                }

            }
            logger.info("get node list from zk:" + nodes);
        }
    }

    /**
     * 更新zk的时候，务必要设置监听点的值为当前更新的identify,否则无法更新
     */
    @Override
    public synchronized void updateNodeList() {
        // 当前 正常使用的节点
        List<String> nodeList = ZookeeperService.getInstance().getNodeList(
                ZookeeperService.ENABLE_PATH);

        // 需要 先将当前正在执行的任务执行完毕，才能destory
        List<String> disableList = new ArrayList<String>();
        for (Node node : nodes) {
            if (!Node.inNodes(nodeList, node.getIdentity())) {
                // 设置not healthy后 轮询时候会跳过该节点
                node.setHealthy(false);
                disableList.add(node.getIdentity());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        for (String identify : disableList) {
            destroy(identify);
        }

        nodes = Node.getNodeFromIdentity(nodeList);
        logger.info("get node list from zk:" + nodes);
    }

}
