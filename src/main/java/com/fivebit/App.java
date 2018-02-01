package com.fivebit;

import com.google.common.collect.Maps;
import com.fivebit.netty.EntryPoint;
import com.fivebit.utils.Jlog;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Map;

/**
 * Hello world!
 *
 */
public class App 
{
    /*是否使用https协议*/
    static final boolean SSL = System.getProperty("ssl") != null;
    static final int PORT = Integer.parseInt(System.getProperty("port", SSL? "8443" : "8899"));
    static final String IP = "127.0.0.1";
    static final Integer BIZGROUPSIZE = Runtime.getRuntime().availableProcessors()*2; //默认
    static final Integer BIZTHREADSIZE = 4;
    public static void main(String[] args) throws Exception {
        ApplicationContext ctx= new ClassPathXmlApplicationContext("applicationContext.xml");
        Jlog.info("load application and begin startup service");
        EntryPoint entryPoint = (EntryPoint)ctx.getBean("entryPoint");
        Map<String,Object> params = Maps.newHashMap();
        params.put("ip",IP);
        params.put("port",PORT);
        params.put("ssl",false);
        params.put("boos_group_size",BIZGROUPSIZE);
        params.put("worker_group_size",BIZTHREADSIZE);
        entryPoint.init(params);
        entryPoint.start();

    }
}
