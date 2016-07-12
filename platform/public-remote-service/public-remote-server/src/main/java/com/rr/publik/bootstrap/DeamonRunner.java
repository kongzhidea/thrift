package com.rr.publik.bootstrap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author po.xu
 */
public class DeamonRunner {
	private static final Log LOG = LogFactory.getLog(DeamonRunner.class);

    public static void main(String[] args) {
        // let the server process shut down in case of any exception raised in
        // any threads. the process will not stop unless an uncaught-exception
        // handler is set. This can allow discovering of severe server crashes
        // such as OOM and null-pointers.
        Thread.setDefaultUncaughtExceptionHandler( // NL
                new Thread.UncaughtExceptionHandler() {
                    public void uncaughtException(Thread t, Throwable e) {
                        LOG.error(e.getMessage(), e);
                    }
                });

        String conf = "server.xml";
        String beanName = "serverStart";

        AppMain app = null;

        try {
            BeanFactory factory = new ClassPathXmlApplicationContext(conf);
            app = (AppMain) factory.getBean(beanName);
        } catch (Throwable e) {
            LOG.fatal("Error from Spring during initialization: "
                    + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        app.doMain(args);

    }
}
