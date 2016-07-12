package com.kk.xoa.web.client;

import com.kk.xoa.web.IWebService;
import com.xoa.client.factory.ServiceFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ClientFactory {
    private static Log logger = LogFactory.getLog(ClientFactory.class);
    private static IWebService client = null;

    public static IWebService getClient() {
        if (client == null) {
            synchronized (ClientFactory.class) {
                if (client == null) {
                    client = ServiceFactory.getService(IWebService.class,3000);
                }
            }
        }
        return client;
    }

}
