# thrift

xoa 服务端 通用打印所有请求。

    @Override
    protected TProcessor createProcessor() {
        return new XMonitorProcessor(new WebService.Processor(this));
        //return new WebService.Processor(this);
    }