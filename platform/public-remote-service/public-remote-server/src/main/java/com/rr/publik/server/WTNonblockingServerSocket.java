package com.rr.publik.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TNonblockingSocket;
import org.apache.thrift.transport.TTransportException;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;

public class WTNonblockingServerSocket extends TNonblockingServerSocket {
    private static Log logger = LogFactory.getLog(WTNonblockingServerSocket.class);

    public WTNonblockingServerSocket(int port) throws TTransportException {
        super(port);
    }

    public WTNonblockingServerSocket(int port, int clientTimeout) throws TTransportException {
        super(port, clientTimeout);
    }

    public WTNonblockingServerSocket(InetSocketAddress bindAddr) throws TTransportException {
        super(bindAddr);
    }

    public WTNonblockingServerSocket(InetSocketAddress bindAddr, int clientTimeout) throws TTransportException {
        super(bindAddr, clientTimeout);
    }

    // 建立连接时候 获取客户端Ip
    @Override
    protected TNonblockingSocket acceptImpl() throws TTransportException {
        TNonblockingSocket tsocket = super.acceptImpl();
        SocketChannel socketChannel = tsocket.getSocketChannel();
        Socket socket = socketChannel.socket();
        String ip = socket.getInetAddress().getHostAddress();
        logger.info("connect.ip=" + ip);
        return tsocket;
    }
}
