package com.rr.publik.client.zk.pool;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class Node {
	private String host;
	private int port;
	private int weight;
	private boolean healthy = true;

	public Node() {
	}

	public Node(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public boolean isHealthy() {
		return healthy;
	}

	public void setHealthy(boolean healthy) {
		this.healthy = healthy;
	}

	public int compareTo(Node o) {
		return this.weight - o.getWeight();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (!(o instanceof Node)) {
			return false;
		}
		Node otherNode = (Node) o;
		if (StringUtils.equals(host, otherNode.getHost())
				&& port == otherNode.getPort()) {
			return true;
		}
		return false;
	}

	public int hashCode() {
		return (host + ":" + port).hashCode();
	}

	public String toString() {
		return host + ":" + port + ",weight:" + weight + ",healthy:" + healthy;
	}

	public String getIdentity() {
		return host + ":" + port;
	}

	public static Node getNodeFromIdentity(String identity) {
		try {
			String[] conts = StringUtils.split(identity, ":");
			String host = conts[0];
			String port = conts[1];
			Node node = new Node(host, Integer.valueOf(port));
			return node;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static List<Node> getNodeFromIdentity(List<String> identities) {
		List<Node> nodes = new ArrayList<Node>();
		for (String identify : identities) {
			Node node = getNodeFromIdentity(identify);
			if (node != null) {
				nodes.add(node);
			}
		}
		return nodes;
	}

	public static boolean inNodes(List<String> nodeList, String identity) {
		if(identity == null || "".equals(identity)){
			return false;
		}
		for (String n : nodeList) {
			if (n.equals(identity)) {
				return true;
			}
		}
		return false;
	}
}