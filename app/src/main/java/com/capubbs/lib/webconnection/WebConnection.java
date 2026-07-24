package com.capubbs.lib.webconnection;

import android.util.Log;

import com.capubbs.lib.XML2Json;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.X509TrustManager;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class WebConnection {

    private static OkHttpClient client;
    private static OkHttpClient downloadClient;

    private static OkHttpClient getClient() {
        if (client == null) {
            client = new OkHttpClient.Builder()
                    .connectTimeout(4, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(13, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(13, java.util.concurrent.TimeUnit.SECONDS)
                    .hostnameVerifier((hostname, session) -> true)
                    .sslSocketFactory(new MySSLSocketFactory(), new MyTrustManager())
                    .followRedirects(true)
                    .followSslRedirects(true)
                    .build();
        }
        return client;
    }

    /** 图片/文件下载专用 client：更长超时 + UA + Referer */
    private static OkHttpClient getDownloadClient() {
        if (downloadClient == null) {
            downloadClient = new OkHttpClient.Builder()
                    .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .hostnameVerifier((hostname, session) -> true)
                    .sslSocketFactory(new MySSLSocketFactory(), new MyTrustManager())
                    .followRedirects(true)
                    .followSslRedirects(true)
                    .retryOnConnectionFailure(true)
                    .build();
        }
        return downloadClient;
    }

    /** 通用 User-Agent，避免服务器拒绝无 UA 的请求 */
    private static final String USER_AGENT =
            "Mozilla/5.0 (Linux; Android " + android.os.Build.VERSION.RELEASE + ") " +
            "CAPUBBS/" + com.capubbs.lib.Constants.version;

    /** 图片/文件下载专用 connect：使用更长超时的 client + UA + Referer */
    public static InputStream connectForDownload(String url) throws IOException {
        url = url.trim();
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .get()
                .header("User-Agent", USER_AGENT);

        // 对 chexie.net 的图片请求添加 Referer，避免防盗链拦截
        if (url.contains("chexie.net")) {
            requestBuilder.header("Referer", com.capubbs.lib.Constants.base_url + "/bbs/");
        }

        Cookies.addCookie(requestBuilder, url);
        Response response = getDownloadClient().newCall(requestBuilder.build()).execute();
        Cookies.setCookie(response, url);
        ResponseBody body = response.body();
        if (body != null) {
            return body.byteStream();
        }
        throw new IOException("Empty response body");
    }

    public static Parameters connect(String url, List<Parameters> params) {
        if (params == null || params.size() == 0) {
            return connectWithGet(url);
        }
        try {
            url = url.trim();

            FormBody.Builder formBodyBuilder = new FormBody.Builder();
            for (Parameters paraItem : params) {
                String string = paraItem.value;
                if (string == null || "".equals(string)) continue;
                formBodyBuilder.add(paraItem.name, paraItem.value);
                if (string.length() >= 300)
                    string = string.substring(0, 299);
                Log.w(paraItem.name, string);
            }

            RequestBody requestBody = formBodyBuilder.build();

            Request.Builder requestBuilder = new Request.Builder()
                    .url(url)
                    .post(requestBody);

            Cookies.addCookie(requestBuilder, url);

            Log.w("URL", url);

            Response response = getClient().newCall(requestBuilder.build()).execute();

            Parameters parameters = new Parameters("", "");
            int returncode = response.code();

            boolean isGbk = false;
            String typeString = response.header("Content-type", "").toLowerCase(Locale.getDefault());
            if (typeString.contains("gbk") || typeString.contains("gb2312"))
                isGbk = true;

            Cookies.setCookie(response, url);

            parameters.name = returncode + "";

            if (returncode == 200) {
                ResponseBody body = response.body();
                if (body != null) {
                    BufferedReader bf;
                    if (!isGbk)
                        bf = new BufferedReader(
                                new InputStreamReader(body.byteStream()));
                    else
                        bf = new BufferedReader(
                                new InputStreamReader(body.byteStream(), "gbk"));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line = bf.readLine();
                    while (line != null) {
                        stringBuilder.append(line).append("\n");
                        line = bf.readLine();
                    }
                    String string = stringBuilder.toString().trim();
                    // 只对 XML 响应进行 JSON 转换，非 XML 响应直接传递原始内容
                    if (string.startsWith("<?xml") || string.startsWith("<capu")) {
                        parameters.value = XML2Json.toJson(string);
                    } else {
                        parameters.value = string;
                    }
                }
            }
            return parameters;
        } catch (Exception e) {
            Log.e("WebConnection", "connect error", e);
            return new Parameters("-1", "");
        }
    }

    private static Parameters connectWithGet(String url) {
        try {
            url = url.trim();

            Request.Builder requestBuilder = new Request.Builder()
                    .url(url)
                    .get();

            Cookies.addCookie(requestBuilder, url);

            Response response = getClient().newCall(requestBuilder.build()).execute();

            Parameters parameters = new Parameters("", "");
            int returncode = response.code();

            boolean isGbk = false;
            String typeString = response.header("Content-type", "").toLowerCase(Locale.getDefault());
            if (typeString.contains("gbk") || typeString.contains("gb2312"))
                isGbk = true;

            Cookies.setCookie(response, url);

            parameters.name = returncode + "";

            if (returncode == 200) {
                ResponseBody body = response.body();
                if (body != null) {
                    BufferedReader bf;
                    if (!isGbk)
                        bf = new BufferedReader(
                                new InputStreamReader(body.byteStream()));
                    else
                        bf = new BufferedReader(
                                new InputStreamReader(body.byteStream(), "gbk"));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line = bf.readLine();
                    while (line != null) {
                        stringBuilder.append(line).append("\n");
                        line = bf.readLine();
                    }
                    String string = stringBuilder.toString().trim();
                    // 只对 XML 响应进行 JSON 转换，非 XML 响应（如 HTML 页面）直接传递原始内容
                    if (string.startsWith("<?xml") || string.startsWith("<capu")) {
                        parameters.value = XML2Json.toJson(string);
                    } else {
                        parameters.value = string;
                    }
                }
            }
            return parameters;
        } catch (Exception e) {
            return new Parameters("-1", "");
        }
    }

    public static InputStream connect(String url) throws IOException {
        url = url.trim();

        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .get();

        Cookies.addCookie(requestBuilder, url);

        Log.w("URL", url);

        Response response = getClient().newCall(requestBuilder.build()).execute();
        Cookies.setCookie(response, url);

        ResponseBody body = response.body();
        if (body != null) {
            return body.byteStream();
        }
        throw new IOException("Empty response body");
    }

    static class MyTrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    static class MySSLSocketFactory extends javax.net.ssl.SSLSocketFactory {
        private final javax.net.ssl.SSLSocketFactory delegate;

        public MySSLSocketFactory() {
            try {
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, new X509TrustManager[]{new MyTrustManager()}, null);
                delegate = sslContext.getSocketFactory();
            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String[] getDefaultCipherSuites() {
            return delegate.getDefaultCipherSuites();
        }

        @Override
        public String[] getSupportedCipherSuites() {
            return delegate.getSupportedCipherSuites();
        }

        @Override
        public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
            SSLSocket socket = (SSLSocket) delegate.createSocket(s, host, port, autoClose);
            return socket;
        }

        @Override
        public Socket createSocket(String host, int port) throws IOException {
            return delegate.createSocket(host, port);
        }

        @Override
        public Socket createSocket(String host, int port, java.net.InetAddress localHost, int localPort) throws IOException {
            return delegate.createSocket(host, port, localHost, localPort);
        }

        @Override
        public Socket createSocket(java.net.InetAddress host, int port) throws IOException {
            return delegate.createSocket(host, port);
        }

        @Override
        public Socket createSocket(java.net.InetAddress address, int port, java.net.InetAddress localAddress, int localPort) throws IOException {
            return delegate.createSocket(address, port, localAddress, localPort);
        }
    }

}