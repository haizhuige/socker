package com.liuhu.socket.common.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.X509Certificate;


public class StockDataFetcher {
    public static void main(String[] args) {
        try {
            // 创建URL对象
            URL url = new URL("https://stock.xueqiu.com/v5/stock/realtime/quotec.json?symbol=SH516010");
            System.setProperty("127.0.0.1", "33210");
            // 打开连接
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
// 禁用SSL证书验证
            TrustManager[] trustAllCertificates = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCertificates, new java.security.SecureRandom());
          //  connection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());


            // 设置请求方法为GET
            connection.setRequestMethod("GET");

            // 设置请求头（如果需要）
             connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Safari/537.36");
            connection.setConnectTimeout(10000); // 连接超时，单位是毫秒
            connection.setReadTimeout(10000);    // 读取超时，单位是毫秒

            // 获取响应代码
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                // 读取响应数据
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // 解析JSON响应
                JSONObject jsonObject = JSON.parseObject(response.toString());

                // 现在可以从 jsonObject 中提取您需要的数据
                String stockName = jsonObject.getJSONObject("data").getJSONObject("quote").getString("name");
                double currentPrice = jsonObject.getJSONObject("data").getJSONObject("quote").getDouble("current");

                // 输出数据
                System.out.println("Stock Name: " + stockName);
                System.out.println("Current Price: " + currentPrice);
            } else {
                System.err.println("HTTP Request Failed with response code: " + responseCode);
            }

            // 关闭连接
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
