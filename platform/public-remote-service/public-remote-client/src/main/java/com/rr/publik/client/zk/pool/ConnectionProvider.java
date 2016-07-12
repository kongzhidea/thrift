package com.rr.publik.client.zk.pool;

/**
 * 
 * 
 * @author po.xu
 * @mail po.xu@renren-inc.com
 * @version 1.0
 */
public interface ConnectionProvider {
	/**
	 * 获取一个链接
	 * 
	 * @param identity
	 *            指定 ip:port
	 * 
	 * @return
	 */
	public GameConnection getConnection(String identity) throws ConnectionException;

	/**
	 * 获取一个链接 实现调度
	 * 
	 * @param identity
	 * 
	 * @return
	 * @throws ConnectionException 
	 */
	public GameConnection getConnection() throws ConnectionException;

	/**
	 * 返回链接<br>
	 * 
	 * 如果链接不是由 getConnection 返回的，则会抛出异常
	 * 
	 * @param gameConnection
	 */
	public void returnConnection(GameConnection connection);

	/**
	 * 销毁连接池
	 * 
	 * @param identity
	 */
	public void destroy(String identity);

	/**
	 * 更新Node节点
	 * 
	 * @return
	 */
	public void updateNodeList();
}
