package com.liuhu.socket.common;

import com.liuhu.socket.service.impl.SharesInfoServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
@Slf4j
public class HttpClientUtils {
    private static final Logger logger = LogManager.getLogger(SharesInfoServiceImpl.class);

    public static List<String> commonGetMethodByParam(String url) {
        logger.info("带参数的get请求url{}",url);
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet(url);
        // 响应模型
        CloseableHttpResponse response = null;
        List<String> dataList = new ArrayList<String>();
        BufferedReader br = null;
        try {
            // 配置信息
            RequestConfig requestConfig = RequestConfig.custom()
                    // 设置连接超时时间(单位毫秒)
                    .setConnectTimeout(5000)
                    // 设置请求超时时间(单位毫秒)
                    .setConnectionRequestTimeout(5000)
                    // socket读写超时时间(单位毫秒)
                    .setSocketTimeout(5000)
                    // 设置是否允许重定向(默认为true)
                    .setRedirectsEnabled(true).build();

            // 将上面的配置信息 运用到这个Get请求里
            httpGet.setConfig(requestConfig);

            // 由客户端执行(发送)Get请求
            response = httpClient.execute(httpGet);

            // 从响应模型中获取响应实体
            HttpEntity responseEntity = response.getEntity();
            System.out.println("响应状态为:" + response.getStatusLine());
            InputStream input = responseEntity.getContent();
            InputStreamReader fReader = new InputStreamReader(input, "gbk");
            br = new BufferedReader(fReader);
            String line = "";
            while ((line = br.readLine()) != null) {
                dataList.add(line);
            }
            return dataList;
        } catch (Exception e) {
          logger.info("get请求失败，e{}",e);
        } finally {
            try {
                // 释放资源
                if (httpClient != null) {
                    httpClient.close();
                }
                if (response != null) {
                    response.close();
                }
                if(br!=null){
                    br.close();
                    br = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
     return null;
    }

    /**
     * 发起http请求并获取结果
     * @param requestUrl 请求地址
     */
    public static String getXpath(String requestUrl){
        logger.info(requestUrl);
        String res="";
        StringBuffer buffer = new StringBuffer();
        System.setProperty("http.proxyHost", "127.0.0.1");
        System.setProperty("http.proxyPort", "33210");
        try{
            URL url = new URL(requestUrl);
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new java.net.InetSocketAddress("127.0.0.1", 33210));
            HttpURLConnection urlCon= (HttpURLConnection)url.openConnection(proxy);
            urlCon.setRequestProperty("cookie", "xq_a_token=29bdb37dee2432c294425cc9e8f45710a62643a5; xqat=29bdb37dee2432c294425cc9e8f45710a62643a5; xq_r_token=3a35db27fcf5471898becda7aa5dab6afeafe471; xq_id_token=eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJ1aWQiOi0xLCJpc3MiOiJ1YyIsImV4cCI6MTY5NjgxMTc5NCwiY3RtIjoxNjk0NDA4OTAwMTY1LCJjaWQiOiJkOWQwbjRBWnVwIn0.TVdhblfV0vdfvR1YfXUbUZFs4BU47KaGlcltI8R4qRCf2FmCxjznQOxFgTgpcbw6sUP1f13nb9ss9g-pg4SoRPw4MEz_qjWO2trtL7CEq7Ci3nKipakoYdz_ZNEm_eQKaTtZ_OJ2EZiwg1ttd19PfBfIslU8uctM3ycN1RTVFA1PlpOq5sGhO7gZo06DHGgWSKQlWgVZTQRod4CW1Ugny9padbgx_hL1u5YgisTwFjtVPyuvaYHlVC2HLjk7WI6lYLh3eirctmlsUxY7RFpSH6JazwchtvtSwuggWXR7SROih-4RuSi8NVjjedhWQMFlNHJcgqdNELatcTO2C1Gcug; cookiesu=141694408909948; u=141694408909948; Hm_lvt_1db88642e346389874251b5a1eded6e3=1694408912; device_id=a783017b6260994507ae643a75a2db68; s=bs1936mgs2; Hm_lpvt_1db88642e346389874251b5a1eded6e3=1694936785");
            log.info("urlCon resp code is:{}",urlCon.getResponseCode());
            if(200==urlCon.getResponseCode()){
                InputStream is = urlCon.getInputStream();
                InputStreamReader isr = new InputStreamReader(is,"utf-8");
                BufferedReader br = new BufferedReader(isr);

                String str = null;
                while((str = br.readLine())!=null){
                    buffer.append(str);
                }
                br.close();
                isr.close();
                is.close();
                res = buffer.toString();
            }
        }catch(IOException e){
            logger.error(requestUrl,e);
        }
        return res;
    }

}
