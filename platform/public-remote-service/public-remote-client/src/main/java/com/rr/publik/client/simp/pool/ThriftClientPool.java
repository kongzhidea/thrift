package com.rr.publik.client.simp.pool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.KeyedPoolableObjectFactory;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

/**
 * a thrift client pool based on @link{GenericKeyedObjectPool}
 * 
 * 将所有的server都轮循加入到一个pool中，提前加入到pool中。
 * 
 * @author kk
 * @param <T>
 */
public abstract class ThriftClientPool<T extends TServiceClient> {
	private static final Log LOG = LogFactory.getLog(ThriftClientPool.class);

	private GenericKeyedObjectPool<String, T> clientPool;

	private static int MAX_TRY_TIMES = 3;

	// TODO 200
	private int maxClient = 4;

	/** 可以从缓存池中分配对象的最大数量 */
	private static int maxActive = 200;

	/** 缓存池中最大空闲对象数量 */
	private static int maxIdle = 40;

	/** 缓存池中最小空闲对象数量 */
	private static int minIdle = 10;

	/** 最多等待多少毫秒 */
	private static long maxWait = 10;

	/** 从缓存池中分配对象，是否执行PoolableObjectFactory.validateObject方法 */
	private boolean testOnBorrow = false;

	private boolean testOnReturn = false;

	private boolean testWhileIdle = false;

	private int timeout;

	private List<String> addresses;

	private List<Tuple<String, Integer>> keys;

	private Iterator<String> curAdress;

	public ThriftClientPool(List<String> adrs, int timeout) {
		if (adrs == null || adrs.isEmpty()) {
			throw new IllegalArgumentException(
					"Addresses should contain one adress at least.");
		}
		addresses = new ArrayList<String>(adrs);
		curAdress = addresses.iterator();
		this.timeout = timeout;
		clientPool = new GenericKeyedObjectPool<String, T>(
				new PooleableThriftFactory());
		clientPool.setMaxActive(maxActive);
		clientPool.setMaxIdle(maxIdle);
		clientPool.setMinIdle(minIdle);
		clientPool.setMaxWait(maxWait);
		clientPool.setTestOnBorrow(testOnBorrow);
		clientPool.setTestOnReturn(testOnReturn);
		clientPool.setTestWhileIdle(testWhileIdle);

		keys = new ArrayList<Tuple<String, Integer>>();
		for (int i = 0; i < maxClient; i++) {
			if (!curAdress.hasNext()) {
				// go to head
				curAdress = this.addresses.iterator();
			}
			if (curAdress.hasNext()) {
				String key = curAdress.next() + "-" + i;
				try {
					clientPool.addObject(key);
					keys.add(new Tuple<String, Integer>(key, 0));
					LOG.info("connection with key [" + key + "] created!");
				} catch (Exception e) {
					LOG.error(e, e);
				}
			}
		}
	}

	protected static List<String> string2List(String adr) {
		List<String> adrs = new ArrayList<String>();
		String[] tmps = adr.split(",");
		for (String host : tmps) {
			adrs.add(host);
		}
		return adrs;
	}

	protected abstract T createClient(TProtocol protocol);

	private AtomicInteger counter = new AtomicInteger();

	/**
	 * <b>使用完成之后必须使用returnObject方法返还client</b>
	 * 
	 * @param identity
	 *            用与对连接池中连接的区分,如果传空，则顺序取,如果不为空，则identify必须再初始化的hosts中
	 * @return
	 * @throws Exception
	 */
	public Tuple<String, T> getClient(String identity) throws Exception {
		if (counter.get() > Integer.MAX_VALUE - 100000) { // 防上溢，打出点提前量
			// 归零
			counter.set(0);
		}
		if (StringUtils.isBlank(identity)) {
			Tuple<String, Integer> tuple = keys.get(counter.addAndGet(1)
					% keys.size());
			T client = clientPool.borrowObject(tuple.left);
			return new Tuple<String, T>(tuple.left, client);
		}
		for (Tuple<String, Integer> key : keys) {
			if (match(key.left, identity) || StringUtils.isBlank(identity)) {
				key.right++;
				T client = clientPool.borrowObject(key.left);
				if (client == null) {
					int trytime = MAX_TRY_TIMES;
					while (client == null && trytime > 0) {
						clientPool.addObject(key.left);
						client = clientPool.borrowObject(key.left);
						--trytime;
						LOG.info("try borrow :" + key.left);
					}
				}
				return new Tuple<String, T>(key.left, client);
			}
		}
		return null;
	}

	/**
	 * proxy dispatch by <b>key</b>
	 * 
	 * @param key
	 * <br/>
	 *            format : <b>host:port-index</b>
	 * @param identity
	 * <br/>
	 *            用与对连接池中连接的区分
	 * @return
	 */
	protected abstract boolean match(String key, String identity);

	public void returnObject(String key, T t) throws Exception {
		clientPool.returnObject(key, t);
	}

	public String getAddr(String key) {
		return key.split("-")[0];
	}

	// close socket connection when finalize this proxy
	protected void finalize() throws Throwable {
		super.finalize();
		clientPool.clear();
	}

	class PooleableThriftFactory implements
			KeyedPoolableObjectFactory<String, T> {

		@Override
		public void activateObject(String key, T client) throws Exception {
		}

		@Override
		public void destroyObject(String key, T client) throws Exception {
			try {
				synchronized (client) {
					TTransport transport = client.getInputProtocol()
							.getTransport();
					if (transport != null && transport.isOpen()) {
						try {
							transport.close();
						} catch (Exception e) {
							LOG.info("Could not close socket.", e);
						}
					}
					transport = client.getOutputProtocol().getTransport();
					if (transport != null && transport.isOpen()) {
						try {
							transport.close();
						} catch (Exception e) {
							LOG.info("Could not close socket.", e);
						}
					}
				}
				LOG.info("destroy key : " + key);
			} catch (Exception e) {
				throw e;
			}
		}

		@Override
		public T makeObject(String key) throws Exception {
			T client = null;
			try {
				String adr = key.split("-")[0];
				String host = adr.split(":")[0];
				int port = Integer.valueOf(adr.split(":")[1]);
				TSocket socket = new TSocket(host, port);
				socket.setTimeout(timeout);
				TTransport transport = new TFramedTransport(socket);
				TProtocol protocol = new TCompactProtocol(transport);
				transport.open();
				client = createClient(protocol);
			} catch (Exception e) {
				client = null;
				LOG.error("make object with key [" + key + "] error!", e);
				throw e;
			}
			return client;
		}

		@Override
		public void passivateObject(String key, T client) throws Exception {

		}

		@Override
		public boolean validateObject(String key, T client) {
			boolean res = false;
			try {
				res = client != null

				&& client.getInputProtocol().getTransport() != null
						&& client.getInputProtocol().getTransport().isOpen()
						&& client.getOutputProtocol().getTransport() != null
						&& client.getOutputProtocol().getTransport().isOpen();
			} catch (Exception e) {
				res = false;
				LOG.error(e, e);
			}
			LOG.debug("KEY : " + key + " : validate = " + res);
			return res;
		}
	}

}
