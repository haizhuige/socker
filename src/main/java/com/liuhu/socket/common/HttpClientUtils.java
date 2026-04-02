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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

@Slf4j
public class HttpClientUtils {
    private static final Logger logger = LogManager.getLogger(SharesInfoServiceImpl.class);

    public static List<String> commonGetMethodByParam(String url) {
        logger.info("带参数的 get 请求 url{}",url);
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet(url);
        // 响应模型
        CloseableHttpResponse response = null;
        List<String> dataList = new ArrayList<String>();
        BufferedReader br = null;
        try {
            // 配置信息
            RequestConfig requestConfig = RequestConfig.custom()
                    // 设置连接超时时间 (单位毫秒)
                    .setConnectTimeout(5000)
                    // 设置请求超时时间 (单位毫秒)
                    .setConnectionRequestTimeout(5000)
                    // socket 读写超时时间 (单位毫秒)
                    .setSocketTimeout(5000)
                    // 设置是否允许重定向 (默认为 true)
                    .setRedirectsEnabled(true).build();

            // 将上面的配置信息 运用到这个 Get 请求里
            httpGet.setConfig(requestConfig);

            // 由客户端执行 (发送)Get 请求
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
          logger.info("get 请求失败，e{}",e);
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
     * 发起 http 请求并获取结果（支持搜狐反爬虫）
     * @param requestUrl 请求地址
     */
    public static String getXpath(String requestUrl){
        logger.info("请求 URL: {}", requestUrl);
        StringBuffer buffer = new StringBuffer();
        // 清除代理设置，使用直连方式
        System.clearProperty("http.proxyHost");
        System.clearProperty("http.proxyPort");
        System.clearProperty("https.proxyHost");
        System.clearProperty("https.proxyPort");
        HttpURLConnection urlCon = null;
        try{
            URL url = new URL(requestUrl);
            // 不使用代理，直接连接
            urlCon = (HttpURLConnection)url.openConnection();
            // 设置超时时间
            urlCon.setConnectTimeout(15000);
            urlCon.setReadTimeout(15000);
            // 设置请求方法
            urlCon.setRequestMethod("GET");
            // 设置请求头，模拟浏览器
            urlCon.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            urlCon.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8");
            urlCon.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
            urlCon.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
            urlCon.setRequestProperty("Connection", "keep-alive");
            urlCon.setRequestProperty("Upgrade-Insecure-Requests", "1");
            urlCon.setRequestProperty("Sec-Ch-Ua", "\"Not_A Brand\";v=\"8\", \"Chromium\";v=\"120\"");
            urlCon.setRequestProperty("Sec-Ch-Ua-Mobile", "?0");
            urlCon.setRequestProperty("Sec-Ch-Ua-Platform", "\"Windows\"");
            urlCon.setRequestProperty("Sec-Fetch-Dest", "document");
            urlCon.setRequestProperty("Sec-Fetch-Mode", "navigate");
            urlCon.setRequestProperty("Sec-Fetch-Site", "none");
            urlCon.setRequestProperty("Sec-Fetch-User", "?1");
            // 设置 Referer，让服务器认为是从搜狐页面跳转的
            if (requestUrl.contains("sohu.com")) {
                urlCon.setRequestProperty("Referer", "https://q.stock.sohu.com/");
            }

            int responseCode = urlCon.getResponseCode();
            log.info("URL 响应码：{}, URL: {}", responseCode, requestUrl);

            if (200 == responseCode) {
                InputStream is = urlCon.getInputStream();
                // 处理 gzip 压缩
                String contentEncoding = urlCon.getContentEncoding();
                if ("gzip".equalsIgnoreCase(contentEncoding)) {
                    is = new GZIPInputStream(is);
                }
                InputStreamReader isr = new InputStreamReader(is, "utf-8");
                BufferedReader br = new BufferedReader(isr);

                String str = null;
                while ((str = br.readLine()) != null) {
                    buffer.append(str).append("\n");
                }
                br.close();
                isr.close();
                is.close();
                return buffer.toString();
            } else {
                logger.error("请求失败，响应码：{}, URL: {}", responseCode, requestUrl);
                // 读取错误响应体
                try {
                    InputStream errorIs = urlCon.getErrorStream();
                    if (errorIs != null) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(errorIs, "utf-8"));
                        String line;
                        StringBuilder errorBuffer = new StringBuilder();
                        while ((line = br.readLine()) != null) {
                            errorBuffer.append(line);
                        }
                        br.close();
                        logger.error("错误响应体：{}", errorBuffer.toString());
                    }
                } catch (Exception e) {
                    logger.error("读取错误响应体失败", e);
                }
            }
        } catch (Exception e) {
            logger.error("请求异常，URL: " + requestUrl, e);
        } finally {
            if (urlCon != null) {
                urlCon.disconnect();
            }
        }
        return "";
    }

}
