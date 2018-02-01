package com.fivebit.service;

import com.alibaba.fastjson.JSONObject;
import com.fivebit.errorhander.AppException;
import com.fivebit.utils.JhttpClient;
import com.fivebit.utils.Slog;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by fivebit on 2017/7/4.
 * api gate way 总入口
 * 路由服务
 * 包括负载均衡和cache服务等。
 */
@Component("routerServer")
public class RouterServer {

    @Autowired
    private  AuthServer authServer;

    @Autowired
    private  FilterServer filterServer;

    @Autowired
    private  CacheServer cacheServer;

    @Autowired
    private LbServer lbServer;

    @Autowired
    private Slog slog;


    public String route(HttpRequest request,HttpMethod method, String uri, Map<String,String> header_params,Map<String,Object> body_params) throws AppException {
        slog.info("route begin:"+method.name()+" uri:"+uri+" body_prams:"+body_params+ " header params:"+header_params);
        filterServer.doFilter(request);

        if(authServer.checkAuth(uri,header_params) == false){
            slog.info("user check oauth failed:"+header_params);
            throw  new AppException("0","NO AUTH");
        }

        String resource_url = getResourceUrl(uri);
        JSONObject ret = new JSONObject();
        if(method.equals(HttpMethod.GET)){
            ret = JhttpClient.httpGet(resource_url,header_params);
        }
        if(method.equals(HttpMethod.DELETE)){
            ret = JhttpClient.httpDelete(resource_url,header_params);
        }
        if(method.equals(HttpMethod.PUT)){
            ret = JhttpClient.httpPut(resource_url,header_params,(JSONObject)JSONObject.toJSON(body_params));
        }
        if(method.equals(HttpMethod.POST)){
            ret = JhttpClient.httpPost(resource_url,header_params,(JSONObject)JSONObject.toJSON(body_params));
        }
        slog.info("route end.get data from:"+resource_url+" method:"+method+" ret:"+ret );
        if(ret == null){
            throw new AppException("0","uri is wrong or server failed");
        }
        return ret.toJSONString();
    }
    public String getResourceUrl(String uri) throws AppException {
        String server_url = lbServer.getLbServer(uri);
        slog.info("get resource url:"+server_url+" uri:"+uri);
        if(server_url.isEmpty()){
            slog.error("get resource url is empty:"+uri);
            throw new AppException("0","uri is wrong");
        }
        return server_url;
    }
}
