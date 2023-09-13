package com.liuhu.socket.common.config;


import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.security.cert.X509Certificate;

public class HttpsURLConnectionExample {
    public static void main(String[] args) {
    try {
        // 1. 禁用SSL证书验证（仅在信任自签名证书时使用）
        disableCertificateValidation();

        // 2. 设置代理（如果需要）
         System.setProperty("http.proxyHost", "127.0.0.1");
         System.setProperty("https.proxyPort", "33210");

        // 3. 创建URL对象
        URL url = new URL("http://stock.xueqiu.com/v5/stock/realtime/quotec.json?symbol=SH516010"); // 替换成您的HTTPS接口URL

        // 4. 打开连接
        HttpURLConnection connection;
        if (System.getProperty("http.proxyHost") != null) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new java.net.InetSocketAddress("127.0.0.1", 33210));
            connection = (HttpURLConnection) url.openConnection(proxy);
        } else {
            connection = (HttpURLConnection) url.openConnection();
        }

        // 5. 发送请求
        connection.setRequestMethod("GET");

        // 6. 获取响应
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            // 处理响应内容
            System.out.println("Response: " + content.toString());
        } else {
            System.err.println("HTTP Request Failed with response code: " + responseCode);
        }

        // 7. 关闭连接
        connection.disconnect();
    } catch (Exception e) {
        e.printStackTrace();
    }
}

    private static void disableCertificateValidation() {
        try {
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
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}