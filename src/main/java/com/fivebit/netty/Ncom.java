package com.fivebit.netty;

import com.alibaba.fastjson.JSONObject;
import com.fivebit.utils.Jlog;
import com.fivebit.utils.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import netscape.javascript.JSObject;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpHeaders.Names.*;

/**
 * Created by fivebit on 2017/7/4.
 * netty的一些基本操作和公共逻辑
 */
public class Ncom {

    //对每个请求，整理一个请求日志。
    public static String makeAccessLog(HttpRequest request){
        StringBuffer logs = new StringBuffer();
        logs.append("VERSION:");
        logs.append(request.getProtocolVersion().text());
        logs.append(" Method:");
        logs.append(request.getMethod());
        logs.append(" REQUEST_URI:");
        logs.append(request.getUri());
        logs.append(" HEADER:");
        for (Map.Entry<String, String> entry : request.headers()) {
            logs.append(entry.getKey() + '=' + entry.getValue() + " ");
        }
        Set<Cookie> cookies;
        String value = request.headers().get(COOKIE);
        if (value == null) {
            cookies = Collections.emptySet();
        } else {
            cookies = CookieDecoder.decode(value);
        }
        logs.append(" COOKIE:");
        for (Cookie cookie : cookies) {
            logs.append(cookie.toString()+" ");
        }
        return logs.toString();
    }

    /**
     * 返回json给前端
     * @param channel
     * @param request
     * @param message
     */
    public static void writeResponse(Channel channel,HttpRequest request, String message) {
        ByteBuf buf = copiedBuffer(message, CharsetUtil.UTF_8);
        // Decide whether to close the connection or not.
        /*boolean close = request.headers().contains(CONNECTION, HttpHeaders.Values.CLOSE, true)
                || request.getProtocolVersion().equals(HttpVersion.HTTP_1_0)
                && !request.headers().contains(CONNECTION, HttpHeaders.Values.KEEP_ALIVE, true);*/
        Boolean close = HttpUtil.isKeepAlive(request);
        // Build the response object.
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buf);
        response.headers().set(CONTENT_TYPE, "application/json; charset=UTF-8");
        if (!close) {
            // There's no need to add 'Content-Length' header if this is the last response.
            response.headers().set(CONTENT_LENGTH, buf.readableBytes());
            response.headers().set(CONNECTION, KEEP_ALIVE);
        }
        Set<Cookie> cookies;
        String value = request.headers().get(COOKIE);
        if (value == null) {
            cookies = Collections.emptySet();
        } else {
            cookies = CookieDecoder.decode(value);
        }
        if (!cookies.isEmpty()) {
            // Reset the cookies if necessary.
            for (Cookie cookie : cookies) {
                response.headers().add(SET_COOKIE, ServerCookieEncoder.encode(cookie));
            }
        }
        // Write the response.
        ChannelFuture future = channel.writeAndFlush(response);
        // Close the connection after the write operation is done if necessary.
        if (close) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }
    public static void writeErrorResp(Channel channel,HttpRequest request, String message){
        writeResponse(channel,request,Utils.getRespons("5000",message));
        channel.close();
    }
    public static String getContentType(HttpRequest request){
        String content_type = "";
        if(request.headers().contains("Content-Type") == true){
            String temp = request.headers().get("Content-Type");
            if(temp.matches(".*application/json.*")){
                content_type = "json";
            }else if(temp.matches(".*multipart/form-data.*")) {
                content_type = "form";
            }else if(temp.matches(".*x-www-form-urlencoded.*")){
                content_type = "urlencoded";
            }
        }
        return content_type;
    }
}
