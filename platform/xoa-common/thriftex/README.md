# thrift

xoa ����� ͨ�ô�ӡ��������

    @Override
    protected TProcessor createProcessor() {
        return new XMonitorProcessor(new WebService.Processor(this));
        //return new WebService.Processor(this);
    }