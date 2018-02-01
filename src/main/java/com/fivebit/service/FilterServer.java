package com.fivebit.service;

import com.fivebit.errorhander.AppException;
import com.fivebit.utils.Slog;
import io.netty.handler.codec.http.HttpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by fivebit on 2017/7/4.
 * IP 黑名单
 * rate limit
 * 流量统计
 * 日志
 * 防攻击
 */
@Component("filterServer")
public class FilterServer {
    @Autowired
    Slog slog;
    public void doFilter(HttpRequest request) throws AppException {

        ipBlackFilter(request);

    }

    protected  void ipBlackFilter(HttpRequest request) throws AppException{
        String ip = getIpAddress(request);

        if(ip.equals("qiong")){
            slog.info("ip black filter block:"+ip);
            throw new AppException("0","ip black ");
        }
        slog.info("ip black filter OK:"+ip);
    }
    protected  String getIpAddress(HttpRequest request) {
        String ip = request.headers().get("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.headers().get("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.headers().get("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.headers().get("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.headers().get("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = "unknown";
        }
        return ip;
    }
}
