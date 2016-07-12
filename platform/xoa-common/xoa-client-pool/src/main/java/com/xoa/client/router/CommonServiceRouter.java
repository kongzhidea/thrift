package com.xoa.client.router;

import com.xoa.client.exception.XoaClientPoolException;
import com.xoa.client.exception.XoaTransportException;
import com.xoa.client.loader.LoadBalancer;
import com.xoa.client.loader.RoundRobinLoadBalancer;
import com.xoa.client.registry.Node;
import com.xoa.client.registry.NodeErrorInfo;
import com.xoa.client.registry.XoaRegistry;
import com.xoa.client.registry.XoaRegistryFactory;
import com.xoa.client.transport.ConnectionProvider;
import com.xoa.client.transport.TTransportConnectionProvider;
import com.xoa.client.transport.XoaTransport;
import org.apache.commons.lang.StringUtils;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;


public class CommonServiceRouter implements ServiceRouter {
    private Logger logger = LoggerFactory.getLogger(CommonServiceRouter.class);

    private static CommonServiceRouter instance = new CommonServiceRouter();

    private ConnectionProvider connectionProvider = new TTransportConnectionProvider();

    private LoadBalancer loadBalancer = new RoundRobinLoadBalancer();
    ScheduledThreadPoolExecutor timerExecutor = new ScheduledThreadPoolExecutor(
            4);

    private XoaRegistry xoaRegistry = XoaRegistryFactory.getInstance()
            .getDefaultRegistry();

    private final static int DISABLE_THRESHOLD = 100;
    private final static int MAX_GET_RETRY = 1;

    private CommonServiceRouter() {
    }

    public static CommonServiceRouter getInstance() {
        return instance;
    }

    public boolean isValidServiceId(String id) {
        if (id == null || id.isEmpty()) {
            logger.error("null service id!");
            return false;
        }


        if (id.split("\\.").length <= 3) {
            return false;
        }
        return true;
    }

    @Override
    public XoaTransport routeService(String serviceId, int timeout)
            throws Exception {
        if (!isValidServiceId(serviceId)) {
            logger.error("bad service id : " + serviceId);
            throw new XoaClientPoolException("bad service id : " + serviceId);
        }
        Node node = null;
        int retry = 0;
        TTransport transport = null;
        XoaTransport xoaTransport = null;

        while (true) {
            node = loadBalancer.getNode(serviceId);

            // 没有可用的node，这是严重的错误
            if (node == null) {
                logger.error("No endpoint available : " + serviceId);
                return null;
            }
            // 出错时，也创建xoaTransport, 以保存node信息
            xoaTransport = new XoaTransport();
            xoaTransport.setNode(node);

            try {
                transport = connectionProvider.getConnection(node, timeout);
                break;
            } catch (Exception e) {
                disableNode(serviceId, node, DISABLE_THRESHOLD / 200);
                logger.error("Get service error : " + node.getIdentity()
                        + ' ' + serviceId);

                if (++retry >= MAX_GET_RETRY) {
                    throw new XoaClientPoolException("service error : " + node.getIdentity()
                            + ' ' + serviceId);
                }
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("get service ok : " + serviceId);
        }
        xoaTransport.setTransport(transport);
        return xoaTransport;
    }

    @Override
    public void returnConn(XoaTransport xoaTransport) throws Exception {
        String signature = xoaTransport.getNode().getIdentity();
        NodeErrorInfo err = errorMap.get(signature);
        if (err != null) {
            err.setCount(0);
        }

        connectionProvider.returnConnection(xoaTransport);
    }

    /**
     * 设定获取到连接的超时时间
     *
     * @param connTimeout
     */
    public void setTimeout(int connTimeout) {
        TTransportConnectionProvider.setTimeout(connTimeout);
    }

    private ConcurrentMap<String, NodeErrorInfo> errorMap = new ConcurrentHashMap<String, NodeErrorInfo>();


    private void smsNotify(String serviceId, String identify, String desc) {
        List<String> emails = xoaRegistry.queryAlarmEmails(serviceId);
        if (emails.size() > 0) {
            //SendMailUtil.sendMail(StringUtils.join(emails, ","), serviceId + "," + identify, desc);
        }

    }

    void disableNode(String serviceId, Node node, int delta) {
        if (node == null) {
            return;
        }
        String identify = node.getIdentity();
        NodeErrorInfo err = errorMap.get(identify);

        connectionProvider.clearConnections(node);

        if (err == null) {
            err = new NodeErrorInfo(serviceId, node, delta);
            errorMap.put(identify, err);
        } else {
            err.addCount(delta);
        }

        logger.info(identify + "disabled" + err.getCount() + '/'
                + DISABLE_THRESHOLD);
        if (node != null && err.getCount() >= DISABLE_THRESHOLD) {
            smsNotify(serviceId, identify, "service disabled");
            xoaRegistry.disableNode(serviceId, node.getIdentity());
            err.setCount(0);
        }
    }

    @Override
    public void serviceException(String serviceId, Throwable e,
                                 XoaTransport xoaTransport) {
        String signature = "null-service";
        Node node = null;
        if (xoaTransport != null) {
            node = xoaTransport.getNode();
            signature = node.getIdentity();
            if (logger.isDebugEnabled()) {
                logger.debug("invalidate addr=" + signature + ",prov="
                        + connectionProvider + ",xoaTransport=" + xoaTransport);
            }
            connectionProvider.invalidateConnection(xoaTransport);
        }

        int delta = 1;

        if (e instanceof TTransportException) {
            Throwable cause = e.getCause();
            if (cause == null) {
                int type = ((TTransportException) e).getType();
                switch (type) {
                    case TTransportException.END_OF_FILE:
                        logger.error("xoa2 service=" + serviceId + " addr=" + signature
                                + " ex=" + "RPC TTransportException END_OF_FILE");
                        delta = DISABLE_THRESHOLD / 200;
                        break;
                    default:
                        logger.error("xoa2 service=" + serviceId + " addr=" + signature
                                + " ex=" + "RPC TTransportException default");
                        delta = DISABLE_THRESHOLD / 200;
                        break;
                }
            } else {
                if (cause instanceof java.net.SocketTimeoutException) {
                    delta = DISABLE_THRESHOLD / 500;
                    logger.error("xoa2 service=" + serviceId + " addr=" + signature
                            + " ex=" + "RPC TTransportException SocketTimeoutException");
                } else {
                    delta = DISABLE_THRESHOLD / 200;
                    logger.error("xoa2 service=" + serviceId + " addr=" + signature
                            + " ex=" + "RPC TTransportException " + cause.getMessage());
                }
            }
        } else if (e instanceof XoaTransportException) {
            delta = DISABLE_THRESHOLD / 200;
            logger.error("xoa2 service=" + serviceId + " addr=" + signature
                    + " ex=" + "XoaTransportException " + e.getMessage());
        } else if (e instanceof XoaClientPoolException) {
            logger.error("xoa2 service=" + serviceId + " addr=" + signature
                    + " ex=" + "XoaClientPoolException");
            delta = 1;
        }

        if (delta <= 0) {
            delta = 1;
        }

        disableNode(serviceId, node, delta);
    }
}
