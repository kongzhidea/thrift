package com.rr.publik.client.zk.pool;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.data.Stat;


/**
 * create(): 发起一个create操作. 可以组合其他方法 (比如mode 或background) 最后以forPath()方法结尾
 * <p/>
 * <p/>
 * delete(): 发起一个删除操作. 可以组合其他方法(version 或background) 最后以forPath()方法结尾
 * <p/>
 * <p/>
 * checkExists(): 发起一个检查ZNode 是否存在的操作. 可以组合其他方法(watch 或background)
 * 最后以forPath()方法结尾
 * <p/>
 * <p/>
 * getData(): 发起一个获取ZNode数据的操作. 可以组合其他方法(watch, background 或get stat)
 * 最后以forPath()方法结尾
 * <p/>
 * <p/>
 * setData(): 发起一个设置ZNode数据的操作. 可以组合其他方法(version 或background) 最后以forPath()方法结尾
 * <p/>
 * <p/>
 * getChildren(): 发起一个获取ZNode子节点的操作. 可以组合其他方法(watch, background 或get stat)
 * 最后以forPath()方法结尾
 *
 * @author Administrator
 */
public class ZookeeperService {
    private static final Log logger = LogFactory.getLog(ZookeeperService.class);

    public static String WATCH_PATH = "/" + GameServiceClientPool.serviceId;
    public static String ENABLE_PATH = "/" + GameServiceClientPool.serviceId + "/"
            + "enable";
    public static String DISABLE_PATH = "/" + GameServiceClientPool.serviceId + "/"
            + "disable";

    private CuratorFramework client;

    private ZookeeperService() {
        client = ZKClient.getClient();
    }

    private static ZookeeperService instance = new ZookeeperService();

    public static ZookeeperService getInstance() {
        return instance;
    }

    ;

    private ConnectionProvider connectionProvider;

    public void setConnectionProvider(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    /**
     * 监控WATCH_PATH，若该值在zk上有变化，则通知所有监听该值的warcher
     */
    public void regWatcherOnLineRserver() {
        logger.info("[zk watcher] register Watcher " + ENABLE_PATH);
        try {
            client.getChildren().usingWatcher(new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    logger.info("recieved zk change " + event.getPath() + " "
                            + event.getType().name());
                    if (event.getType() == Event.EventType.NodeChildrenChanged) {
                        try {
                            // update
                            if (connectionProvider != null) {
                                connectionProvider.updateNodeList();
                            }
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                    regWatcherOnLineRserver();
                }
            }).forPath(ENABLE_PATH);
        } catch (Exception e) {
            logger.error("zk watcher register error!" + e.getMessage(), e);
        }
    }

    /**
     * 得到该节点下的所有子节点
     *
     * @return
     */
    public List<String> getNodeList(String path) {
        List<String> ret = new ArrayList<String>();
        try {
            // 得到该节点下的所有子节点
            ret = client.getChildren().forPath(path);
        } catch (Exception e) {
            logger.error(
                    "get node list error! " + path + ".." + e.getMessage(), e);
        }

        return ret;
    }

    /**
     * 服务更新时请调用此方法
     *
     * @throws UnsupportedEncodingException
     * @throws Exception
     */
    public void updateState(String evt) throws UnsupportedEncodingException,
            Exception {
        // 更新在zk上该地址的值
        client.setData().forPath(WATCH_PATH, evt.getBytes("utf-8"));
    }

    /**
     * 增加服务时请调用此方法
     *
     * @throws Exception
     */
    public void addNode(String path, String identify, boolean notify)
            throws Exception {
        // 添加一个节点,值为空
        // creatingParentsIfNeeded 如果指定的节点的父节点不存在，递归创建父节点
        client.create().creatingParentsIfNeeded()
                .forPath(path + "/" + identify);
        if (notify) {
            updateState(identify);
        }
    }

    /**
     * 删除服务时请调用此方法
     *
     * @param gid
     * @throws Exception
     */
    public void deleteNode(String path, String identify, boolean notify)
            throws Exception {
        // 删除一个节点
        client.delete().forPath(path + "/" + identify);
        if (notify) {
            updateState(identify);
        }
    }

    /**
     * 得到某节点的状态，不存在则返回null
     *
     * @param gid
     * @return
     * @throws Exception
     */
    public Stat getStat(String path, String identify) throws Exception {
        // 得到某节点的状态，不存在则返回null
        return client.checkExists().forPath(path + "/" + identify);
    }

}
