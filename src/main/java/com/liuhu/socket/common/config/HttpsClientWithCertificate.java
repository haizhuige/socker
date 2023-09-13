package com.liuhu.socket.common.config;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.SecureRandom;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

public class HttpsClientWithCertificate {
    public static void main(String[] args) {
        // 证书文件路径
        String certificateFilePath = "C:\\Users\\liuhuc\\mykeystore.jks";

        // 证书密码
        String certificatePassword = "mypassword";

        // HTTPS接口URL
        String httpsUrl = "https://stock.xueqiu.com/v5/stock/realtime/quotec.json?symbol=SH516010"; // 替换成您的HTTPS接口URL

        try {
            // 创建证书工厂，用于加载客户端证书
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            FileInputStream certificateInputStream = new FileInputStream(certificateFilePath);
            X509Certificate clientCertificate = (X509Certificate) certificateFactory.generateCertificate(certificateInputStream);

            // 创建KeyStore并将客户端证书导入
            KeyStore clientKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            clientKeyStore.load(null, certificatePassword.toCharArray());
            clientKeyStore.setCertificateEntry("client", clientCertificate);

            // 创建TrustManager，用于验证服务器证书
            TrustManager[] trustManagers = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }
                        public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
                            // 在这里可以对服务器证书进行验证，如果不满足条件抛出CertificateException
                        }
                    }
            };

            // 初始化SSL上下文
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagers, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

            // 打开HTTPS连接
            URL url = new URL(httpsUrl);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

            // 设置请求方法
            connection.setRequestMethod("GET");

            // 获取响应
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // 处理响应数据
                System.out.println("Response: " + response.toString());
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
