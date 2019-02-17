package com.rr.publik.client.demo;

import com.rr.publik.client.zk.pool.ZookeeperService;

public class ZKTest {
	public static void main(String[] args) {

//		ZookeeperService.getInstance().regWatcherOnLineRserver();
		try {
			ZookeeperService.getInstance().addNode(
					ZookeeperService.ENABLE_PATH, "localhost:9301", true);
//			ZookeeperService.getInstance().deleteNode(
//					ZookeeperService.ENABLE_PATH, "10.2.45.39:9301", true);
			// ZookeeperService.getInstance().updateState("10.2.45.39:9301");
			// System.out.println(ZookeeperService.getInstance().getStat(
			// ZookeeperService.ENABLE_PATH, "10.2.45.39:9301"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println(ZookeeperService.getInstance().getNodeList(
				ZookeeperService.ENABLE_PATH));
		System.exit(0);
	}
}
