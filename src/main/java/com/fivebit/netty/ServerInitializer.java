package com.fivebit.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by fivebit on 2017/7/4.
 */
@Component("serverInitializer")
public class ServerInitializer extends ChannelInitializer<SocketChannel> {

    @Autowired

    GateWayServerHandler gateWayServerHandler;

    private SslContext sslCtx = null;

    public ServerInitializer() {
    }
    //最后一次初始化的机会了。
    public void intParams(Map<String,Object> params){
        this.sslCtx = null;
    }

    @Override
    public void initChannel(SocketChannel ch) throws CloneNotSupportedException {
        ChannelPipeline p = ch.pipeline();
        if (sslCtx != null) {
            /*
            SSLEngine engine = SecureChatSslContextFactory.getServerContext().createSSLEngine();
            engine.setNeedClientAuth(true); //ssl双向认证
            engine.setUseClientMode(false);
            engine.setWantClientAuth(true);
            engine.setEnabledProtocols(new String[]{"SSLv3"});
            */
            p.addLast(sslCtx.newHandler(ch.alloc()));
        }
        p.addLast(new HttpServerCodec());/*HTTP 服务的解码器*/
        p.addLast("aggregator",new HttpObjectAggregator(10240));/*HTTP 消息的合并处理*/
        //http服务器端对request解码
        p.addLast("decoder", new HttpRequestDecoder());
        //http服务器端对response编码
        //p.addLast("encoder", new HttpResponseEncoder());
        //p.addLast("compressor", new HttpContentCompressor());
        //p.addLast("deflater", new HttpContentCompressor());
        //p.addLast(new ApiGateWayServerHandler());
        p.addLast("handler",gateWayServerHandler.clone());
    }
}
