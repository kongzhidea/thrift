package com.rr.publik.client.zk.pool;

public class ConnectionException extends Exception {
	private String identify;

	public ConnectionException(String identify, String message, Throwable cause) {
		super(message, cause);
		this.identify = identify;
	}

	public String getIdentify() {
		return identify;
	}

	public void setIdentify(String identify) {
		this.identify = identify;
	}

}
