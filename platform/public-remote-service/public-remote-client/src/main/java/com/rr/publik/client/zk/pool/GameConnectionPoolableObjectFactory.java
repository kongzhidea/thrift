package com.rr.publik.client.zk.pool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import com.rr.publik.api.GameService;
import com.rr.publik.api.GameService.Client;


/**
 * @author po.xu
 * @mail po.xu@renren-inc.com
 * @version 1.0
 */

class GameConnectionPoolableObjectFactory implements PoolableObjectFactory<GameConnection> {
	private Log logger = LogFactory.getLog(this.getClass().getName());

	private Node node;

    private int timeout;

    private boolean keepAlive = true;
    
    /**
     * @param node
     * @param timeout
     */
    public GameConnectionPoolableObjectFactory(Node node,
            int timeout) {
    	this.node = node;
        this.timeout = timeout;
    }
    
    /**
     * @param node
     * @param timeout
     * @param keepAlive
     */
    public GameConnectionPoolableObjectFactory(Node node,
            int timeout, boolean keepAlive) {
    	this.node = node;
        this.timeout = timeout;
        this.keepAlive = keepAlive;
    }


    @Override
    public void destroyObject(GameConnection object) throws Exception {
        try {
			TTransport transport = object.getTransport();
			if (transport != null && transport.isOpen()) {
			    transport.close();
			}
        } catch (Exception e) {
			if (logger.isErrorEnabled()){
				logger.error("destroyObject from pool error.", e);
			}
		}
    }

    @Override
    public GameConnection makeObject() throws Exception {
        try {
            TSocket socket = new TSocket(node.getHost(),
                    node.getPort(), timeout);
            socket.getSocket().setKeepAlive(true);
            TTransport transport = new TFramedTransport(socket);
            TProtocol protocol = new TCompactProtocol(transport);
            transport.open();
            Client client = createClient(protocol);
            return new GameConnection(node, client, socket);
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
	private Client createClient(TProtocol protocol) {
		return new GameService.Client(protocol);
	}

    @Override
    public boolean validateObject(GameConnection object) {
        try {
            TTransport transport = object.getTransport();

            if (transport.isOpen()) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void passivateObject(GameConnection object) throws Exception {
        // DO NOTHING
    }

    @Override
    public void activateObject(GameConnection object) throws Exception {
        // DO NOTHING
    }

	public Node getNode() {
		return node;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setNode(Node node) {
		this.node = node;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public boolean isKeepAlive() {
		return keepAlive;
	}

	public void setKeepAlive(boolean keepAlive) {
		this.keepAlive = keepAlive;
	}
	
	
}
