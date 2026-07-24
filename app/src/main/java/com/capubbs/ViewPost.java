package com.capubbs;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Message;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;

import androidx.appcompat.app.AlertDialog.Builder;

import com.capubbs.lib.CenterTagHandler;
import com.capubbs.lib.Constants;
import com.capubbs.lib.Editor;
import com.capubbs.lib.MyBitmapFactory;
import com.capubbs.lib.MyCalendar;
import com.capubbs.lib.MyFile;
import com.capubbs.lib.RequestingTask;
import com.capubbs.lib.Share;
import com.capubbs.lib.Util;
import com.capubbs.lib.ViewSetting;
import com.capubbs.lib.XML2Json;
import com.capubbs.lib.view.CustomToast;
import com.capubbs.lib.webconnection.Parameters;

import com.capubbs.lib.json.JSONArray;
import com.capubbs.lib.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

public class ViewPost {
    ArrayList<PostInfo> postInfos = new ArrayList<>();
    int page;
    int tmpPage;
    int totalPage;
    String tmpThreadid;
    String title;
    int index;
    String tmpBoard;
    int selectNum;
    int selection = 0;

    ViewActivity viewActivity;
    ListView listViewRef;
    BaseAdapter adapterRef;
    // 待删除的 pid（在发送删除请求时设置，删除成功后使用）
    int pendingDeletePid = -1;
    public ViewPost(ViewActivity activity) {viewActivity=activity;}

    public void getPosts(String threadid, int page) {
        ArrayList<Parameters> arrayList = new ArrayList<>();
        arrayList.add(new Parameters("ask", "show"));
        arrayList.add(new Parameters("p", page + ""));
        arrayList.add(new Parameters("bid", viewActivity.board));
        arrayList.add(new Parameters("tid", threadid));
        arrayList.add(new Parameters("token", Userinfo.token));
        new RequestingTask(viewActivity, "正在获取帖子...",
                Constants.bbs_url, Constants.REQUEST_BBS_GET_POST)
                .execute(arrayList);
        tmpPage = page;
        tmpThreadid = threadid;
    }

    // 发起删除请求，使用与网页端相同的接口 /bbs/delete/
    // 网页端参数：ask=delpid, bid, tid, pid，认证通过 Cookie 中的 token
    public void requestDelete(int pid, int fid, String bid, String tid) {
        pendingDeletePid = pid;
        String deleteUrl = Constants.base_url + "/bbs/delete/";
        // 手动将 token 注入 Cookie 存储，确保删除请求携带认证信息
        if (Userinfo.token != null && !Userinfo.token.isEmpty()) {
            com.capubbs.lib.webconnection.Cookies.setCookieManually(deleteUrl, "token", Userinfo.token);
        }
        ArrayList<Parameters> arrayList = new ArrayList<>();
        arrayList.add(new Parameters("ask", "delpid"));
        arrayList.add(new Parameters("bid", bid));
        arrayList.add(new Parameters("tid", tid));
        arrayList.add(new Parameters("pid", pid + ""));
        android.util.Log.w("ViewPost", "requestDelete: url=" + deleteUrl + " bid=" + bid + " tid=" + tid + " pid=" + pid + " token=" + Userinfo.token);
        new RequestingTask(viewActivity, "正在删除...", deleteUrl, Constants.REQUEST_BBS_DELETE).execute(arrayList);
    }

    // 处理删除返回
    void finishDelete(String string) {
        try {
            android.util.Log.w("ViewPost", "finishDelete response: " + string);
            // 新接口 /bbs/delete/ 返回极简响应（Content-Length=1），200即表示成功
            // 兼容旧接口 client.php 的 JSON 格式响应
            if (string == null || string.isEmpty() || "1".equals(string.trim())) {
                // 删除成功：立即在本地移除该楼层
                int delPid = pendingDeletePid;
                pendingDeletePid = -1;
                for (int i = 0; i < postInfos.size(); i++) {
                    if (postInfos.get(i).pid == delPid) {
                        postInfos.remove(i);
                        break;
                    }
                }
                if (adapterRef != null) {
                    adapterRef.notifyDataSetChanged();
                }
                CustomToast.showSuccessToast(viewActivity, "删除成功");
                return;
            }
            // 尝试按 JSON 格式解析（兼容旧接口）
            JSONArray jsonArray = new JSONArray(string);
            if (jsonArray.length() == 0) {
                CustomToast.showErrorToast(viewActivity, "删除失败：响应数据异常");
                return;
            }
            int code = jsonArray.getJSONObject(0).getInt("code");
            if (code != 0) {
                String msg = jsonArray.getJSONObject(0).optString("msg", XML2Json.getErrorMessage(code));
                CustomToast.showErrorToast(viewActivity, msg);
                android.util.Log.w("ViewPost", "delete failed code=" + code + " msg=" + msg + " raw=" + string);
                return;
            }
            // JSON 格式删除成功
            int delPid = pendingDeletePid;
            pendingDeletePid = -1;
            for (int i = 0; i < postInfos.size(); i++) {
                if (postInfos.get(i).pid == delPid) {
                    postInfos.remove(i);
                    break;
                }
            }
            if (adapterRef != null) {
                adapterRef.notifyDataSetChanged();
            }
            CustomToast.showSuccessToast(viewActivity, "删除成功");
        } catch (Exception e) {
            // 响应不是 JSON 也不是空，但 HTTP 200 也视为成功
            android.util.Log.w("ViewPost", "finishDelete non-JSON response, treating as success, raw=" + string);
            int delPid = pendingDeletePid;
            pendingDeletePid = -1;
            for (int i = 0; i < postInfos.size(); i++) {
                if (postInfos.get(i).pid == delPid) {
                    postInfos.remove(i);
                    break;
                }
            }
            if (adapterRef != null) {
                adapterRef.notifyDataSetChanged();
            }
            CustomToast.showSuccessToast(viewActivity, "删除成功");
        }
    }

