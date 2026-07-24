package com.capubbs.lib.webconnection;

import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Request;
import okhttp3.Response;

public class Cookies {

    private static final HashMap<String, HashMap<String, String>> cookies = new HashMap<>();

    private static String getDomain(String url) {
        try {
            String domain = url;
            int pos1 = domain.indexOf("://") + 3;
            domain = url.substring(pos1);
            int pos2 = domain.indexOf("/");
            if (pos2 == -1) return domain;
            return domain.substring(0, pos2).trim();
        } catch (Exception e) {
            return "";
        }
    }

    public static void setCookie(Response response, String url) {
        String domain = getDomain(url);
        if (domain.equals("")) return;
        HashMap<String, String> hashMap = cookies.get(domain);
        if (hashMap == null) hashMap = new HashMap<>();
        if (!whetherToSetCookie(url)) return;

        List<String> cookiesList = response.headers("Set-Cookie");
        if (cookiesList == null || cookiesList.isEmpty()) return;

        for (String cookie : cookiesList) {
            String[] strings = cookie.split(";");
            String[] strings2 = strings[0].split("=");
            String key = strings2[0].trim();
            String value = strings2.length > 1 ? strings2[1].trim() : "";
            if (!"".equals(key)) {
                Log.w("Set-cookie:", domain + ": " + key + "=" + value);
                hashMap.put(key, value);
            }
        }
        cookies.put(domain, hashMap);
    }

    /**
     * 手动设置指定域名的 cookie（用于 token 等需要主动注入的场景）
     */
    public static void setCookieManually(String url, String key, String value) {
        String domain = getDomain(url);
        if (domain.equals("")) return;
        HashMap<String, String> hashMap = cookies.get(domain);
        if (hashMap == null) hashMap = new HashMap<>();
        hashMap.put(key, value);
        cookies.put(domain, hashMap);
        Log.w("Set-cookie-manual:", domain + ": " + key + "=" + value);
    }

    public static void addCookie(Request.Builder requestBuilder, String url) {
        String domain = getDomain(url);
        Log.w("domain", domain);
        if (domain.equals("")) return;
        if (!whetherToSendCookie(url)) return;
        HashMap<String, String> hashMap = cookies.get(domain);
        if (hashMap == null) return;
        StringBuilder cookieString = new StringBuilder();
        for (Map.Entry<String, String> entry : hashMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            cookieString.append(key).append("=").append(value).append(";");
        }
        if (cookieString.length() > 0) {
            Log.w("cookie", cookieString.toString());
            requestBuilder.addHeader("Cookie", cookieString.toString());
        }
    }

    private static boolean whetherToSendCookie(String url) {
        return true;
    }

    private static boolean whetherToSetCookie(String url) {
        return true;
    }

}