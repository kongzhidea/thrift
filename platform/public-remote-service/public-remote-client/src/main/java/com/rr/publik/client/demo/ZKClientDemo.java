package com.rr.publik.client.demo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.thrift.TException;

import com.rr.publik.api.IntegerListRequest;
import com.rr.publik.api.StringMapResponse;
import com.rr.publik.client.zk.pool.GameConnectionProvider;
import com.rr.publik.client.zk.pool.GameServiceClientPool;

public class ZKClientDemo {
	public static void main(String[] args) throws Exception {

//		 System.setProperty("xoa.hosts.com.rr.publik.service", "localhost:9301");

		GameServiceClientPool gameServiceClient = new GameServiceClientPool();
		List<String> tels = new ArrayList<String>();
		tels.add("12345");
		tels.add("23456");
		tels.add("34567");
		IntegerListRequest req = new IntegerListRequest(tels);
		StringMapResponse ret = gameServiceClient.sendMessages(req);
		System.out.println(ret);

		for (int i = 0; i < 100; i++) {
			Thread thread = new Thread(new Task(gameServiceClient));
			thread.start();
		}

		for (int i = 0; i >= 0; i++) {
			gameServiceClient.getConnectionprovider();
			System.out.println(GameConnectionProvider
					.getConnectionStatus());
			Thread.sleep(3000);
		}

		System.exit(0);
	}
}

class Task implements Runnable {
	int i = 0;
	GameServiceClientPool gameServiceClient;

	public Task(GameServiceClientPool gameServiceClient) {
		this.gameServiceClient = gameServiceClient;
	}

	@Override
	public void run() {
		while (true) {
			try {
				Map<String, String> params = new HashMap<String, String>();
				gameServiceClient.handle(Thread.currentThread().getName(), ""
						+ (i++), "test", params);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
}