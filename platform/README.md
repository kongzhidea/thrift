# xoa框架简介
> xoa框架是一款基于thrift的rpc框架，在thrift基础上增加负载均衡，连接池，性能监控，通过动态代理使得类似调用本地方法一样与服务端通信。      
> 
> xoa框架 负载均衡策略默认采用 轮询算法， 也可以自定义使用随机或者加权轮询等。    
>    
> xoa框架将可用节点存储在zookeeper中，在每个服务对应的接口中加@XoaService注解，并且配置业务方的serviceId，需要保证每个业务方的serviceId不能重复。 在zk中约束 /thrift/${serviceId}/enable/${node1}，/thrift/${serviceId}/enable/${node2}... 路径存储可用节点， 其中node格式为 ip:port 
> 
> 对 /thrift/${serviceId}/enable节点监听其child节点变化，当有节点加入或删除时候，同步到本地内存。   
>      
> 调用thrift接口时候，通过jdk动态代理，在invoke方法中，首先通过当前方法来获取真正的thrift方法，然后 从本地内存中获取可用节点列表，如果是第一次调用则从zk中获取，然后同步到本地内存，均以serviceId为单位，然后对该serviceId对应的enable节点监听其child节点变化； 然后通过负载均衡算法获取一个可用节点，再对此节点建立连接池，使用时候从pool中borrow一个节点，再调用真是的thrift方法，调用结束后再return。
> 
> xoa框架中 对调用失败的节点 设置一个阈值，当失败次数超过阈值时候，则自动踢掉此节点。 这样就要求服务端的每个接口中 需要加上catch住所有异常，否则可能导致因为业务异常而踢掉所有节点，此时需要重新修改代码，然后再把节点再加入到enable中。
> 
> 上线时候，首先需要切环境，将zk中enable节点 迁移到disable节点中，disable节点仅做一个备份的作用，服务中不会从disable节点中取数据；  将zk中enable节点删除一个后，客户端在负载均衡时候不会再处理此节点，此时可以将此节点对应的服务上线，上线结束后 在从disable节点 迁移到enable节点，该节点上线完成，其他节点以此类推。
> 
> 服务端采用 THsHaServer， 序列化方式采用TCompactProtocol。



#### API:
```
@XoaService("com.rr.publik.service")
public interface IGameService extends GameService.Iface {

}
```

#### 调用形式
```
//采用默认的timeout，也可以自己设置timeout
IGameService client = ServiceFactory.getService(IGameService.class);
```


### thrift 服务端5中io模型
* TSimpleServer  IO   只接收一个请求。
* TThreadPoolServer  IO, 但是有请求过来使用线程池处理。
* TNonblockingServer   NIO，  一个线程处理。   select()方法，里面处理handleAccept，handleRead等。    handleAccept时候，将FrameBuffer通过 selectionKey.attachment方法传递到 handleRead中。
* THsHaServer   继承  TNonblockingServer ， NIO   一个selector  处理accept和读，利用线程池方式来处理读数据。
* TThreadedSelectorServer   NIO   一个selector线程处理accept， 多个selector处理 网络io， 利用线程池方式来处理读数据。

> NIO Server都继承  AbstractNonblockingServer，  启动时候调用serve方法，内部会调用 startThreads，模板方式来调动个子类的实现方法， 启动线程，向Selector上注册accept,read等事件。

### thrift 客户端

1. thrift在生成java文件的时候，会把定义的每个struct，以及service里面的 方法 封装后一个  method_args，  生成java文件， 同时 实现TBase接口，   每个类实现自己的实现 read和write接口。

2. 调用方法的时候，先传方法名，再传method_args（调用method_args.write(protocol) 方法，  每个类都自己实现序列化和反序列化 自身），再传 写方法结束， 最后调用TTranport.flush()。

3. 写结束后， 然后读数据：  首先 读messginBegin， 然后调用  method_result.read(protocol)方法，反序列化自身。 

4. 最后返回 结果。


```
write时候， 按照  thrift定义的 序号，  进行序列化，

read时候，也按照thrift定义的需要，进行 反序列化。



read,write 传入的是相同的 TProtocol， TProtocol定义了  writeStructBegin, writeFieldBegin, writeI32 等方法。

TProtocol  大概分为，  TJsonProtocol,  TBinaryProtocol（2进制传输），TCompactProtocol（紧凑2进制），

TBinaryProtocol中  

            writeI32：  把int转成4个字节。

            writeString ： 先写string的长度， 再写内容：string.getByte("utf-8")

            writeList时候，先写 List类型，再写List长度，  然后再写 列表内容。

TCompactProtocol:  http://www.tuicool.com/articles/JZRn2y   将内容进行压缩后再传输。



TProtocol中 调用 TTransport的 read和write方法，  例如，TSocket 建立socket链接(阻塞IO)，传输数据到服务端。

TSocket中，将socket 设置  setTcpNoDelay(true) ， 禁用纳格算法 ，  
TSocket继承 TIOStreamTransport，在open方法中，
    this.inputStream_ = new BufferedInputStream(this.socket_.getInputStream(), 1024);
    this.outputStream_ = new BufferedOutputStream(this.socket_.getOutputStream(), 1024);

```

