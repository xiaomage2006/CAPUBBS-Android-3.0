package com.capubbs;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;

import com.capubbs.lib.Constants;
import com.capubbs.lib.MyCalendar;
import com.capubbs.lib.RequestingTask;
import com.capubbs.lib.Util;
import com.capubbs.lib.XML2Json;
import com.capubbs.lib.view.CustomToast;
import com.capubbs.lib.webconnection.Parameters;

import com.capubbs.lib.json.JSONArray;
import com.capubbs.lib.json.JSONObject;

import java.util.ArrayList;

public class ViewThread {
    ArrayList<ThreadInfo> threadInfos = new ArrayList<>();
    int page;
    int tmpPage;
    int totalPage;
    int selection;
    boolean isExtr;

    ViewActivity viewActivity;

    /** ViewHolder 避免每次 getView() 都 inflate + findViewById */
    private static class ThreadItemHolder {
        android.widget.TextView title, author, time;
    }

    public ViewThread(ViewActivity activity) {viewActivity=activity;isExtr=false;}


    public void getThreads(int page) {
        getThreads(page, isExtr);
    }
    public void getThreads(int page, boolean extr) {
        ArrayList<Parameters> arrayList = new ArrayList<>();
        arrayList.add(new Parameters("ask", "show"));
        arrayList.add(new Parameters("p", page + ""));
        arrayList.add(new Parameters("bid", viewActivity.board));
        arrayList.add(new Parameters("token", Userinfo.token));
        if (extr) arrayList.add(new Parameters("extr", "1"));
        new RequestingTask(viewActivity, "正在获取...",
                Constants.bbs_url, Constants.REQUEST_BBS_GET_LIST)
                .execute(arrayList);
        tmpPage = page;
        isExtr=extr;
    }

    void finishRequest(String string) {
        try {
            JSONArray jsonArray=new JSONArray(string);
            //JSONObject jsonObject = new JSONObject(string);
            int code = jsonArray.getJSONObject(0).getInt("code");
            if (code != 0 && code != -1) {
                CustomToast.showErrorToast(viewActivity,
                        jsonArray.getJSONObject(0).optString("msg", XML2Json.getErrorMessage(code)));
                viewActivity.setContentView(R.layout.bbs_thread_listview);
                viewActivity.showingPage = ViewActivity.PAGE_THREAD;
                return;
            }
            totalPage = jsonArray.getJSONObject(0).getInt("pages");
           // JSONArray datas = jsonObject.getJSONArray("datas");
            threadInfos.clear();
            int len = jsonArray.length();
            for (int i = 0; i < len; i++) {
                JSONObject thread = jsonArray.getJSONObject(i);
                String[] strings=thread.optString("author").split("/");
                ThreadInfo ti = new ThreadInfo(viewActivity.board, Board.getNameById(viewActivity.board),
                        strings[0].trim(), strings.length>1?strings[1].trim():"", thread.optString("time"),
                        thread.optString("text"), thread.getInt("tid"), thread.optInt("top"), thread.optInt("extr"),
                        thread.optInt("lock"));
                // 预格式化标题，避免在 getView() 中反复调用 Html.fromHtml()
                String title = "<font color='#006060'>" + Util.stripHtmlTags(ti.title) + "</font>";
                if (ti.isTop) title = "<font color='red'>[置顶]</font> " + title;
                if (ti.isExtr) title += " <font color='blue'>[精华]</font>";
                if (ti.isLock) title += " <font color='gray'>[锁定]</font>";
                ti.formattedTitle = Util.fromHtml(title);
                threadInfos.add(ti);
            }

            page = tmpPage;
            selection = 0;
            viewThreads();
        } catch (Exception e) {
            threadInfos.clear();
            CustomToast.showErrorToast(viewActivity, "获取失败");
        }
    }

    public void viewThreads() {
        viewActivity.setContentView(R.layout.bbs_thread_listview);
        viewActivity.showingPage = ViewActivity.PAGE_THREAD;
        viewActivity.invalidateOptionsMenu();
        viewActivity.setTitle("(" + page + "/" + totalPage + ") " + viewActivity.boardName);
        final ListView listView = (ListView) viewActivity.findViewById(R.id.bbs_thread_listview);
        listView.setAdapter(new BaseAdapter() {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                ThreadItemHolder holder;
                if (convertView == null) {
                    convertView = viewActivity.getLayoutInflater()
                            .inflate(R.layout.bbs_thread_item, parent, false);
                    holder = new ThreadItemHolder();
                    holder.title = convertView.findViewById(R.id.bbs_thread_item_title);
                    holder.author = convertView.findViewById(R.id.bbs_thread_item_author);
                    holder.time = convertView.findViewById(R.id.bbs_thread_item_time);
                    convertView.setTag(holder);
                } else {
                    holder = (ThreadItemHolder) convertView.getTag();
                }

                ThreadInfo threadInfo = threadInfos.get(position);
                holder.title.setText(threadInfo.formattedTitle);
                holder.author.setText(threadInfo.author + " / " + threadInfo.replyer);
                holder.time.setText(MyCalendar.format(threadInfo.time));
                return convertView;
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public int getCount() {
                return threadInfos.size();
            }
        });
        listView.setOnItemClickListener((parent, view, position, id) -> {
            ThreadInfo threadInfo = threadInfos.get(position);
            String threadid = threadInfo.threadid + "";
            selection = listView.getFirstVisiblePosition();
            viewActivity.viewPost.getPosts(threadid, 1);
        });
        listView.setSelection(selection);
    }

    public void jump() {
        if (totalPage <= 0) {
            com.capubbs.lib.view.CustomToast.showErrorToast(viewActivity, "数据未加载完成");
            return;
        }
        try {
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(viewActivity);
            builder.setTitle("跳转（当前第" + page + "页，共" + totalPage + "页）");
            builder.setNegativeButton("取消", null);

            final Spinner spinner = new Spinner(builder.getContext());
            ArrayList<String> arrayList = new ArrayList<>();
            for (int i = 1; i <= totalPage; i++)
                arrayList.add("第 " + i + " 页" + (i == page ? "（当前）" : ""));
            ArrayAdapter<String> adapter = new ArrayAdapter<>(builder.getContext(),
                    android.R.layout.simple_spinner_item, arrayList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
            spinner.setSelection(page - 1);

            // 添加内边距让布局更美观
            LinearLayout container = new LinearLayout(builder.getContext());
            container.setOrientation(LinearLayout.VERTICAL);
            int paddingH = (int) (24 * viewActivity.getResources().getDisplayMetrics().density);
            int paddingV = (int) (8 * viewActivity.getResources().getDisplayMetrics().density);
            container.setPadding(paddingH, paddingV, paddingH, paddingV);
            container.addView(spinner);

            builder.setView(container);
            builder.setPositiveButton("跳转", (dialog, which) -> {
                try {
                    int targetPage = spinner.getSelectedItemPosition() + 1;
                    if (targetPage > 0 && targetPage <= totalPage) {
                        getThreads(targetPage);
                    }
                } catch (Exception e) {
                    com.capubbs.lib.view.CustomToast.showErrorToast(viewActivity, "跳转失败");
                }
            });
            builder.show();
        } catch (Exception e) {
            com.capubbs.lib.view.CustomToast.showErrorToast(viewActivity, "跳转失败");
        }
    }

}
