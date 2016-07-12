package com.rr.publik.client.zk.pool;

import java.util.List;

/**
 * 用于封装负载均衡逻辑 <br>
 */
public interface LoadBalancer {

	/**
	 * 给定serviceId，返回一个负载均衡后的节点
	 * 
	 * @param serviceId
	 * @param nodes
	 * @return
	 */
	public Node getNode(List<Node> nodes);

}
