package com.fivebit.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fivebit.com.Constants;
import com.fivebit.errorhander.AppException;
import com.fivebit.utils.Slog;
import com.fivebit.utils.Sredis;
import com.fivebit.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by fivebit on 2017/7/5.
 * 负载均衡
 */
@Component("lbServer")
public class LbServer {
    @Autowired
    private Sredis sredis;

    @Autowired
    private Slog slog;

    /**
     * 通过uri，根据权重或者健康状态，获取该uri对应的后端的服务host和port。
     * 这里只是简单的通过随机选择负载。
     * @param uri
     * @return
     */
    public String getLbServer(String uri) throws AppException {
        String[] patchs = uri.split("/");
        if(patchs.length < 2){
            throw new AppException("0","uri format is wrong");
        }
        String key = Constants.REDIS_LOADBALANCE_PREFIX+patchs[1];
        String server_string = sredis.getString(key);
        slog.debug("get lb server:uri:"+uri+" lb redis key:"+key+" server_string:"+server_string);
        String url = "";
        if(server_string != null){
            JSONObject server_info = null;
            int index = 0;
            JSONArray server_list = null;
            try {
                server_list = JSONArray.parseArray(server_string);
                slog.debug("server_list:" + server_list.toJSONString());
                index = Utils.getRandom(server_list.size() - 1);
                server_info = server_list.getJSONObject(index);
            }catch (Exception ee){
                slog.error("get string from redis:"+server_string+" paser error:"+uri);
                throw new AppException("0","uri server config is wrong");
            }
            int while_count = 0;
            while(server_info.containsKey("weight") && server_info.getInteger("weight") == 0){
                index = (index+1)%server_list.size();
                server_info = server_list.getJSONObject(index);
                while_count++;
                if(while_count > server_list.size()){
                    throw new AppException("0","uri server config is wrong,all weight is 0");
                }
            }
            url = server_info.getString("host") + ":" + server_info.getInteger("port") + uri;
        }else{
            slog.error("get config from redis empty:"+uri);
            throw new AppException("0","uri server off line");
        }
        slog.info("get lb server: uri:"+uri+" and lb url:"+url);
        return url;
    }
}