    // 本类通过 requestDelete -> finishDelete 在服务器确认后进行本地删除与调整

    void finishRequest(String string) {
        try {
            JSONArray jsonArray=new JSONArray(string);
            int code = jsonArray.getJSONObject(0).getInt("code");
            if (code != 0 && code != -1) {
                if (code==1 && tmpPage>1) {
                    getPosts(tmpThreadid, tmpPage-1);
                    return;
                }
                CustomToast.showErrorToast(viewActivity,
                        jsonArray.getJSONObject(0).optString("msg", XML2Json.getErrorMessage(code)));
                viewActivity.setContentView(R.layout.bbs_thread_listview);
                viewActivity.showingPage = ViewActivity.PAGE_THREAD;
                return;
            }
            int tmpNum = selectNum;
            selectNum = 0;
            selection = 0;
            totalPage = jsonArray.getJSONObject(0).getInt("pages");
            title = Util.stripHtmlTags(jsonArray.getJSONObject(0).optString("title"));
            postInfos.clear();
            int len = jsonArray.length();
            for (int i = 0; i < len; i++) {
                JSONObject post = jsonArray.getJSONObject(i);
                PostInfo postInfo = new PostInfo(post.optString("author"), post.getInt("floor"),
                        post.getInt("fid"), post.optString("time"), post.optInt("lzl"), post.optString("text"), post.optString("sig"));
                postInfos.add(postInfo);
                int number = post.getInt("floor");
                if (tmpNum == number)
                    selection = i;
            }
            page = tmpPage;
            viewActivity.threadid = tmpThreadid;
            viewPosts();
        } catch (Exception e) {
            postInfos.clear();
            CustomToast.showErrorToast(viewActivity, "获取失败");
        }
    }

