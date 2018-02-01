package com.fivebit.utils;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by fivebit on 2017/5/11.
 * common tools about string/date/others
 */
public class Utils {
    private static Logger log = LoggerFactory.getLogger(Utils.class);

    /**
     * 检测token的格式，要符合xxxxxxxx-xxxx-xxxx-xxxxxx-xxxxxxxxxx
     * @param token
     * @return true/false
     */
    public static Boolean checkTokenFormat(String token){
        Boolean status = true;
        if(token == null || token.isEmpty() == true || token.length() !=36 ){
            status = false;
        }else {
            status = token.matches("[a-zA-Z0-9]{8}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{12}");
        }
        log.info("check token format:"+token+" and ret:"+status);
        return status;
    }

    /**
     * 规范过期时间，在0-1800之间
     * @param expire_ts
     * @return
     */
    public static int formatExpiredTime(int expire_ts){
        int default_et= 1800;
        if(expire_ts > 0 && expire_ts < 1800){
            return expire_ts;
        }
        return default_et;
    }

    public static Boolean checkAuthBearerHeader(String authHeader){
        return true;
    }
    public static Boolean checkAuthBasicaHeader(String authHeader){
        return true;
    }

    public static String encodePassword(String password){
        //return  Jencode.MD5(password);
        //return  new BCryptPasswordEncoder().encode(password);

        return null;
    }
    public static String getNewToken(){
        return UUID.randomUUID().toString();
    }

    /**
     * 检测password是否符合要求，eg. length,
     * @param password
     * @return
     */
    public static Boolean checkPasswordFormat(String password){
        return true;
    }

    public static String getRespons(int status,String code,Object data){
        JSONObject oper = new JSONObject();
        oper.put("status",status);
        oper.put("code",code);
        oper.put("data",data);
        return oper.toJSONString();
    }
    public static String getRespons(String code,Object data){
        return Utils.getRespons(200,code,data);

    }
    public static String getRespons(Object data){
        return Utils.getRespons("0",data);

    }
    public static String getRespons(){
        return Utils.getRespons("0","");

    }
    public static Boolean checkString(String st){
        if( null == st || st.isEmpty() == true ){
            return false;
        }
        return true;
    }
    public static String replaceString(String src,String rep,int start,int length){
        if(src.length()<start+length){
            log.error("replace index error:src:"+src+" start:"+start+" length:"+length);
            return src;
        }
        char[] src_char = src.toCharArray();
        char[] rep_char = rep.toCharArray();
        int ret_char_len = src.length()-length+rep.length();    //结果长度
        char[] ret_char = new char[ret_char_len];               //结果数组
        int i = 0;
        for(i=0;i<start;i++){
            ret_char[i] = src_char[i];
        }
        for(i=0;i<rep.length();i++){
            ret_char[i+start] = rep_char[i];
        }
        for(i=0;i<src.length()-length-start;i++){
            ret_char[i+start+rep.length()] = src_char[i+start+length];
        }
        return String.valueOf(ret_char);
    }

    /**
     * 合并两个map
     * @param src
     * @param dest
     * @return
     */
    public static Map<String,String> mergMap(Map<String,String> src,List<Map<String,String>> dest){
        if(src == null ){
            src = new HashMap<String,String>();
        }
        if(dest == null){
            return src;
        }
        for(Map<String,String> item:dest){
            for(Map.Entry<String,String> _set:item.entrySet()){
                src.put(_set.getKey(),_set.getValue());
            }
        }
        return src;

    }

    /***
     * 合并两个map.
     * @param src
     * @param dest
     * @return
     */
    public static Map<String,String> mergMap(Map<String,String> src,Map<String,String> dest){
        if(src == null ){
            src = new HashMap<String,String>();
        }
        if(dest == null){
            return src;
        }
        for(Map.Entry<String,String> _set:dest.entrySet()){
            src.put(_set.getKey(),_set.getValue());
        }
        return src;

    }

    /**
     * 从map转成对应的json object
     * @param srcmap
     * @return
     */
    public static JSONObject map2Json(Map<String,String> srcmap){
        JSONObject json_map = new JSONObject();
        if(srcmap != null) {
            for (Map.Entry<String, String> entry : srcmap.entrySet()) {
                json_map.put(entry.getKey(), entry.getValue());
            }
        }
        return json_map;
    }

    public static Map<String,String> json2map(JSONObject srcjson){
        Map<String,String> map_json = Maps.newHashMap();
        if(srcjson != null){
            for(Map.Entry<String, Object> entry: srcjson.entrySet()) {
                map_json.put(entry.getKey(), entry.getValue().toString());
            }
        }
        return map_json;
    }

//    /**
//     * 从文本中，获取其中一个URL并返回
//     * @param message
//     * @return
//     */
//    public static String getUrlFromMessage(String message){
//        Pattern pb = Pattern.compile("(?<!\\d)(?:(?:[\\w[.-://]]*\\.[com|cn|net|tv|gov|org|biz|cc|uk|jp|edu]+[^\\s|^\\u4e00-\\u9fa5]*))");
//        Matcher mb = pb.matcher(message);
//        String url = "";
//        if(mb.find()) {
//            url = mb.group();
//        }
//        return url;
//    }
//
    /**
     * 获取所有的url并返回，Map<Order, Link>
     * @param message
     * @param type email/sms
     * @return
     */
    public static HashMap<Integer, String> getUrlsFromMessage(String message,String type){
        Pattern patternHttp = null;
        Jlog.debug("getUrlsFromMessage message:" + message+" type:"+type);
        if(type == null || type.equals("email")) {
            patternHttp = Pattern.compile("<a.*?href=[\"']?((https?://)?/?[^\"']+)[\"']?.*?>(.+)</a>");
            patternHttp = Pattern.compile("(?<!\\d)(?:(?:[\\w[.-://]]*\\.[com|cn|net|tv|gov|org|biz|cc|uk|jp|edu]+[^\\s|^\\u4e00-\\u9fa5]*))");
        }else{
            patternHttp = Pattern.compile("(?<!\\d)(?:(?:[\\w[.-://]]*\\.[com|cn|net|tv|gov|org|biz|cc|uk|jp|edu]+[^\\s|^\\u4e00-\\u9fa5]*))");
        }
        Matcher matcher = patternHttp.matcher(message);
        HashMap<Integer, String> linkOrderMap = new HashMap<Integer, String>();
        int i = 0;
        while(matcher.find()) {
            String link = "";
            if (type == null || type.equals("email")) {
                //link = matcher.group(1).trim();
                link = matcher.group().trim();
            } else {
                link = matcher.group().trim();
            }
            if (link.matches("topzedu.com") == true){
                continue;
            }
            linkOrderMap.put(i, link);
            i++;
        }

        return linkOrderMap;
    }



    /**
     * 替换链接
     * @param message
     * @param old_url
     * @param new_url
     * @return
     */
    public static String replaceUrlFromMessage(String message,String old_url,String new_url){
        Pattern pb = Pattern.compile(old_url);
        String re_message = message;
        Matcher mb = pb.matcher(message);
        if (mb.find()) {
            Jlog.debug("replace url:" + mb.group()+" new url:"+new_url);
            re_message = mb.replaceAll(new_url);
        }
        return re_message;
    }

    public static String getUUID(){
        return UUID.randomUUID().toString().replace("-", "");
    }


    public static int getRandom(int max){
        int min=0;
        Random random = new Random();
        return random.nextInt(max)%(max-min+1) + min;
    }

}
