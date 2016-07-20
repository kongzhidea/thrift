# thrift
xoa通用客户端，

需要配置zk.properties

xoa-client  通用客户端

API:

@XoaService("com.rr.publik.service")
public interface IGameService extends GameService.Iface {

}





调用样例:

package com.kk.xoa.test;

import com.rr.publik.api.IGameService;
import com.rr.publik.api.IntegerListRequest;
import com.rr.publik.api.StringMapResponse;
import com.xoa.client.factory.ServiceFactory;
import org.apache.thrift.TException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameXoaTest {
    public static void main(String[] args) throws Exception {
        IGameService client = ServiceFactory.getService(IGameService.class);

        List<String> tels = new ArrayList<String>();
        tels.add("12345");
        tels.add("23456");
        tels.add("34567");
        IntegerListRequest req = new IntegerListRequest(tels);
        StringMapResponse ret = client.sendMessages(req);
        System.out.println(ret);

        for (int i = 0; i < 100; i++) {
            Thread thread = new Thread(new Task(client));
            thread.start();
        }

        for (int i = 0; i >= 0; i++) {
            Thread.sleep(3000);
        }

        System.exit(0);
    }
}

class Task implements Runnable {
    int i = 0;
    IGameService client;

    public Task(IGameService client) {
        this.client = client;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Map<String, String> params = new HashMap<String, String>();
                client.handle(Thread.currentThread().getName(), ""
                        + (i++), "test", params);
//                Thread.sleep(10000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}






Note：

缺少server容器



初始化zookeeper时候，  zookeeper和netflix jar包版本太低，会导致 getClient()时候，初始化比较慢，需要等5s左右，

修复方法：使用新版本 jar包，与zk版本一致， class包名有修改。 另外，使用时候 path必须 以"/"开头。



<dependency>
   <groupId>org.apache.zookeeper</groupId>
   <artifactId>zookeeper</artifactId>
   <version>3.4.6</version>
</dependency>
<dependency>
   <groupId>org.apache.curator</groupId>
   <artifactId>curator-framework</artifactId>
   <version>2.9.0</version>
</dependency>




需要注意地方：

 maven-thrift-plugin  版本修改 为  0.1.11



2. 如果项目引入了httpclient，注意版本，httpcore.jar  4.2.1和4.1.3 不兼容， 需要把4.1.3的去掉。