package com.xoa.client.registry.impl.zookeeper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import com.sun.org.apache.xpath.internal.operations.Bool;
import com.xoa.client.registry.Node;
import com.xoa.client.registry.Service;
import com.xoa.client.registry.impl.AbstractXoaRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.ConnectionLossException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;


/**
 * 基于Zookeeper的registry实现
 */
public class ZookeeperBasedRegistry extends AbstractXoaRegistry {

    protected final Log logger = LogFactory.getLog(this.getClass());

    private CuratorFramework client = null;

    private Map<String, Boolean> watchMap = new ConcurrentHashMap<String, Boolean>();


    private CuratorFramework getClient(){
        if(client == null){
            synchronized (this){
                if(client == null){
                    client = ZKClient.getClient();
                }
            }
        }
        return client;
    }

    @Override
    protected Service loadService(String serviceId) {
        //监听节点变化
        listen(serviceId);

        //根据给定的serviceId拼出存储在zookeeper中的配置文件的路径
        String enableNodePath = ZKPathUtil.serviceIdToEnablePath(serviceId);

        String emailPath = ZKPathUtil.getAlarmEmailsByServiceId(serviceId);
        try {
            // 可用节点
            List<String> childNodes = getChildNodeList(enableNodePath);

            // 报警邮件
            List<String> alarmEmails = getChildNodeList(emailPath);

            Service service = new Service();
            service.setServiceId(serviceId);
            service.setNodes(Node.getNodeFromIdentity(childNodes));
            service.setAlarmEmails(alarmEmails);
            return service;
        } catch (Exception e) {
            logger.error("serviceId:" + serviceId + ",  " + e.getMessage(), e);
        }
        return null;
    }


    @Override
    protected void disableNodeInner(String serviceId, String identify) {
        //根据给定的serviceId拼出存储在zookeeper中的配置文件的路径
        String enableNodePath = ZKPathUtil.serviceIdToEnablePath(serviceId);
        String disableNodePath = ZKPathUtil.serviceIdToDisablePath(serviceId);
        String nodePath = enableNodePath + "/" + identify;
        String disNodePath = disableNodePath + "/" + identify;
        try {
            if (getStat(nodePath) != null) {
                deleteNode(nodePath);
                if (getStat(disNodePath) == null) {
                    getClient().create().creatingParentsIfNeeded().forPath(disNodePath);
                }
            } else {
                clearService(serviceId);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }


    private List<String> getChildNodeList(String nodePath) {
        List<String> nodes = new ArrayList<String>();
        try {
            // 得到该节点下的所有子节点
            if (getStat(nodePath) != null) {
                nodes = getClient().getChildren().forPath(nodePath);
            }

        } catch (Exception e) {
            logger.error(
                    "get node list error! " + nodePath + ".." + e.getMessage(), e);
        }
        return nodes;
    }

    /**
     * 删除服务时请调用此方法
     *
     * @throws Exception
     */
    public void deleteNode(String path) throws Exception {
        // 删除一个节点
        getClient().delete().forPath(path);
    }


    /**
     * 得到某节点的状态，不存在则返回null
     *
     * @param path
     * @return
     * @throws Exception
     */
    public Stat getStat(String path) throws Exception {
        // 得到某节点的状态，不存在则返回null
        return getClient().checkExists().forPath(path);
    }


    private void listen(String serviceId) {
        Boolean listened = watchMap.get(serviceId);
        if (listened == null || listened == false) {
            synchronized (serviceId.intern()) {
                listened = watchMap.get(serviceId);
                if (listened == null || listened == false) {
                    regWatcherOnLineRserver(serviceId);
                    watchMap.put(serviceId, true);
                }
            }
        }
    }

    /**
     * 监控WATCH_PATH，若该值在zk上有变化，则通知所有监听该值的warcher
     */
    public void regWatcherOnLineRserver(final String serviceId) {
        String nodePath = ZKPathUtil.serviceIdToEnablePath(serviceId);

        logger.info("[zk watcher] register Watcher " + nodePath);
        try {
            getClient().getChildren().usingWatcher(new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    logger.info("recieved zk change " + event.getPath() + " "
                            + event.getType().name());
                    if (event.getType() == Event.EventType.NodeChildrenChanged) {
                        try {
                            clearService(serviceId);
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                    regWatcherOnLineRserver(serviceId);
                }
            }).forPath(nodePath);
        } catch (Exception e) {
            logger.error("zk watcher register error!" + e.getMessage(), e);
        }
    }

}
