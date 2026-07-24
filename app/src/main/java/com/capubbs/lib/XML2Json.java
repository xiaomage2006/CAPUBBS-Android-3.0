package com.capubbs.lib;

import android.util.Log;

import com.capubbs.lib.json.*;

/**
 * Created by oc on 2016/10/13.
 */
public class XML2Json {

    public static String toJson(String xml) {
        try {
            //String string=XML.toJSONObject(xml).optJSONObject("capu").optJSONArray("info").toString();
            JSONObject jsonObject=XML.toJSONObject(xml).optJSONObject("capu");
            String string;
            if (jsonObject.optJSONArray("info")!=null)
                string=jsonObject.optJSONArray("info").toString();
            else {
                JSONArray jsonArray=new JSONArray();
                jsonArray.put(jsonObject.optJSONObject("info")==null?jsonObject:jsonObject.optJSONObject("info"));
                string=jsonArray.toString();
            }
            Log.w("json", string);
            return string;
        }
        catch (Exception e) {
            Log.e("XML2Json", "toJson error", e);
        }
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("code", 20);
            jsonObject.put("msg", "XML");
            JSONArray jsonArray=new JSONArray();
            jsonArray.put(jsonObject);
            return jsonArray.toString();
        }
        catch (Exception e) {Log.e("XML2Json", "fallback error", e);}
        return "";
    }

    public static String getErrorMessage(int code) {
        switch (code) {
            case -25:return " (errorcode: -1)";
            case 1:return "(errorcode: 1)";
            case 2:return "(errorcode: 2)";
            case 3:return "(errorcode: 3)";
            case 4:return "15s (errorcode: 4)";
            case 5:return "(errorcode: 5)";
            case 6:return "(errorcode: 6)";
            case 7:return "ֻ(errorcode: 7)";
            case 8:return "(errorcode: 8)";
            case 9:return "(errorcode: 9)";
            case 10:return "(errorcode: 10)";
            case 11:return "(errorcode: 11)";
            default:break;
        }
        return "(errorcode: "+code+")";
    }


}
