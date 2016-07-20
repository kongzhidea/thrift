# thrift
1.simple ，使用连接池，  但是无法切环境

2.基于zookeeper,连接池的thrift工程，上线可切环境。 支持 java系统变量还绑环境
需要配置 zk.properties

执行  python xoa_tool.py  com.rr.publik.service create localhost:9301  创建节点