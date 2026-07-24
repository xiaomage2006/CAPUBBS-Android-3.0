package com.capubbs;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.capubbs.lib.Constants;
import com.capubbs.lib.Editor;
import com.capubbs.lib.MyCalendar;
import com.capubbs.lib.RequestingTask;
import com.capubbs.lib.view.CustomToast;
import com.capubbs.lib.webconnection.WebConnection;

import com.capubbs.lib.json.JSONArray;
import com.capubbs.lib.json.JSONObject;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TopFragment extends Fragment {
    public static TopFragment topFragment;
    public static ArrayList<ThreadInfo> tops = new ArrayList<>();
    public static ArrayList<ThreadInfo> topPinnedPosts = new ArrayList<>();
    static View topView;

    private static final int PINNED_COLOR = 0xFF1A5276;
    private static final int NORMAL_COLOR = 0xFF006060;

    /** ViewHolder 避免每次 getView() 都 inflate + findViewById */
    private static class TopItemHolder {
        android.widget.TextView rank, from, title, author, time;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.bbs_top_listview, container, false);
        topFragment = this;
        topView = rootView;
        realShowView((BBSActivity)getActivity(), true);
        return rootView;
    }

    public static void showView(BBSActivity bbsActivity) {
        new RequestingTask(bbsActivity, "正在获取热门帖...", Constants.bbs_url+"?ask=hot&hotnum=30",
                Constants.REQUEST_BBS_GET_TOP).execute(new ArrayList<>());
        // 与热门帖 API 并行抓取置顶帖，避免串行等待
        fetchPinnedPosts();
    }

    /**
     * 从网页端首页抓取置顶帖，在后台线程执行
     */
    public static void fetchPinnedPosts() {
        new Thread(() -> {
            try {
                String homepageUrl = Constants.base_url + "/bbs/";
                com.capubbs.lib.webconnection.Parameters result =
                        WebConnection.connect(homepageUrl, null);
                if (!"200".equals(result.name) || result.value == null) return;

                ArrayList<ThreadInfo> pinned = new ArrayList<>();
                // 匹配置顶帖链接: <a href='../content/?bid=X&tid=Y&p=Z#N'>标题</a>
                Pattern pattern = Pattern.compile(
                        "<a\\s+href='\\.\\./content/\\?bid=(\\d+)&tid=(\\d+)&p=\\d+#\\d+'>([^<]*?【置顶】[^<]*?)</a>" +
                        ".*?<span\\s+class='hint2'>([^<]+)</span>.*?<span\\s+class='hint2'>([^<]+)</span>",
                        Pattern.DOTALL);
                Matcher matcher = pattern.matcher(result.value);
                int rank = 1;
                while (matcher.find()) {
                    try {
                        String bid = matcher.group(1);
                        int tid = Integer.parseInt(matcher.group(2));
                        String title = matcher.group(3).replace("【置顶】", "").trim();
                        String author = matcher.group(4).trim();
                        String time = matcher.group(5).trim();
                        ThreadInfo info = new ThreadInfo(rank, bid, Board.getNameById(bid),
                                author, author, time, title, tid, 1);
                        pinned.add(info);
                        rank++;
                    } catch (Exception e) {
                        Log.e("TopFragment", "parse pinned post error", e);
                    }
                }
                topPinnedPosts = pinned;
                if (topFragment != null) {
                    final android.app.Activity activity = topFragment.getActivity();
                    if (activity != null) {
                        activity.runOnUiThread(() -> {
                            if (topView != null) {
                                BBSActivity act = (BBSActivity) topFragment.getActivity();
                                if (act != null) {
                                    realShowView(act, false);
                                }
                            }
                        });
                    }
                }
            } catch (Exception e) {
                Log.e("TopFragment", "fetchPinnedPosts error", e);
            }
        }).start();
    }

    public static void finishRequest(BBSActivity bbsActivity, String string) {
        tops = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(string);
            int len = jsonArray.length();
            for (int i = 0; i < len; i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                try {
                    tops.add(new ThreadInfo(i+1,
                            jsonObject.optString("bid"), Board.getNameById(jsonObject.optString("bid")),
                            jsonObject.optString("author"), jsonObject.optString("replyer"),
                            jsonObject.optString("time"),
                            jsonObject.optString("text"), jsonObject.getInt("tid"), jsonObject.getInt("pid")+1));
                } catch (Exception e) {
                    Log.e("TopFragment", "parse error", e);
                }
            }
        } catch (Exception e) {
            tops = new ArrayList<>();
        } finally {
            if (tops.size() == 0) {
                CustomToast.showInfoToast(bbsActivity, "暂时没有热门帖子，请刷新重试", 1500);
            }
            realShowView(bbsActivity, false);
        }
    }

    public static void realShowView(BBSActivity bbsActivity, boolean refresh) {
        if (tops.size() == 0 && refresh) {
            showView(bbsActivity);
            return;
        }

        // 合并置顶帖和热门帖
        final ArrayList<ThreadInfo> allPosts = new ArrayList<>();
        allPosts.addAll(topPinnedPosts);
        allPosts.addAll(tops);
        final int pinnedCount = topPinnedPosts.size();

        ListView listView = (ListView) topView.findViewById(R.id.bbs_top_listview);
        listView.setAdapter(new BaseAdapter() {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TopItemHolder holder;
                if (convertView == null) {
                    convertView = bbsActivity.getLayoutInflater()
                            .inflate(R.layout.bbs_top_listitem, parent, false);
                    holder = new TopItemHolder();
                    holder.rank = convertView.findViewById(R.id.bbs_top_item_rank);
                    holder.from = convertView.findViewById(R.id.bbs_top_item_from);
                    holder.title = convertView.findViewById(R.id.bbs_top_item_title);
                    holder.author = convertView.findViewById(R.id.bbs_top_item_author);
                    holder.time = convertView.findViewById(R.id.bbs_top_item_time);
                    convertView.setTag(holder);
                } else {
                    holder = (TopItemHolder) convertView.getTag();
                }

                ThreadInfo threadInfo = allPosts.get(position);
                boolean isPinned = position < pinnedCount;

                holder.from.setText(threadInfo.boardName);

                if (isPinned) {
                    holder.rank.setText("置顶");
                    holder.rank.setTextColor(PINNED_COLOR);
                    holder.title.setTextColor(PINNED_COLOR);
                } else {
                    holder.rank.setText("#" + threadInfo.rank);
                    holder.title.setTextColor(NORMAL_COLOR);
                }

                holder.title.setText(threadInfo.title);
                holder.author.setText(threadInfo.replyer);
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
                return allPosts.size();
            }
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {

            if (!Editor.getBoolean(bbsActivity, "hint_top", false)) {
                new AlertDialog.Builder(bbsActivity)
                        .setTitle("使用提示").setMessage("点击热门帖可查看帖子内容，长按热门帖可查看帖子列表。\n\n再次点击不再提示")
                        .setPositiveButton("知道了",null).show();
                Editor.putBoolean(bbsActivity, "hint_top", true);
                return;
            }

            try {
                ThreadInfo threadInfo = allPosts.get(position);
                Intent intent = new Intent(bbsActivity, ViewActivity.class);
                intent.putExtra("bid", threadInfo.board);
                intent.putExtra("tid", threadInfo.threadid + "");
                intent.putExtra("pid", threadInfo.pid);
                intent.putExtra("type", "thread");
                bbsActivity.startActivity(intent);
            } catch (Exception e) {
                Log.e("TopFragment", "click error", e);
                CustomToast.showErrorToast(bbsActivity, "打开失败", 1500);
            }
        });

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            ThreadInfo threadInfo = allPosts.get(position);
            Intent intent = new Intent(bbsActivity, ViewActivity.class);
            intent.putExtra("bid", threadInfo.board);
            intent.putExtra("tid", threadInfo.threadid + "");
            intent.putExtra("type", "thread");
            bbsActivity.startActivity(intent);
            return true;
        });

    }

}