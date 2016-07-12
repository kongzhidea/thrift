# thrift
xoaͨ�ÿͻ��ˣ�


xoa-client  ͨ�ÿͻ���

API:

@XoaService("com.rr.publik.service")
public interface IGameService extends GameService.Iface {

}





��������:

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






Note��

ȱ��server����



��ʼ��zookeeperʱ��  zookeeper��netflix jar���汾̫�ͣ��ᵼ�� getClient()ʱ�򣬳�ʼ���Ƚ�������Ҫ��5s���ң�

�޸�������ʹ���°汾 jar������zk�汾һ�£� class�������޸ġ� ���⣬ʹ��ʱ�� path���� ��"/"��ͷ��



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




��Ҫע��ط���

 maven-thrift-plugin  �汾�޸� Ϊ  0.1.11



2. �����Ŀ������httpclient��ע��汾��httpcore.jar  4.2.1��4.1.3 �����ݣ� ��Ҫ��4.1.3��ȥ����