    public void viewPosts() {
        viewActivity.setContentView(R.layout.bbs_post_listview);
        viewActivity.showingPage = ViewActivity.PAGE_POST;
        viewActivity.invalidateOptionsMenu();
        viewActivity.setTitle("(" + page + "/" + totalPage + ") " + title);
        listViewRef = (ListView) viewActivity.findViewById(R.id.bbs_post_listview);
        adapterRef = new BaseAdapter() {

            @SuppressLint("ViewHolder")
            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                convertView = viewActivity.getLayoutInflater().inflate(R.layout.bbs_post_item,
                        parent, false);
                final PostInfo postInfo = postInfos.get(position);

                String cleanedContent = Util.cleanHtmlContent(postInfo.content);

                try {
                    ViewSetting.setTextView(convertView, R.id.bbs_post_item_text,
                            Html.fromHtml(cleanedContent, Html.FROM_HTML_MODE_LEGACY, source -> {
                                if (source == null) return null;
                                source = Util.fixRelativeUrl(source);

                                if (!source.startsWith(Constants.base_url + "/bbsimg/expr")
                                        && !source.matches("https://chexie\\.net/bbsimg/[\\d]+\\.gif")
                                        && !postInfos.get(position).imgs.contains(source))
                                    postInfos.get(position).imgs.add(source);

                                final File file = MyFile.getCache(viewActivity, Util.getHash(source));
                                if (file.exists()) {
                                    Bitmap bitmap = MyBitmapFactory.getCompressedBitmap(file.getAbsolutePath(), 2.5);
                                    if (bitmap != null) {
                                        Drawable drawable = new BitmapDrawable(viewActivity.getResources(), bitmap);
                                        int width = drawable.getIntrinsicWidth(), height = drawable.getIntrinsicHeight();
                                        width *= 1.8;
                                        height *= 1.8;
                                        if (width > ViewActivity.listWidth) {
                                            height = (ViewActivity.listWidth * height) / width;
                                            width = ViewActivity.listWidth;
                                        }
                                        drawable.setBounds(0, 0, width, height);
                                        return drawable;
                                    }
                                }
                                if (Editor.getBoolean(viewActivity, "picture", true)) {
                                    final String url = source;
                                    new Thread(() -> {
                                        if (MyFile.urlToFile(url, file))
                                            viewActivity.handler.sendMessage(
                                                    Message.obtain(viewActivity.handler, Constants.MESSAGE_IMAGE_REQUEST_FINISHED, postInfo.pid, 0));
                                    }).start();
                                }
                                return null;
                            }, new CenterTagHandler()));
                } catch (Exception e) {
                    // Html.fromHtml 在某些 HTML 结构下会抛出 PARAGRAPH span 异常
                    // 尝试更激进的清理：移除所有 block 标签，保留 inline 格式
                    android.util.Log.w("ViewPost", "Html.fromHtml failed, trying aggressive cleanup", e);
                    try {
                        String aggressiveClean = cleanedContent
                                .replaceAll("</?(p|div|center|right|h[1-6]|table|tr|td|th|ul|ol|li|blockquote)[^>]*>", "<br>")
                                .replaceAll("<br>\\s*<br>", "<br><br>");
                        ViewSetting.setTextView(convertView, R.id.bbs_post_item_text,
                                Html.fromHtml(aggressiveClean, Html.FROM_HTML_MODE_LEGACY,
                                        source -> {
                                            if (source == null) return null;
                                            source = Util.fixRelativeUrl(source);
                                            // 降级路径也正确加载图片，不再返回 null
                                            final File file = MyFile.getCache(viewActivity, Util.getHash(source));
                                            if (file.exists()) {
                                                Bitmap bitmap = MyBitmapFactory.getCompressedBitmap(file.getAbsolutePath(), 2.5);
                                                if (bitmap != null) {
                                                    Drawable drawable = new BitmapDrawable(viewActivity.getResources(), bitmap);
                                                    int width = drawable.getIntrinsicWidth(), height = drawable.getIntrinsicHeight();
                                                    width *= 1.8; height *= 1.8;
                                                    if (width > ViewActivity.listWidth) {
                                                        height = (ViewActivity.listWidth * height) / width;
                                                        width = ViewActivity.listWidth;
                                                    }
                                                    drawable.setBounds(0, 0, width, height);
                                                    return drawable;
                                                }
                                            }
                                            return null;
                                        }, null));
                    } catch (Exception e2) {
                        // 最终降级为纯文本
                        ViewSetting.setTextView(convertView, R.id.bbs_post_item_text,
                                Util.stripHtmlTags(cleanedContent));
                    }
                }
                
                android.webkit.WebView sigWebView = convertView.findViewById(R.id.bbs_post_item_sig);
                sigWebView.setVisibility(View.GONE);

                ViewSetting.setTextView(convertView, R.id.bbs_post_item_floor, postInfo.pid + "");
                ViewSetting.setTextView(convertView, R.id.bbs_post_item_author, postInfo.author);
                ViewSetting.setTextView(convertView, R.id.bbs_post_item_time, MyCalendar.format(postInfo.timestamp));
                ViewSetting.setTextView(convertView, R.id.bbs_post_item_lzl, "["+postInfo.lzl+"]");
                convertView.setTag(position);
                return convertView;
            }

            @Override
            public long getItemId(int position) { return 0; }

            @Override
            public Object getItem(int position) { return null; }

            @Override
            public int getCount() { return postInfos.size(); }
        };
        listViewRef.setAdapter(adapterRef);
        listViewRef.setOnItemLongClickListener((parent, view, position, id) -> {
            index = position;
            return false;
        });
        listViewRef.setOnItemClickListener((parent, view, position, id) -> {
            if (!Editor.getBoolean(viewActivity, "hint_post", false)) {
                new Builder(viewActivity)
                        .setTitle("使用提示").setMessage("点击帖子可显示楼层、楼层数、作者、时间、楼中楼和查看帖子菜单\n长按帖子可显示楼层、楼层数、作者、时间、楼中楼和菜单\n\n再次点击不再提示")
                        .setPositiveButton("知道了",null).show();
                Editor.putBoolean(viewActivity, "hint_post", true);
                return;
            }
            index = position;
            PostInfo postInfo=postInfos.get(position);
            if (postInfo.lzl!=0) {
                Intent intent=new Intent(viewActivity, LZLActivity.class);
                intent.putExtra("fid", postInfo.fid);
                intent.putExtra("bid", viewActivity.board);
                intent.putExtra("tid", tmpThreadid);
                viewActivity.startActivityForResult(intent,4);
                return;
            }
            listViewRef.showContextMenu();
        });
        listViewRef.setSelection(selection);
        selection = 0;
        viewActivity.registerForContextMenu(listViewRef);
    }

    public void jump() {
        if (totalPage <= 0 || viewActivity.threadid == null || viewActivity.threadid.isEmpty()) {
            CustomToast.showErrorToast(viewActivity, "数据未加载完成");
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
                        getPosts(viewActivity.threadid, targetPage);
                    }
                } catch (Exception e) {
                    CustomToast.showErrorToast(viewActivity, "跳转失败");
                }
            });
            builder.show();
        } catch (Exception e) {
            CustomToast.showErrorToast(viewActivity, "跳转失败");
        }
    }

    public void share() {
        String url=Constants.base_url+"/bbs/content/?bid="+viewActivity.board+"&tid="+tmpThreadid;
        Share.readyToShareURL(viewActivity, "分享", url, title, "来自CAPUBBS (Android "+Constants.version+")", null);
    }
}