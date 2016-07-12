package com.rr.publik.server;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;

import com.rr.publik.api.GameService;
import com.rr.publik.api.IntegerListRequest;
import com.rr.publik.api.StringMapResponse;

/**
 * thrift的服务端
 * 
 * @author kk
 * 
 */
public class GameServiceImpl extends ThriftServerImplementor implements
		GameService.Iface {
	protected static Log logger = LogFactory.getLog(GameServiceImpl.class
			.getName());

	@Override
	public void afterStart() {
	}

	@Override
	protected TProcessor createProcessor() {
		return new GameService.Processor(this);
	}

	@Override
	public int handle(String identity, String tel, String message,
			Map<String, String> params) throws TException {
		logger.info("identify:" + identity + ", tel:" + tel + ", message"
				+ message + ",params:" + params);
		try {
			Thread.sleep(40);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public StringMapResponse sendMessages(IntegerListRequest req)
			throws TException {
		Map<String, String> result = new HashMap<String, String>();
		for (String tel : req.getResult()) {
			result.put(tel, "--" + tel);
		}
		StringMapResponse ret = new StringMapResponse(result);
		return ret;
	}

}
