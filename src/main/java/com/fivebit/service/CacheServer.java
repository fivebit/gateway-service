package com.fivebit.service;

import com.alibaba.fastjson.JSONObject;
import com.fivebit.utils.Slog;
import com.fivebit.utils.Sredis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by fivebit on 2017/7/4.
 * 缓存
 */
@Component("cacheServer")
public class CacheServer {
    @Autowired
    Sredis sredis;
    @Autowired
    Slog slog;

    private Integer short_expired = 10;     //短期的访问失效时间
    private Integer long_expired = 8600;    //一天的有效期

    public Boolean setUserInfo(String token,JSONObject user_info){
        sredis.addString("user_info:"+token,user_info.toJSONString(),short_expired);
        return true;
    }
    public JSONObject getUserInfo(String token){
        String user_info = sredis.getString("user_info:"+token);
        JSONObject ret = null;
        if(user_info != null){
            ret = JSONObject.parseObject(user_info);
        }
        return ret;
    }
    public Boolean setNormalCache(String key,Object value){
        try {
            sredis.addString("normal_info:" + key, value.toString(), short_expired);
        }catch (Exception ee){
            slog.error("add normal data to redis error:"+ee.getMessage());
        }
        return true;
    }
    public JSONObject getNormalCache(String key){
        String cache_data = sredis.getString("normal_info:"+key);
        JSONObject ret = null;
        if(cache_data != null){
            try {
                ret = JSONObject.parseObject(cache_data);
            }catch (Exception ee){
                slog.error("paser json from redis error:"+cache_data);
            }
        }
        return ret;
    }
}
