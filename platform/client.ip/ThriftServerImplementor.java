package com.kk.server;

import java.net.Socket;
import java.util.Calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.server.THsHaServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

/**
 * abstract thrift based rpc server basic implementation
 */
public abstract class ThriftServerImplementor {
    private static final Log LOG = LogFactory.getLog(ThriftServerImplementor.class);

    private int port;

    private int threadNum;

    private String version;

    private String serverName;

    public String getVersion() {
        return version;
    }


    public void setVersion(String version) {
        this.version = version;
    }


    public String getServerName() {
        return serverName;
    }


    public void setServerName(String serverName) {
        this.serverName = serverName;
    }


    public int getPort() {
        return port;
    }


    public int getThreadNum() {
        return threadNum;
    }


    /**
     * callback before server started
     */
    public boolean beforeStart() {
        return true;
    }

    /**
     * check server status when starting
     *
     * @return server will be halted if not healthy
     */
    protected boolean check() {
        return true;
    }

    /**
     * callback after server started
     */
    public void afterStart() {
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setThreadNum(int threadNum) {
        this.threadNum = threadNum;
    }

    abstract protected TProcessor createProcessor();


    public void start() {
        doStart();
        // make this thread as daemon thread
        while (true) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                LOG.info("rpc server deamon thread is interrupted");
                break;
            }
        }
        LOG.info("rpc server stopped.");
    }


    protected void doStart() {
        Thread daemonThread = new Thread(new Runnable() {
            public void run() {
                TNonblockingServerSocket socket;
                try {
                    socket = new WTNonblockingServerSocket(port);
                    THsHaServer.Args options = new THsHaServer.Args(socket);
                    LOG.info("start service with worker threads: "
                            + threadNum);
                    options.workerThreads(threadNum);
//						options.workerThreads = threadNum;
                    TProcessor processor = createProcessor();
                    options.processor(processor);
                    options.protocolFactory(new TCompactProtocol.Factory());

                    TServer server = new THsHaServer(options);
                    server.serve();
                } catch (Exception e) {
                    LOG.error("daemon thread exception", e);
                    throw new RuntimeException(e);
                }
            }
        });
        daemonThread.start();
        LOG.info("server-name:" + serverName + ",port=" + port);
        LOG.info("copyright "
                + Calendar.getInstance().get(Calendar.YEAR));
        LOG.info("version " + version);
        LOG.info("-------------------------");
        LOG.info("server started on port " + port + " with "
                + threadNum + " handlers");

        afterStart();
    }


}
