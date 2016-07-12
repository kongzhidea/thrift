package com.rr.publik.client.demo;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.map.HashedMap;
import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.async.TAsyncClientManager;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingSocket;
import org.apache.thrift.transport.TNonblockingTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import com.rr.publik.api.GameService;
import com.rr.publik.api.GameService.Client;

/**
 * blog http://www.micmiu.com
 * 
 * @author Michael
 * 
 */
public class TestClientDemo {

	public static void main(String[] args) {
		try {
			String host = "127.0.0.1";
			int port = 9301;
			TSocket socket = new TSocket(host, port);
			socket.setTimeout(500);
			TTransport transport = new TFramedTransport(socket);
			TProtocol protocol = new TCompactProtocol(transport);
			transport.open();

			// ............
			Client client = new GameService.Client(protocol);
			Map<String, String> params = new HashMap<String, String>();
			int ret = client.handle("", "186", "test", params);
			System.out.println(ret);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}