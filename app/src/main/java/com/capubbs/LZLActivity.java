package com.capubbs;

import static android.text.Html.*;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.capubbs.lib.BaseActivity;
import com.capubbs.lib.Constants;
import com.capubbs.lib.MyCalendar;
import com.capubbs.lib.RequestingTask;
import com.capubbs.lib.Util;
import com.capubbs.lib.ViewSetting;
import com.capubbs.lib.XML2Json;
import com.capubbs.lib.json.JSONArray;
import com.capubbs.lib.json.JSONObject;
import com.capubbs.lib.view.CustomToast;
import com.capubbs.lib.webconnection.Cookies;
import com.capubbs.lib.webconnection.Parameters;

import java.util.ArrayList;

public class LZLActivity extends BaseActivity {

    int fid;
    String bid = "";
    String tid = "";
    int lzlIndex = -1;

    static class LZLInfo {
        String author;
        int lzlid;
        String content;
        long time;

        public LZLInfo(int _lzlid, String _author, String _content, String _time) {
            lzlid=_lzlid;author=_author;content=_content;time= MyCalendar.format(_time);
        }
    }

    ArrayList<LZLInfo> lzlInfos=new ArrayList<>();
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fid=getIntent().getIntExtra("fid",-1);
        bid=getIntent().getStringExtra("bid");
        tid=getIntent().getStringExtra("tid");
        if (fid==-1) {
            wantToExit();
            return;
        }
        listView=new ListView(this);
        setContentView(listView);
        setTitle("查看楼中楼");
        registerForContextMenu(listView);
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            lzlIndex = position;
            return false;
        });
        get();
        if (getIntent().getBooleanExtra("add",false))
            reply("");
    }

    protected void finishRequest(int requesttype, String string) {
        if (requesttype== Constants.REQUEST_BBS_LZL_SHOW) {
            try {
                lzlInfos.clear();
                JSONArray jsonArray=new JSONArray(string);
                int code=jsonArray.getJSONObject(0).getInt("code");
                if (code!=0 && code!=-1) {
                    CustomToast.showErrorToast(this, jsonArray.getJSONObject(0).optString("msg", XML2Json.getErrorMessage(code)));
                }
                else {
                    int len=jsonArray.length();
                    for (int i=1;i<len;i++) {
                        JSONObject jsonObject=jsonArray.getJSONObject(i);
                        lzlInfos.add(new LZLInfo(jsonObject.optInt("id"), jsonObject.optString("author"), jsonObject.optString("text"),
                                jsonObject.optString("time")));
                    }
                }
            }
            catch (Exception e) {
                lzlInfos.clear();
            }
            show();
            return;
        }
        if (requesttype==Constants.REQUEST_BBS_LZL_POST) {
            try {
                JSONArray jsonArray=new JSONArray(string);
                int code=jsonArray.getJSONObject(0).getInt("code");
                if (code!=0 && code!=-1)
                    CustomToast.showErrorToast(this, jsonArray.getJSONObject(0).optString("msg", XML2Json.getErrorMessage(code)));
                else {
                    CustomToast.showSuccessToast(this, "发送成功");
                    setResult(RESULT_OK);
                    get();
                }
            }
            catch (Exception e) {CustomToast.showErrorToast(this, "发送失败");}
        }
        if (requesttype == Constants.REQUEST_BBS_LZL_DELETE) {
            android.util.Log.w("LZLActivity", "finishLzlDelete response: " + string);
            // /bbs/delete/ 返回极简响应，HTTP 200 即视为成功
            if (string == null || string.isEmpty() || "1".equals(string.trim())) {
                // 本地移除该楼层
                if (lzlIndex >= 0 && lzlIndex < lzlInfos.size()) {
                    lzlInfos.remove(lzlIndex);
                    lzlIndex = -1;
                    show();
                }
                CustomToast.showSuccessToast(this, "删除成功");
                setResult(RESULT_OK);
                return;
            }
            // 兼容 JSON 格式响应
            try {
                JSONArray jsonArray = new JSONArray(string);
                if (jsonArray.length() > 0) {
                    int code = jsonArray.getJSONObject(0).getInt("code");
                    if (code != 0) {
                        String msg = jsonArray.getJSONObject(0).optString("msg", XML2Json.getErrorMessage(code));
                        CustomToast.showErrorToast(this, msg);
                        android.util.Log.w("LZLActivity", "lzl delete failed code=" + code + " msg=" + msg);
                        return;
                    }
                }
                if (lzlIndex >= 0 && lzlIndex < lzlInfos.size()) {
                    lzlInfos.remove(lzlIndex);
                    lzlIndex = -1;
                    show();
                }
                CustomToast.showSuccessToast(this, "删除成功");
                setResult(RESULT_OK);
            } catch (Exception e) {
                // 非 JSON 响应但 HTTP 200，视为成功
                android.util.Log.w("LZLActivity", "lzl delete non-JSON response, treating as success, raw=" + string);
                if (lzlIndex >= 0 && lzlIndex < lzlInfos.size()) {
                    lzlInfos.remove(lzlIndex);
                    lzlIndex = -1;
                    show();
                }
                CustomToast.showSuccessToast(this, "删除成功");
                setResult(RESULT_OK);
            }
        }
    }

    void get() {
        ArrayList<Parameters> arrayList=new ArrayList<>();
        arrayList.add(new Parameters("ask","lzl"));
        arrayList.add(new Parameters("method","show"));
        arrayList.add(new Parameters("fid",""+fid));
        arrayList.add(new Parameters("token",Userinfo.token));

        new RequestingTask(this, "正在获取...", Constants.bbs_url, Constants.REQUEST_BBS_LZL_SHOW)
                .execute(arrayList);
    }

    void show() {
        listView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return lzlInfos.size();
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @SuppressLint("ViewHolder")
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                convertView=getLayoutInflater().inflate(R.layout.lzl_view,parent,false);
                LZLInfo lzlInfo=lzlInfos.get(position);
                ViewSetting.setTextView(convertView,R.id.author,lzlInfo.author);
                String cleanedContent = Util.cleanHtmlContent(lzlInfo.content);
                ViewSetting.setTextView(convertView,R.id.text, fromHtml(cleanedContent));
                ViewSetting.setTextView(convertView,R.id.time,MyCalendar.format(lzlInfo.time));
                return convertView;
            }
        });
        listView.setOnItemClickListener((parent, view, position, id) -> {
            String author=ViewSetting.getTextView(view,R.id.author);
            if (!"".equals(author))
                reply("回复 @"+author+": ");
            else reply("");
        });
    }
    @SuppressLint("InflateParams")
    void reply(String string) {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        final View view=getLayoutInflater().inflate(R.layout.edittext,null,false);
        ViewSetting.setEditTextValue(view,R.id.edittext,string);
        builder.setView(view).setTitle("回复楼中楼").setPositiveButton("发送", (dialog, which) -> {
            String content = ViewSetting.getEditTextValue(view, R.id.edittext).trim();
            if (content.length() >= 240) {
                CustomToast.showErrorToast(LZLActivity.this, "内容不要超过240字");
                reply(content);
                return;
            }
            if (content.isEmpty()) {
                CustomToast.showErrorToast(LZLActivity.this, "内容不能为空");
                reply("");
                return;
            }
            ArrayList<Parameters> arrayList=new ArrayList<>();
            arrayList.add(new Parameters("ask","lzl"));
            arrayList.add(new Parameters("method","post"));
            arrayList.add(new Parameters("fid",fid+""));
            arrayList.add(new Parameters("text",content));
            arrayList.add(new Parameters("token",Userinfo.token));

            new RequestingTask(LZLActivity.this, "正在发送", Constants.bbs_url, Constants.REQUEST_BBS_LZL_POST).execute(arrayList);

        }).setNegativeButton("取消",null).show();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (lzlIndex >= 0 && lzlIndex < lzlInfos.size()) {
            LZLInfo lzlInfo = lzlInfos.get(lzlIndex);
            if (Userinfo.isOwner(lzlInfo.author)) {
                menu.add(Menu.NONE, Constants.CONTEXT_MENU_LZL_DELETE, Constants.CONTEXT_MENU_LZL_DELETE, "删除");
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == Constants.CONTEXT_MENU_LZL_DELETE) {
            if (lzlIndex < 0 || lzlIndex >= lzlInfos.size()) return true;
            LZLInfo lzlInfo = lzlInfos.get(lzlIndex);
            if (!Userinfo.isOwner(lzlInfo.author)) {
                CustomToast.showInfoToast(this, "只能删除自己的楼中楼");
                return true;
            }
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("删除确认")
                    .setMessage("确定要删除该楼中楼吗？")
                    .setPositiveButton("确定", (dialog, which) -> requestLzlDelete(lzlInfo.lzlid))
                    .setNegativeButton("取消", null)
                    .show();
            return true;
        }
        return super.onContextItemSelected(item);
    }

    // 发起楼中楼删除请求
    void requestLzlDelete(int lzlid) {
        String deleteUrl = Constants.base_url + "/bbs/deletelzl/";
        if (Userinfo.token != null && !Userinfo.token.isEmpty()) {
            Cookies.setCookieManually(deleteUrl, "token", Userinfo.token);
        }
        ArrayList<Parameters> arrayList = new ArrayList<>();
        arrayList.add(new Parameters("fid", fid + ""));
        arrayList.add(new Parameters("id", lzlid + ""));
        android.util.Log.w("LZLActivity", "requestLzlDelete: url=" + deleteUrl + " fid=" + fid + " lzlid=" + lzlid + " token=" + Userinfo.token);
        new RequestingTask(this, "正在删除...", deleteUrl, Constants.REQUEST_BBS_LZL_DELETE).execute(arrayList);
    }

    protected void wantToExit() {
        Intent intent=new Intent();
        intent.putExtra("lzl",lzlInfos.size());
        setResult(RESULT_OK,intent);
        finish();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        menu.add(Menu.NONE, Constants.MENU_BBS_LZL_POST, Constants.MENU_BBS_LZL_POST, "")
                .setIcon(R.drawable.ic_reply_white_36dp).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==Constants.MENU_BBS_LZL_POST) {
            reply("");
        }
        return super.onOptionsItemSelected(item);
    }
}