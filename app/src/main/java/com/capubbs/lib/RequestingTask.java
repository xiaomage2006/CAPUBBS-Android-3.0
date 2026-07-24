package com.capubbs.lib;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.capubbs.lib.view.CustomToast;
import com.capubbs.lib.webconnection.Parameters;
import com.capubbs.lib.webconnection.WebConnection;

import java.util.ArrayList;

@SuppressWarnings("deprecation")
public class RequestingTask extends AsyncTask<ArrayList<Parameters>, String, Parameters> {
    ProgressDialog progressDialog;
    String requestString;
    int requestType;
    BaseActivity baseActivity;

    /**
     *
     *
     * @param msg  显示消息
     * @param url  请求地址
     * @param type 请求类型
     */
    public RequestingTask(BaseActivity activity, String msg, String url, int type) {
        baseActivity = activity;
        progressDialog = new ProgressDialog(activity);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setTitle("提示");
        progressDialog.setMessage(msg);
        progressDialog.setIndeterminate(false);
        progressDialog.setCancelable(true);
        requestString = url;
        requestType = type;
    }

    @Override
    protected void onPreExecute() {
        progressDialog.show();
    }

    @Override
    protected Parameters doInBackground(ArrayList<Parameters>... params) {
        return WebConnection.connect(requestString, params[0]);
    }

    @Override
    protected void onPostExecute(Parameters parameters) {
        progressDialog.dismiss();
        if (!"200".equals(parameters.name)) {
            if ("-1".equals(parameters.name)) {
                CustomToast.showInfoToast(baseActivity, "无法连接服务器(-1,-1)");
            } else
                CustomToast.showInfoToast(baseActivity, "无法连接到服务器 (HTTP " + parameters.name + ")");
        } else baseActivity.finishRequest(requestType, parameters.value);
    }

}
