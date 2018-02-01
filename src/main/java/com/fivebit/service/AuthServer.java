package com.fivebit.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fivebit.com.Constants;
import com.fivebit.errorhander.AppException;
import com.fivebit.utils.JhttpClient;
import com.fivebit.utils.Slog;
import com.fivebit.utils.Sredis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by fivebit on 2017/7/4.
 * 验证Token是否有效
 */
@Component("authServer")
public class AuthServer {
    @Autowired
    private Slog slog;
    @Autowired
    private CacheServer cacheServer;
    @Autowired
    private Sredis sredis;

    @Autowired
    private LbServer lbServer;
    /**
     * 对uri的参数进行判断，是否需要认证token
     * @param uri
     * @param header_params
     * @return
     */
    public Boolean checkAuth(String uri,Map<String,String> header_params) throws AppException {

        Boolean auth_status = true;
        slog.info("check auth:"+uri+" params:"+header_params.toString());
        if(checkWhiteAuthList(uri) == true){
            return auth_status;
        }
        JSONObject ret = getAuthInfo(uri,header_params);
        if(ret == null || ret.getInteger("status") != 200){
            auth_status = false;
        }
        return auth_status;
    }
    public JSONObject getAuthInfo(String uri,Map<String,String> params) throws AppException {
        JSONObject user_info = null;
        if(params.containsKey("token") == false){
            return  user_info;
        }
        String token = params.get("token").toString();

        JSONObject user_info_r = cacheServer.getUserInfo(token);
        if(user_info_r != null){
            slog.debug("get user info from redis:"+user_info_r);
            return user_info_r;
        }

        String oauth_url = lbServer.getLbServer("/chain-api/token/"+token);
        try {
            user_info = JhttpClient.httpGet(oauth_url,null);
        }catch (Exception ee){

        }
        cacheServer.setUserInfo(token,user_info);
        slog.info("get auth info:"+user_info.toJSONString());
        return user_info;
    }

    /**
     * 检测uri是否需要认证
     * @param uri
     * @return true/false
     */
    public Boolean checkWhiteAuthList(String uri){

        Boolean check_status = false;
        String redis_key = Constants.REDIS_AUTH_WHITELIST_PREFIX+uri;
        String redis_ret = sredis.getString(redis_key);
        if(redis_ret != null){      //精确匹配
            check_status = true;
        }else{      //模糊匹配
            redis_key = Constants.REDIS_AUTH_WHITELIST_REG;
            redis_ret = sredis.getString(redis_key);
            if(redis_ret != null){
                JSONArray regs = JSONArray.parseArray(redis_ret);
                int n = regs.size();
                for(int i=0 ;i < n;i++){
                    String reg = regs.getString(i);
                    if(uri.matches(reg)){
                        slog.debug("match reg:"+reg+" uri:"+uri);
                        check_status = true;
                        break;
                    }
                }
            }
        }
        slog.debug("checkWhiteAuthList uri:"+uri+" rediskey:"+redis_key+" ret:"+redis_ret+" check_status:"+check_status);
        return check_status;
    }
}
