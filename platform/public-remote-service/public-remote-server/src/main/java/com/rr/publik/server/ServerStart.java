package com.rr.publik.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.stereotype.Service;

import com.rr.publik.bootstrap.AppMain;

@Service
public class ServerStart implements AppMain {
	private static final Log logger = LogFactory.getLog(ServerStart.class);

	@Autowired
	GameServiceImpl gameService;

	@Override
	public void doMain(String[] argv) {
		logger.info("starting server.........");
		long start = System.currentTimeMillis();

		// ...............
		gameService.start();

	}

}
