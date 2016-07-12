package com.rr.publik.client.demo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.thrift.TException;

import com.rr.publik.api.IntegerListRequest;
import com.rr.publik.api.StringMapResponse;
import com.rr.publik.client.simp.pool.GameServiceClientPool;

public class SimpClientDemo {
	public static void main(String[] args) throws Exception {
		 String hosts = "localhost:9301";
		GameServiceClientPool gameServiceClient = new GameServiceClientPool(
				hosts, 5000);
		// Map<String, String> params = new HashedMap();
		// int ret = gameServiceClient.handle("", "18612566582", "我是来测试的1",
		// params);
		List<String> tels = new ArrayList<String>();
		tels.add("12345");
		tels.add("23456");
		tels.add("34567");
		IntegerListRequest req = new IntegerListRequest(tels);
		StringMapResponse ret = gameServiceClient.sendMessages(req);
		System.out.println(ret);
		for (int i = 0; i < 10; i++) {
			Map<String, String> params = new HashMap<String, String>();
			gameServiceClient.handle("", "186", "test", params);
			Thread.sleep(100);
		}

	}
}
