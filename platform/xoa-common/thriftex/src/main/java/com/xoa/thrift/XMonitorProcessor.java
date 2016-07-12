package com.xoa.thrift;

import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.*;
import org.perf4j.StopWatch;
import org.perf4j.log4j.Log4JStopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 对生成代码Processor的封装，可以实现以下功能：
 * 1. 从输入协议中获取调用函数名称，函数内容可读数据等
 * 2. 从输入协议中获取调用者 ip:port 等信息
 * 3. TODO: 获取完整RPC协议数据
 *
 */
public class XMonitorProcessor implements TProcessor {
    private static Logger logger = LoggerFactory.getLogger(XMonitorProcessor.class);

    /**
     * 默认的慢函数时间<br>
     *     超过 timeoutInvoke 时间的函数调用会在log打印出来函数信息<br>
     *
     * TODO: 根据业务类型的不同在配置文件中传入
     */
    private final static long timeoutInvoke = 100L;

    /**
     * 代码生成的实际 Processor
     */
    private TProcessor realProcessor;

    public XMonitorProcessor(TProcessor realProcessor) {
    	this.realProcessor = realProcessor;
    }
    
    @Override
    public boolean process(TProtocol in, TProtocol out) throws TException {
        StopWatch stopWatch = new Log4JStopWatch();

        XReadableProtocol xoaProtocol = XReadableProtocol.getProtocol(in);
        //XReadableProtocol outProtocol = XReadableProtocol.getProtocol(out);

        boolean success = realProcessor.process(xoaProtocol, out);

        // 获取对端信息
        //String desc = in.getTransport().toString();

        StringBuilder key = new StringBuilder();
        key.append('.').append(xoaProtocol.getFuncName());

        long elapsed = stopWatch.getElapsedTime();

        if (elapsed > timeoutInvoke) {
            key.append(".SLOW");
            logger.warn(" called " + key + " time=" + elapsed + " [" + xoaProtocol.getReadableText() + "]");
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug(" called " + key + " time=" + elapsed + " [" + xoaProtocol.getReadableText() + "]");
            }
        }

        stopWatch.setTag(xoaProtocol.getFuncName());
        
        return success;
    }
}
