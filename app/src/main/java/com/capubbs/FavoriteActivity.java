package com.capubbs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.capubbs.lib.BaseActivity;
import com.capubbs.lib.Constants;
import com.capubbs.lib.Editor;
import com.capubbs.lib.MyCalendar;
import com.capubbs.lib.Util;
import com.capubbs.lib.ViewSetting;
import com.capubbs.lib.view.CustomToast;

import com.capubbs.lib.json.JSONArray;
import com.capubbs.lib.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

public class FavoriteActivity extends BaseActivity {

    private static ArrayList<SearchInfo> arrayList=null;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("我的收藏");
        load(this);
        setContentView(R.layout.bbs_thread_listview);
        listView=(ListView)findViewById(R.id.bbs_thread_listview);
        listView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return arrayList.size();
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
                //return null;
                SearchInfo threadInfo=arrayList.get(position);
                convertView = getLayoutInflater().inflate(
                        R.layout.bbs_thread_item, parent, false);
                String title = "<font color='#006060'>" + threadInfo.title + "</font>";
                ViewSetting.setTextView(convertView, R.id.bbs_thread_item_title,
                        Util.fromHtml(title));
                ViewSetting.setTextView(convertView, R.id.bbs_thread_item_author,
                        threadInfo.author + " / " + Board.getNameById(threadInfo.board));
                ViewSetting.setTextViewColor(convertView, R.id.bbs_thread_item_author,
                        Color.parseColor("#333333"));
                ViewSetting.setTextView(convertView, R.id.bbs_thread_item_time,
                        "收藏于 " + MyCalendar.format(threadInfo.timestamp, "yyyy-MM-dd"));
                ViewSetting.setTextViewColor(convertView, R.id.bbs_thread_item_time,
                        Color.parseColor("#333333"));
                return convertView;
            }
        });
        listView.setOnItemClickListener((parent, view, position, id) -> {
            SearchInfo threadInfo = arrayList.get(position);
            Intent intent = new Intent(FavoriteActivity.this, ViewActivity.class);
            intent.putExtra("bid", threadInfo.board);
            intent.putExtra("tid", threadInfo.threadid + "");
            intent.putExtra("type", "thread");
            startActivity(intent);
        });
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            final SearchInfo threadInfo = arrayList.get(position);
            String[] strings = new String[]{"删除此条"};
            new AlertDialog.Builder(FavoriteActivity.this)
                    .setItems(strings, (dialog, which) -> {
                        delFavorite(FavoriteActivity.this,
                                threadInfo.board, threadInfo.threadid);
                        ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
                    }).show();
            return true;
        });
        if (arrayList.size()==0) {
            CustomToast.showInfoToast(this, "你还没有收藏的帖子哦");
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, Constants.MENU_SUBACTIVITY_CLOSE, Constants.MENU_SUBACTIVITY_CLOSE, "")
                .setIcon(R.drawable.ic_close_white_36dp).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==Constants.MENU_SUBACTIVITY_CLOSE) {
            wantToExit();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private static void load(Context context) {
        String favorite= Editor.getString(context, "bbs_favorite_posts", "[]");
        arrayList=new ArrayList<>();
        try {
            JSONArray jsonArray=new JSONArray(favorite);
            int len=jsonArray.length();
            for (int i=0;i<len;i++) {
                JSONObject jsonObject=jsonArray.getJSONObject(i);
                SearchInfo searchInfo=new SearchInfo(jsonObject.getString("board"),
                        jsonObject.getInt("threadid"), jsonObject.getString("title"),
                        jsonObject.optString("author"), 0,
                        jsonObject.getLong("time"));
                arrayList.add(searchInfo);
            }
        }
        catch (Exception e) {}
    }

    private static void save(Context context) {
        if (arrayList==null) return;
        try {
            Collections.sort(arrayList, (lhs, rhs) -> (int)(rhs.timestamp-lhs.timestamp));
            JSONArray jsonArray=new JSONArray();
            int len=arrayList.size();
            for (int i=0;i<len;i++) {
                SearchInfo searchInfo=arrayList.get(i);
                JSONObject jsonObject=new JSONObject();
                jsonObject.put("board", searchInfo.board);
                jsonObject.put("threadid", searchInfo.threadid);
                jsonObject.put("title", searchInfo.title);
                jsonObject.put("author", searchInfo.author);
                jsonObject.put("time", searchInfo.timestamp);
                jsonArray.put(jsonObject);
            }
            Editor.putString(context, "bbs_favorite_posts", jsonArray.toString());
        }
        catch (Exception e) {CustomToast.showErrorToast(context, "保存失败");}
    }

    public static boolean addFavorite(Context context,
                                      String board, int threadid, String title,
                                      String author) {
        if (arrayList==null) load(context);
        for (SearchInfo info: arrayList.toArray(new SearchInfo[arrayList.size()])) {
            if (info.threadid==threadid && info.board.equals(board)) {
                CustomToast.showInfoToast(context, "已收藏过此帖子");
                return false;
            }
        }
        arrayList.add(new SearchInfo(board, threadid, title, author, 0, System.currentTimeMillis()));
        save(context);
        return true;
    }

    public static void delFavorite(Context context, String board, int threadid) {
        if (arrayList==null) load(context);
        for (SearchInfo info: arrayList.toArray(new SearchInfo[arrayList.size()])) {
            if (info.threadid==threadid && info.board.equals(board)) {
                arrayList.remove(info);
                save(context);
                return;
            }
        }
    }

    @Override
    protected void finishRequest(int type, String string) {

    }

}
