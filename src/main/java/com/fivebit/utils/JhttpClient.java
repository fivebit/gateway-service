package com.fivebit.utils;

import com.alibaba.fastjson.JSONObject;
import com.fivebit.errorhander.AppException;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by fivebit on 2017/5/19.
 */
public class JhttpClient {
    public static HttpResponse httpExecute(HttpUriRequest request,Map<String,String> header_params) throws IOException {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        if(header_params != null){
            for(Map.Entry<String,String> item:header_params.entrySet()){
                if(item.getKey().equals("Content-Type")){
                    continue;
                }
                request.setHeader(item.getKey(),item.getValue());
            }
        }
        HttpResponse response = httpClient.execute(request);
        return response;
    }

    public static JSONObject httpPost(String url,Map<String,String> header_params,JSONObject jsonParam)throws AppException {
        return JhttpClient.httpPost(url, header_params,jsonParam, false);
    }
    public static JSONObject httpPost(String url,Map<String,String> header_params,JSONObject params,Boolean noNeedResponse) throws AppException {
        JSONObject jsonResult = null;
        HttpPost request = new HttpPost(url);
        try {
            if ( null != params ) {
                StringEntity entity = new StringEntity(params.toString(), "utf-8");
                entity.setContentEncoding("UTF-8");
                entity.setContentType("application/json");
                request.setEntity(entity);
            }
            HttpResponse response = JhttpClient.httpExecute(request,header_params);
            Jlog.info("request:"+url+" and return:"+response.getStatusLine());
            if (response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201) {
                String str = "";
                try {
                    str = EntityUtils.toString(response.getEntity(),"UTF-8");
                    Jlog.info("request return entity :"+str);
                    if (noNeedResponse) {
                        return null;
                    }
                    jsonResult = JSONObject.parseObject(str);
                } catch (Exception e) {
                    Jlog.error("post request error:" + e.getMessage());
                }
            }else{
                throw new AppException(String.valueOf(response.getStatusLine().getStatusCode()),"server error");
            }
        } catch (IOException e) {
            Jlog.error("post request error:" + e.getMessage());
        }
        Jlog.debug("http post return:"+jsonResult.toJSONString());
        return jsonResult;
    }
    public static JSONObject httpPostForm(String url,Map<String,String> params){
        DefaultHttpClient httpClient = new DefaultHttpClient();
        JSONObject jsonResult = null;
        HttpPost method = new HttpPost(url);
        try {
            if ( null != params ) {
                List<NameValuePair> params_body = new ArrayList<NameValuePair>(3);
                for(Map.Entry<String,String> entity:params.entrySet()) {
                    params_body.add(new BasicNameValuePair(entity.getKey(), entity.getValue()));
                }
                method.setEntity(new UrlEncodedFormEntity(params_body,"UTF-8"));
            }
            HttpResponse result = httpClient.execute(method);
            Jlog.info("request:"+url+" and return:"+result.getStatusLine());
            url = URLDecoder.decode(url, "UTF-8");
            if (result.getStatusLine().getStatusCode() == 200) {
                String str = "";
                try {
                    str = EntityUtils.toString(result.getEntity(),"UTF-8");
                    Jlog.info("request return entity :"+str);
                    jsonResult = JSONObject.parseObject(str);
                } catch (Exception e) {
                    Jlog.error("post request error:" + e.getMessage());
                }
            }
        } catch (IOException e) {
            Jlog.error("post request error:" + e.getMessage());
        }
        Jlog.debug("http post return:"+jsonResult.toJSONString());
        return jsonResult;
    }
    //GET method restful
    public static JSONObject httpGet(String url,Map<String,String> header_params) throws AppException {
        JSONObject jsonResult = null;
        try {
            HttpGet request = new HttpGet(url);
            if(header_params != null){
                for(Map.Entry<String,String> item:header_params.entrySet()){
                    request.setHeader(item.getKey(),item.getValue());
                }
            }
            HttpResponse response = JhttpClient.httpExecute(request,header_params);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String strResult = EntityUtils.toString(response.getEntity());
                jsonResult = JSONObject.parseObject(strResult);
            } else {
                Jlog.error("request error method: GET url:"+url );
                throw new AppException(String.valueOf(response.getStatusLine().getStatusCode()),"server error");
            }
        } catch (IOException e) {
            Jlog.error("post request error:" + e.getMessage());
        }
        return jsonResult;
    }
    //DELETE method restful
    public static JSONObject httpDelete(String url,Map<String,String> header_params) throws AppException {
        JSONObject jsonResult = null;
        try {
            HttpDelete request = new HttpDelete(url);
            HttpResponse response = JhttpClient.httpExecute(request,header_params);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String strResult = EntityUtils.toString(response.getEntity());
                jsonResult = JSONObject.parseObject(strResult);
            } else {
                Jlog.error("request error method: DELETE url:"+url );
                throw new AppException(String.valueOf(response.getStatusLine().getStatusCode()),"server error");
            }
        } catch (IOException e) {
            Jlog.error("post request error:" + e.getMessage());
        }
        return jsonResult;
    }
    //PUT method restful
    public static JSONObject httpPut(String url,Map<String,String> header_params,JSONObject params) throws AppException {
        Jlog.debug("put:"+url+" params:"+params.toJSONString());
        DefaultHttpClient httpClient = new DefaultHttpClient();
        JSONObject jsonResult = null;
        HttpPut method = new HttpPut(url);
        try {
            if ( null != params ) {
                StringEntity entity = new StringEntity(params.toString(), "utf-8");
                entity.setContentEncoding("UTF-8");
                entity.setContentType("application/json");
                method.setEntity(entity);
            }
            HttpResponse response = httpClient.execute(method);
            Jlog.info("request:"+url+" and return:"+response.getStatusLine());
            url = URLDecoder.decode(url, "UTF-8");
            if (response.getStatusLine().getStatusCode() == 200) {
                String str = "";
                try {
                    str = EntityUtils.toString(response.getEntity(),"UTF-8");
                    Jlog.debug("request return entity :"+str);
                    jsonResult = JSONObject.parseObject(str);
                } catch (Exception e) {
                    Jlog.error("put request error:" + e.getMessage());
                }
            }else{
                Jlog.error("request error method:PUT url:"+url );
                throw new AppException(String.valueOf(response.getStatusLine().getStatusCode()),"server error");
            }
        } catch (IOException e) {
            Jlog.error("put request error:" + e.getMessage());
        }
        Jlog.debug("http put return:"+jsonResult.toJSONString());
        return jsonResult;
    }
}
