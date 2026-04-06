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
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

@Slf4j
public class HttpClientUtils {
    private static final Logger logger = LogManager.getLogger(SharesInfoServiceImpl.class);
    private static final Random random = new Random();

    // 浏览器 User-Agent 列表
    private static final String[] USER_AGENTS = {
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:121.0) Gecko/20100101 Firefox/121.0",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36"
    };

    // 全局共享的 HttpClient 连接池（优化：增加连接数支持高并发）
    private static final CloseableHttpClient sharedHttpClient = HttpClientBuilder.create()
            .setMaxConnTotal(300)  // 总连接数从 200 增加到 300
            .setMaxConnPerRoute(80)  // 每路由连接从 50 增加到 80
            .setConnectionTimeToLive(10, TimeUnit.MINUTES)  // 连接最大生命周期 10 分钟
            .evictIdleConnections(60, TimeUnit.SECONDS)  // 60 秒空闲连接回收
            .build();

    /**
     * HTTP 请求结果
     */
    public static class HttpResponseResult {
        private int statusCode;
        private String content;
        private boolean is503;

        public HttpResponseResult(int statusCode, String content, boolean is503) {
            this.statusCode = statusCode;
            this.content = content;
            this.is503 = is503;
        }

        public int getStatusCode() { return statusCode; }
        public String getContent() { return content; }
        public boolean is503() { return is503; }
        public boolean isSuccess() { return statusCode == 200 && content != null && !content.isEmpty(); }
    }

    public static List<String> commonGetMethodByParam(String url) {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse response = null;
        List<String> dataList = new ArrayList<String>();
        BufferedReader br = null;
        try {
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(5000)
                    .setConnectionRequestTimeout(5000)
                    .setSocketTimeout(5000)
                    .setRedirectsEnabled(true).build();

            httpGet.setConfig(requestConfig);
            response = httpClient.execute(httpGet);

            HttpEntity responseEntity = response.getEntity();
            InputStream input = responseEntity.getContent();
            InputStreamReader fReader = new InputStreamReader(input, "gbk");
            br = new BufferedReader(fReader);
            String line = "";
            while ((line = br.readLine()) != null) {
                dataList.add(line);
            }
            return dataList;
        } catch (Exception e) {
          logger.error("get 请求失败，url: {}, e: {}", url, e.getMessage());
        } finally {
            try {
                if (httpClient != null) httpClient.close();
                if (response != null) response.close();
                if(br!=null) { br.close(); br = null; }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
     return null;
    }

    /**
     * 发起 http 请求并获取结果（支持搜狐反爬虫）
     * @param requestUrl 请求地址
     * @return 响应内容
     */
    public static String getXpath(String requestUrl){
        return getXpathWithRetry(requestUrl, 3, 1000);
    }

    /**
     * 带重试的 HTTP 请求（使用 Apache HttpClient）
     * @param requestUrl 请求地址
     * @param maxRetries 最大重试次数
     * @param retryDelay 重试延迟（毫秒）
     * @return 响应内容
     */
    public static String getXpathWithRetry(String requestUrl, int maxRetries, long retryDelay) {
        HttpResponseResult result = getXpathByHttpClientWithRetry(requestUrl, maxRetries, retryDelay);
        return result != null ? result.getContent() : "";
    }

    /**
     * 带重试的 HTTP 请求（返回详细状态）- 简化版，重试交给上层处理
     * @param requestUrl 请求地址
     * @param maxRetries 最大重试次数
     * @param retryDelay 重试延迟（毫秒）
     * @return 响应结果
     */
    public static HttpResponseResult getXpathByHttpClientWithRetry(String requestUrl, int maxRetries, long retryDelay) {
        int retryCount = 0;
        int lastStatusCode = 0;

        while (retryCount < maxRetries) {
            try {
                // 重试前延迟
                if (retryCount > 0) {
                    long delay = retryDelay + ThreadLocalRandom.current().nextInt(500);
                    log.debug("第 {} 次重试，延迟 {} ms", retryCount, delay);
                    Thread.sleep(delay);
                }

                HttpResponseResult result = getXpathByHttpClient(requestUrl);
                lastStatusCode = result.getStatusCode();

                // 成功则直接返回
                if (result.isSuccess()) {
                    if (retryCount > 0) {
                        log.info("请求成功，URL: {}, 重试次数：{}", requestUrl, retryCount);
                    }
                    return result;
                }

                // 503 错误需要重试
                if (result.is503()) {
                    log.warn("请求返回 503，URL: {}, 第 {} 次重试", requestUrl, retryCount + 1);
                    retryCount++;
                    continue;
                }

                // 其他错误也重试
                retryCount++;
                if (retryCount < maxRetries) {
                    log.warn("请求返回非 200 状态码：{}, URL: {}, 第 {} 次重试", lastStatusCode, requestUrl, retryCount + 1);
                }

            } catch (Exception e) {
                retryCount++;
                log.warn("请求异常 (第{}次/{}): {}, URL: {}", retryCount, maxRetries, e.getMessage(), requestUrl);
            }
        }

        log.error("请求失败，达到最大重试次数 {}, 最后状态码：{}, URL: {}", maxRetries, lastStatusCode, requestUrl);
        return new HttpResponseResult(lastStatusCode, "", lastStatusCode == 503);
    }

    /**
     * 使用 Apache HttpClient 发起请求（推荐，使用共享连接池）
     * @param requestUrl 请求地址
     * @return 响应结果
     */
    public static HttpResponseResult getXpathByHttpClient(String requestUrl) {
        return getXpathByHttpClientInternal(requestUrl, sharedHttpClient);
    }

    /**
     * 使用新的 HttpClient 发起请求（用于重试时换一个新的连接）
     * @param requestUrl 请求地址
     * @return 响应结果
     */
    public static HttpResponseResult getXpathByHttpClientNewConnection(String requestUrl) {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        try {
            return getXpathByHttpClientInternal(requestUrl, httpClient);
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    /**
     * 内部请求方法（优化版 - 更短的超时时间）
     */
    private static HttpResponseResult getXpathByHttpClientInternal(String requestUrl, CloseableHttpClient httpClient) {
        // 清除代理设置
        System.clearProperty("http.proxyHost");
        System.clearProperty("http.proxyPort");
        System.clearProperty("https.proxyHost");
        System.clearProperty("https.proxyPort");

        CloseableHttpResponse response = null;
        BufferedReader br = null;

        try {
            // 创建 HttpGet 请求
            HttpGet httpGet = new HttpGet(requestUrl);

            // 优化：减少超时时间，让失败请求更快返回
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(10000)        // 连接超时：10 秒（从 30 秒减少）
                    .setConnectionRequestTimeout(5000)  // 从连接池获取连接超时：5 秒
                    .setSocketTimeout(15000)         // 读取超时：15 秒（从 30 秒减少）
                    .setRedirectsEnabled(true)
                    .build();

            httpGet.setConfig(requestConfig);

            // 设置浏览器请求头
            setBrowserHeaders(httpGet, requestUrl);

            // 执行请求
            response = httpClient.execute(httpGet);

            // 检查响应码
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode == 200) {
                HttpEntity responseEntity = response.getEntity();
                InputStream input = responseEntity.getContent();

                // 处理 GZIP 压缩
                String contentEncoding = response.getFirstHeader("Content-Encoding") != null
                    ? response.getFirstHeader("Content-Encoding").getValue() : null;
                if ("gzip".equalsIgnoreCase(contentEncoding)) {
                    input = new GZIPInputStream(input);
                }

                // 读取响应内容
                InputStreamReader fReader = new InputStreamReader(input, "utf-8");
                br = new BufferedReader(fReader);

                StringBuilder buffer = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    buffer.append(line);
                }

                return new HttpResponseResult(200, buffer.toString(), false);
            } else if (statusCode == 503) {
                // 503 单独标记
                return new HttpResponseResult(503, "", true);
            } else {
                logger.debug("请求失败，响应码：{}, URL: {}", statusCode, requestUrl);
                return new HttpResponseResult(statusCode, "", false);
            }

        } catch (Exception e) {
            logger.debug("请求异常，URL: " + requestUrl + ", e: " + e.getMessage());
            return new HttpResponseResult(0, "", false);
        } finally {
            try {
                if (br != null) br.close();
                if (response != null) response.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    /**
     * 使用 HttpURLConnection 发起请求（备用方案）
     * @param requestUrl 请求地址
     * @return 响应内容
     */
    public static String getXpathByHttpURLConnection(String requestUrl) {
        // 清除代理设置
        System.clearProperty("http.proxyHost");
        System.clearProperty("http.proxyPort");
        System.clearProperty("https.proxyHost");
        System.clearProperty("https.proxyPort");

        HttpURLConnection urlCon = null;
        try {
            URL url = new URL(requestUrl);
            urlCon = (HttpURLConnection)url.openConnection();

            // 优化：减少超时时间
            urlCon.setConnectTimeout(10000);    // 10 秒
            urlCon.setReadTimeout(15000);       // 15 秒

            // 设置请求方法
            urlCon.setRequestMethod("GET");

            // 设置浏览器请求头
            setBrowserHeaders(urlCon, requestUrl);

            int responseCode = urlCon.getResponseCode();

            if (200 == responseCode) {
                InputStream is = urlCon.getInputStream();

                // 处理 gzip 压缩
                String contentEncoding = urlCon.getContentEncoding();
                if ("gzip".equalsIgnoreCase(contentEncoding)) {
                    is = new GZIPInputStream(is);
                }

                InputStreamReader isr = new InputStreamReader(is, "utf-8");
                BufferedReader br = new BufferedReader(isr);

                StringBuffer buffer = new StringBuffer();
                String str;
                while ((str = br.readLine()) != null) {
                    buffer.append(str).append("\n");
                }

                br.close();
                isr.close();
                is.close();
                return buffer.toString();
            } else {
                logger.error("请求失败，响应码：{}, URL: {}", responseCode, requestUrl);
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

    /**
     * 设置浏览器请求头（HttpURLConnection）
     */
    private static void setBrowserHeaders(HttpURLConnection connection, String requestUrl) {
        String userAgent = USER_AGENTS[random.nextInt(USER_AGENTS.length)];
        connection.setRequestProperty("User-Agent", userAgent);
        connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8");
        connection.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
        connection.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
        connection.setRequestProperty("Connection", "keep-alive");
        connection.setRequestProperty("Upgrade-Insecure-Requests", "1");
        connection.setRequestProperty("Sec-Ch-Ua", "\"Not_A Brand\";v=\"8\", \"Chromium\";v=\"120\"");
        connection.setRequestProperty("Sec-Ch-Ua-Mobile", "?0");
        connection.setRequestProperty("Sec-Ch-Ua-Platform", "\"Windows\"");
        connection.setRequestProperty("Sec-Fetch-Dest", "document");
        connection.setRequestProperty("Sec-Fetch-Mode", "navigate");
        connection.setRequestProperty("Sec-Fetch-Site", "none");
        connection.setRequestProperty("Sec-Fetch-User", "?1");
        if (requestUrl.contains("sohu.com")) {
            connection.setRequestProperty("Referer", "https://q.stock.sohu.com/");
        }
    }

    /**
     * 设置浏览器请求头（HttpGet）
     */
    private static void setBrowserHeaders(HttpGet httpGet, String requestUrl) {
        String userAgent = USER_AGENTS[random.nextInt(USER_AGENTS.length)];
        httpGet.setHeader("User-Agent", userAgent);
        httpGet.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8");
        httpGet.setHeader("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
        httpGet.setHeader("Accept-Encoding", "gzip, deflate, br");
        httpGet.setHeader("Connection", "keep-alive");
        httpGet.setHeader("Upgrade-Insecure-Requests", "1");
        httpGet.setHeader("Sec-Ch-Ua", "\"Not_A Brand\";v=\"8\", \"Chromium\";v=\"120\"");
        httpGet.setHeader("Sec-Ch-Ua-Mobile", "?0");
        httpGet.setHeader("Sec-Ch-Ua-Platform", "\"Windows\"");
        httpGet.setHeader("Sec-Fetch-Dest", "document");
        httpGet.setHeader("Sec-Fetch-Mode", "navigate");
        httpGet.setHeader("Sec-Fetch-Site", "none");
        httpGet.setHeader("Sec-Fetch-User", "?1");
        if (requestUrl.contains("sohu.com")) {
            httpGet.setHeader("Referer", "https://q.stock.sohu.com/");
        }
    }
}
