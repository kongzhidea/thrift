package com.xoa.client.registry.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.xoa.client.registry.Node;
import com.xoa.client.registry.Service;
import com.xoa.client.registry.XoaRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * XoaRegistry的一个基础实现类
 */
public abstract class AbstractXoaRegistry implements XoaRegistry {

    protected Log logger = LogFactory.getLog(this.getClass());

    private Map<String, Service> serviceMap = new ConcurrentHashMap<String, Service>();

    @Override
    public List<Node> queryService(String serviceId) {
        List<Node> propNodes = getHostConfigFromSysProp(serviceId);
        if (!propNodes.isEmpty()) {
            return propNodes;
        }
        Service service = getService(serviceId);

        //not found
        if (service == null) {
            return null;
        }
        return service.getNodes();
    }

    private Service getService(String serviceId) {
        Service service = serviceMap.get(serviceId);
        if (service == null) {
            synchronized (serviceId.intern()) {
                service = serviceMap.get(serviceId);
                if (service == null) {
                    //load
                    service = loadService(serviceId);
                    //put into map
                    if (service != null) {
                        serviceMap.put(serviceId, service);
                    }
                }
            }
        }

        return service;
    }

    protected void clearService(String serviceId) {
        synchronized (serviceMap) {
            serviceMap.remove(serviceId);
        }
    }

    @Override
    public List<String> queryAlarmEmails(String serviceId) {
        Service service = serviceMap.get(serviceId);
        if (service == null) {
            return new ArrayList<String>();
        }
        return service.getAlarmEmails();
    }

    @Override
    public void disableNode(String serviceId, String identify) {
        disableNodeInner(serviceId, identify);
    }

    protected abstract void disableNodeInner(String serviceId, String identify);


    protected abstract Service loadService(String serviceId);

    class LocalConfig {
        public LocalConfig(List<Node> nodes, long time) {
            this.nodes = nodes;
            this.time = time;
        }

        public List<Node> nodes;
        public long time;
    }

    private Map<String, LocalConfig> hostConfigCache = new HashMap<String, LocalConfig>();

    private List<Node> getLocalConfigCache(String serviceId) {
        LocalConfig cfg = hostConfigCache.get(serviceId);
        long curTime = (new java.util.Date()).getTime();
        if (cfg != null && curTime - cfg.time < 10000) {
            return cfg.nodes;
        }
        return null;
    }

    private List<Node> getHostConfigFromSysProp(String serviceId) {
        List<Node> nodes = getLocalConfigCache(serviceId);

        if (nodes != null) {
            return nodes;
        }

        StringBuilder propName = new StringBuilder();
        propName.append("xoa.hosts.");
        propName.append(serviceId);

        String hosts = System.getProperty(propName.toString());

        nodes = new ArrayList<Node>();
        if (hosts != null) {
            String hostVec[] = hosts.split(",");
            for (String host : hostVec) {
                String[] ss = host.split(":");
                if (ss.length == 2) {
                    Node node = Node.getNodeFromIdentity(host);
                    nodes.add(node);
                }
            }
            if (nodes.size() > 0) {
                logger.warn("Using system property to locate XOA service nodes:" + hosts);
            }
            hostConfigCache.put(serviceId, new LocalConfig(nodes, (new java.util.Date()).getTime()));
        }
        return nodes;
    }

}
