package com.rr.publik.client.zk.pool;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import com.rr.publik.api.GameService;
import com.rr.publik.api.IntegerListRequest;
import com.rr.publik.api.StringMapResponse;
import com.rr.publik.client.util.BaseEmailSend;

public class GameServiceClientPool implements GameService.Iface {

	/**
	 * 使用测试环境，指定服务地址，例如:
	 * 
	 * System.setProperty("com.rr.publik.service", "10.2.45.39:9301");
	 */
	public static final String serviceId = "com.rr.publik.service";

	private static final Log logger = LogFactory
			.getLog(GameServiceClientPool.class);
	private final GameConnectionProvider connectionProvider = new GameConnectionProvider();

	/** 错误次数 */
	private static int ERROR_COUNT_MAX = 200;
	private Map<String, Integer> errorIdentityCount = new ConcurrentHashMap<String, Integer>();

	public GameServiceClientPool() {
		monitor();
	}

	/**
	 * 监控 服务error情况，将error太多的服务踢掉
	 */
	private void monitor() {
		Thread t1 = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						for (String identity : errorIdentityCount.keySet()) {
							int errorCount = errorIdentityCount.get(identity);
							if (errorCount > ERROR_COUNT_MAX) {
								logger.error("identity ["
										+ identity
										+ "]'s error count has reached ERROR_COUNT_MAX("
										+ ERROR_COUNT_MAX + ")");
								errorIdentityCount.put(identity, 0);
								try {
									// zk节点变化
									ZookeeperService.getInstance().deleteNode(
											ZookeeperService.ENABLE_PATH,
											identity, true);
									ZookeeperService.getInstance().addNode(
											ZookeeperService.DISABLE_PATH,
											identity, false);
								} catch (Exception e) {
									logger.error(e.getMessage(), e);
								}

								String sms = String.format(
										"server %s error count reach %d",
										identity, errorCount);
								BaseEmailSend.send(
										"zhihui.kong@renren-inc.com",
										"game service error!", sms);
							}
						}
						Thread.sleep(5000);
					} catch (Exception e) {
						logger.error("catch error! " + e.getMessage(), e);
					}
				}
			}
		});
		t1.start();

	}

	public GameConnectionProvider getConnectionprovider() {
		return connectionProvider;
	}

	protected static List<String> string2List(String adr) {
		List<String> adrs = new ArrayList<String>();
		String[] tmps = StringUtils.split(adr, ",");
		for (String host : tmps) {
			adrs.add(host);
		}
		return adrs;
	}

	/**
	 * 增加错误次数
	 * 
	 * @param connection
	 */
	private void increErrorCount(String identity) {
		errorIdentityCount.put(identity, errorIdentityCount
				.containsKey(identity) ? errorIdentityCount.get(identity) + 1
				: 1);
	}

	private void dealHandleException(GameConnection connection, Exception e)
			throws TTransportException, TException {
		if (connection != null
				&& connection.getClient() != null
				&& connection.getClient().getInputProtocol() != null
				&& connection.getClient().getInputProtocol().getTransport() != null) {
			connection.getClient().getInputProtocol().getTransport().close();
			connection.getClient().getInputProtocol().getTransport().open();
		}
		logger.error("handle transport exception:" + e.getMessage(), e);
		// 增加错误次数
		if (connection != null) {
			increErrorCount(connection.getNode().getIdentity());
		}
		throw new TException("get/handle client by key[" + connection
				+ "] error", e);
	}

	private void returnPool(GameConnection connection) throws TException {
		try {
			connectionProvider.returnConnection(connection);
		} catch (Exception e) {
			logger.error("return connection error.", e);
			throw new TException("return connection by key[" + connection
					+ "] error", e);
		}
	}

	private void dealConnectException(ConnectionException e) throws TException {
		logger.error("handle transport exception:" + e.getMessage(), e);
		// 增加错误次数
		increErrorCount(e.getIdentify());
		throw new TException("get/handle client by key[" + e.getIdentify()
				+ "] error", e);
	}

	@Override
	public int handle(String identity, String tel, String message,
			Map<String, String> params) throws TException {
		int result = 0;
		GameConnection connection = null;
		try {
			// logger.info("[ADU][handle]identity:" + identity + ", tel:" + tel
			// + ", message:" + message + ", params:" + params);
			connection = connectionProvider.getConnection();
			result = connection.getClient().handle(identity, tel, message,
					params);
		} catch (ConnectionException e) {
			dealConnectException(e);
		} catch (Exception e) {
			dealHandleException(connection, e);
		} finally {
			returnPool(connection);
		}
		return result;
	}

	@Override
	public StringMapResponse sendMessages(IntegerListRequest req)
			throws TException {
		StringMapResponse result = null;
		GameConnection connection = null;
		try {
			// logger.info("[ADU][handle] req:" + req.toString());
			connection = connectionProvider.getConnection();
			result = connection.getClient().sendMessages(req);
		} catch (ConnectionException e) {
			dealConnectException(e);
		} catch (Exception e) {
			dealHandleException(connection, e);
		} finally {
			returnPool(connection);
		}
		return result;
	}

}
