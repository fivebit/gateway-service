package com.fivebit.netty;

import com.fivebit.utils.Jlog;
import com.fivebit.utils.Slog;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by fivebit on 2017/7/4.
 * netty 服务端入口
 */
@Component("entryPoint")
public class EntryPoint {

    private Integer boos_group_size = 2;
    private Integer worker_group_size = 4;
    private String ip = "127.0.0.1";
    private Integer port =  8980;
    private Boolean ssl = false;

    @Autowired
    private ServerInitializer serverInitializer;
    @Autowired
    private Slog slog;

    public EntryPoint(){
    }

    /**
     * 初始化服务配置
     * @param params
     */
    public void init(Map<String,Object> params){
        if(params.containsKey("ip")){
            this.ip = params.get("ip").toString();
        }
        if(params.containsKey("port")){
            this.port = Integer.parseInt(params.get("port").toString());
        }
        slog.info("init service finish:"+params);
    }
    public void start()throws Exception {
        // Configure the server.
        final SslContext sslCtx;
        if (ssl == true) {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
        } else {
            sslCtx = null;
        }
        EventLoopGroup bossGroup = new NioEventLoopGroup(boos_group_size);
        EventLoopGroup workerGroup = new NioEventLoopGroup(worker_group_size);
        serverInitializer.intParams(null);
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 1024);
            b.group(bossGroup, workerGroup);
            b.channel(NioServerSocketChannel.class); // 设置nio类型的channel
            b.handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(serverInitializer);
            ChannelFuture f = b.bind(ip, port).sync(); // 设置监听端口
            slog.info("netty service started: " + (ssl ? "https" : "http") + "://" + ip + ":" + port);
            f.channel().closeFuture().sync();
        }catch (Exception ee){
            slog.error("netty service error:"+ee.getMessage());
            slog.info("netty service end: "+(ssl? "https" : "http")+"://"+ip+":"+port);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
