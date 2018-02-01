package com.fivebit.netty;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.fivebit.errorhander.AppException;
import com.fivebit.service.RouterServer;
import com.fivebit.utils.Jlog;
import com.fivebit.utils.Slog;
import com.fivebit.utils.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.EndOfDataDecoderException;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.ErrorDataDecoderException;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;
import io.netty.util.CharsetUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

/**
 * Created by fivebit on 2017/7/4.
 */
//@ChannelHandler.Sharable
@Component("gateWayServerHandler")
public class GateWayServerHandler extends SimpleChannelInboundHandler<HttpObject> implements Cloneable{

    @Autowired
    Slog slog;

    @Autowired
    private RouterServer routerServer;

    private HttpRequest request;
    private boolean readingChunks;
    private static final HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE); //Disk
    private HttpPostRequestDecoder decoder;
    private String req_id = "";             //请求的唯一ID
    //channle主动关闭之后(页面关闭),触发channelInactive事件，时调用。
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (decoder != null) {
            decoder.cleanFiles();
        }
        slog.debug("channelInactive end");
    }
    public void messageReceived(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        slog.debug("message received begin:"+msg.getClass().getName());
        if (msg instanceof HttpRequest == false) {
            slog.error("not http request:"+msg.toString());
            Ncom.writeErrorResp(ctx.channel(),request,"request not restful");
            return;
        }
        HttpRequest request = this.request = (HttpRequest) msg;
        URI uri = new URI(request.getUri());
        HttpMethod method = request.getMethod();            //请求的方法
        Map<String,Object> body_params = Maps.newHashMap(); //请求的参数
        Map<String,String> header_params = Maps.newHashMap();   //请求的头部
        String resouce = uri.getPath();                     //请求的资源
        if (resouce.equals("/favicon.ico")) {
            return;
        }
        if (resouce.equals("/")) {      //访问根路径
            Ncom.writeResponse(ctx.channel(),request,Utils.getRespons("route not match,please user other"));
            return;
        }
        if( resouce.equals("/nav")) {      //访问服务列表

        }
        if( resouce.equals("/info")) {      //查看基本信息

        }
        if( resouce.equals("/check")) {      //check

        }
        slog.info("message received begin:"+Ncom.makeAccessLog(request));
        //add header to body params
        for (Map.Entry<String, String> entry : request.headers()) {
            header_params.put(entry.getKey(), entry.getValue());
        }


        if (method.equals(HttpMethod.GET)) {
            // GET Method: should not try to create a HttpPostRequestDecoder
            //Ncom.writeResponse(ctx.channel(),request,Utils.getRespons("OK"));
            /**
             * List<String>表示当参数相同时，把相同的参数的值放在list中
             */
            QueryStringDecoder decoderQuery = new QueryStringDecoder(request.getUri());
            Map<String, List<String>> uriAttributes = decoderQuery.parameters();
            for (Entry<String, List<String>> attr : uriAttributes.entrySet()) {
                for (String attrVal : attr.getValue()) {
                    //responseContent.append("URI: " + attr.getKey() + '=' + attrVal + "\r\n");
                }
            }
        }else if (method.equals(HttpMethod.POST) || method.equals(HttpMethod.PUT)) {
            String content_type = Ncom.getContentType(request);
            try { //通过HttpDataFactory和request构造解码器
                decoder = new HttpPostRequestDecoder(factory, request);
            } catch (ErrorDataDecoderException e1) {
                slog.error("create HttpPostRequestDecoder error:"+e1.getMessage());
                Ncom.writeErrorResp(ctx.channel(),request,"request decode error");
                return;
            }
            readingChunks = HttpHeaders.isTransferEncodingChunked(request);
            slog.info("http post method content_type:"+content_type+" ismultipart:"+decoder.isMultipart() +" is chunked:"+readingChunks);
            if (readingChunks) {
                readingChunks = true;
            }
        }else if ( method.equals(HttpMethod.DELETE)) {

        }else {
            Ncom.writeErrorResp(ctx.channel(),request,"not support this method:"+method);
            return;
        }
        if (decoder != null) {
            String content_type = Ncom.getContentType(request);
            if(content_type.equals("json")){
                FullHttpRequest req = (FullHttpRequest) msg;//客户端的请求对象
                JSONObject requestJson = null;
                try{
                    requestJson = JSONObject.parseObject(parseJosnRequest(req));
                }catch(Exception e) {
                    Ncom.writeErrorResp(ctx.channel(),request,"json param error");
                    return;
                }
                body_params = (Map)requestJson;
                slog.debug("json param:"+body_params);
            }else {
                if (msg instanceof HttpContent) {
                    slog.debug("message received begin:" + msg.getClass().getName());
                    HttpContent chunk = (HttpContent) msg;
                    try {
                        decoder.offer(chunk);
                    } catch (ErrorDataDecoderException e1) {
                        slog.error("decoder offer error:" + e1.getMessage());
                        Ncom.writeErrorResp(ctx.channel(), request, "request decode error");
                        return;
                    }
                    try {
                        while (decoder.hasNext()) {
                            InterfaceHttpData data = decoder.next();
                            if (data != null) {
                                try {
                                    body_params.putAll(getHttpData(data));
                                } finally {
                                    data.release();
                                }
                            }
                        }
                    } catch (EndOfDataDecoderException e1) {
                        slog.debug(" get from http data end:");
                    }
                    slog.debug("get from decoder:" + body_params.toString());
                    // example of reading only if at the end
                    if (chunk instanceof LastHttpContent) {
                        slog.debug("last http content");
                        readingChunks = false;
                        reset();
                    }
                }
            }
        }
        slog.info("message received end:"+Ncom.makeAccessLog(request));

        String resource_ret = "";
        try {
            resource_ret = routerServer.route(request, method, resouce,header_params, body_params);
        }catch (AppException ee){
            resource_ret = ee.toJson().toJSONString();
        }

        //call api server
        Ncom.writeResponse(ctx.channel(),request,resource_ret);
    }

    // destroy the decoder to release all resources
    private void reset() {
        decoder.destroy();
        decoder = null;
    }

    /**
     * HttpDataType有三种类型
     * Attribute, FileUpload, InternalAttribute
     */
    private Map<String,Object> getHttpData(InterfaceHttpData data) {
        Map<String,Object> rets = Maps.newHashMap();
        if (data.getHttpDataType() == HttpDataType.Attribute) {
            Attribute attribute = (Attribute) data;
            try {
                rets.put(attribute.getName(),attribute.getValue());
            } catch (IOException e1) {
                slog.error("get key value from http data error:"+e1.getMessage());
                return rets;
            }
        }
        return rets;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.channel().close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        messageReceived(ctx, msg);
    }
    @Override
    public GateWayServerHandler clone() throws CloneNotSupportedException {
        GateWayServerHandler gateWayServerHandler =  (GateWayServerHandler) super.clone();
        gateWayServerHandler.request = null;
        gateWayServerHandler.decoder = null;
        gateWayServerHandler.req_id = "";
        slog.debug("GateWayServerHandler clone end");
        return gateWayServerHandler;
    }
    private String parseJosnRequest(FullHttpRequest request) {
        ByteBuf jsonBuf = request.content();
        String jsonStr = jsonBuf.toString(CharsetUtil.UTF_8);
        return jsonStr;
    }




    public String getRequestID(){
        if(req_id.equals("") || req_id.length() == 15) {
            if (request != null) {
                Jlog.debug("=========:"+req_id);
                for (Map.Entry<String, String> entry : request.headers()) {
                    if (entry.getKey().equals("X-Request-ID")) {
                        req_id = entry.getValue();
                    }
                }
            }
            Jlog.debug("=========:"+req_id);
            if(req_id.equals("")) {
                req_id =  UUID.randomUUID().toString().replace("-", "").substring(0, 15);
            }
        }
        return req_id;
    }
}
