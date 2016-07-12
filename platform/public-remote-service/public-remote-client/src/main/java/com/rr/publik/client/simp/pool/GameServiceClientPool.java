package com.rr.publik.client.simp.pool;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;

import com.rr.publik.api.GameService;
import com.rr.publik.api.GameService.Client;
import com.rr.publik.api.IntegerListRequest;
import com.rr.publik.api.StringMapResponse;

/**
 * thrift 连接池,初始化时候指定大小和服务地址，不支持服务平滑升级
 * 
 * @author kk
 * 
 */
public class GameServiceClientPool extends ThriftClientPool<GameService.Client>
		implements GameService.Iface {
	private static final Log logger = LogFactory
			.getLog(GameServiceClientPool.class);

	public GameServiceClientPool(List<String> adrs, int timeout) {
		super(adrs, timeout);
		logger.info("GameServiceClientPool created!");
	}

	public GameServiceClientPool(String adr, int timeout) {
		this(string2List(adr), timeout);
	}

	@Override
	protected Client createClient(TProtocol protocol) {
		return new GameService.Client(protocol);
	}

	@Override
	protected boolean match(String key, String identity) {
		return StringUtils.equalsIgnoreCase(getAddr(key), identity);
	}

	@Override
	public int handle(String identity, String tel, String message,
			Map<String, String> params) throws TException {
		int result = 0;
		Tuple<String, Client> tuple = null;
		Client client = null;
		try {
			logger.info("[ADU][handle]identity:" + identity + ", tel:" + tel
					+ ", message:" + message + ", params:" + params);

			tuple = getClient(identity);

			logger.info("[ADU][getClient]tuple=" + tuple);
			client = tuple.right;
			result = client.handle(identity, tel, message, params);
		} catch (TException e) {
			if (client != null && client.getInputProtocol() != null
					&& client.getInputProtocol().getTransport() != null) {
				client.getInputProtocol().getTransport().close();
				client.getInputProtocol().getTransport().open();
			}
			logger.error(e.getMessage(), e);
			throw e;
		} catch (Exception e) {
			logger.error(e, e);
		} finally {
			try {
				returnObject(tuple.left, tuple.right);
			} catch (Exception e) {
				logger.error(e, e);
			}
		}
		return result;
	}

	@Override
	public StringMapResponse sendMessages(IntegerListRequest req)
			throws TException {
		logger.info("[ADU][handle] req:" + req.toString());
		StringMapResponse result = null;
		Tuple<String, Client> tuple = null;
		Client client = null;
		try {
			tuple = getClient("");

			logger.info("[ADU][getClient]tuple=" + tuple);
			client = tuple.right;
			result = client.sendMessages(req);
		} catch (TException e) {
			if (client != null && client.getInputProtocol() != null
					&& client.getInputProtocol().getTransport() != null) {
				client.getInputProtocol().getTransport().close();
				client.getInputProtocol().getTransport().open();
			}
			logger.error(e.getMessage(), e);
			throw e;
		} catch (Exception e) {
			logger.error(e, e);
		} finally {
			try {
				returnObject(tuple.left, tuple.right);
			} catch (Exception e) {
				logger.error(e, e);
			}
		}
		return result;
	}

}
