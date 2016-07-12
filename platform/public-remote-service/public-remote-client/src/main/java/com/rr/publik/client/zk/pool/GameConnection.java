package com.rr.publik.client.zk.pool;

import org.apache.thrift.transport.TTransport;

import com.rr.publik.api.GameService.Client;

/**
 * @author po.xu
 * @mail po.xu@renren-inc.com
 * @version 1.0
 * 
 * 
 */

public class GameConnection {
	private Client client;
	private TTransport transport;
	private Node node;
	private boolean disabled = false;

	public GameConnection(Node node, Client client, TTransport transport) {
		this.node = node;
		this.client = client;
		this.transport = transport;
		this.disabled = false;
	}

	public GameConnection(Node node, Client client, TTransport transport,
			boolean disabled) {
		this.node = node;
		this.client = client;
		this.transport = transport;
		this.disabled = disabled;
	}

	public TTransport getTransport() {
		return transport;
	}

	public void setTransport(TTransport transport) {
		this.transport = transport;
	}

	public Client getClient() {
		return client;
	}

	public void setClient(Client client) {
		this.client = client;
	}

	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	@Override
	public String toString() {
		return "GameConnection [node=" + node + "]";
	}

}
