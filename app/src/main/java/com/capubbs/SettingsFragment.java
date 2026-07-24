package com.capubbs;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.capubbs.lib.Constants;
import com.capubbs.lib.Editor;
import com.capubbs.lib.MyFile;
import com.capubbs.lib.Share;
import com.capubbs.lib.Util;
import com.capubbs.lib.ViewSetting;
import com.capubbs.lib.subactivity.SubActivity;
import com.capubbs.lib.view.BadgeView;
import com.capubbs.lib.view.CustomToast;
import com.capubbs.update.UpdateChecker;

import java.io.File;

public class SettingsFragment extends Fragment {
    static View settingsView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.setting_view, container, false);
        settingsView = rootView;
        set((BBSActivity) getActivity());
        setOthers((BBSActivity) getActivity());
        return rootView;
    }

    public static void set(BBSActivity bbsActivity) {
        if (settingsView == null) return;
        try {
            if ("".equals(Userinfo.token)) {
                ViewSetting.setTextView(settingsView, R.id.settings_username, "点击登录...");
                ViewSetting.setOnClickListener(settingsView, R.id.settings_username, v -> Userinfo.showLoginView(bbsActivity));
            } else {
                ViewSetting.setTextView(settingsView, R.id.settings_username, Userinfo.username);
                ViewSetting.setOnClickListener(settingsView, R.id.settings_username, v ->
                        new AlertDialog.Builder(bbsActivity)
                                .setTitle("确认注销登录").setMessage("确定要注销吗")
                                .setPositiveButton("确定", (dialog, which) -> {Userinfo.logout(bbsActivity);set(bbsActivity);})
                                .setNegativeButton("取消",null).show());
            }


        } catch (Exception e) {
            Log.e("SettingsFragment", "set error", e);
        }
    }

    public static void setOthers(BBSActivity context) {
        if (settingsView == null) return;
        ViewSetting.setOnClickListener(settingsView, R.id.settings_favorite, v ->
                context.startActivity(new Intent(context, FavoriteActivity.class)));
        ViewSetting.setSwitchChecked(settingsView, R.id.settings_switch_autologin, Editor.getBoolean(context, "autologin", true));
        ViewSetting.setSwitchOnCheckChangeListener(settingsView, R.id.settings_switch_autologin, (buttonView, isChecked) -> {
            Editor.putBoolean(context, "autologin", isChecked);
        });
        ViewSetting.setSwitchChecked(settingsView, R.id.settings_switch_picture, Editor.getBoolean(context, "picture", true));
        ViewSetting.setSwitchOnCheckChangeListener(settingsView, R.id.settings_switch_picture, (buttonView, isChecked) -> {
            Editor.putBoolean(context, "picture", isChecked);
        });
        ViewSetting.setOnClickListener(settingsView, R.id.settings_clearcache, v -> clearcache(context));
        ViewSetting.setOnClickListener(settingsView, R.id.settings_faq, v -> {
            Intent intent=new Intent(context, ViewActivity.class);
            intent.putExtra("type","thread");
            intent.putExtra("bid","28");
            intent.putExtra("tid","28");
            context.startActivity(intent);
        });
        ViewSetting.setOnClickListener(settingsView, R.id.settings_report, v -> {
            Intent intent=new Intent(context, ViewActivity.class);
            intent.putExtra("type","thread");
            intent.putExtra("bid","28");
            intent.putExtra("tid","30");
            context.startActivity(intent);
        });
        ViewSetting.setOnClickListener(settingsView, R.id.settings_about, v -> {
            Intent intent=new Intent(context, SubActivity.class);
            intent.putExtra("type", Constants.SUBACTIVITY_TYPE_ABOUT);
            context.startActivity(intent);
        });
        ViewSetting.setOnClickListener(settingsView, R.id.settings_source, v-> {
            String html = "<html><meta charset='utf8'><script>window." +
                    "setTimeout(\"location.href='https://github.com/" + Constants.github_repo + "'\", 3000);</script>" +
                    context.getResources().getString(R.string.settings_source_hint)+"</html>";
            Intent intent=new Intent(context, SubActivity.class);
            intent.putExtra("type",Constants.SUBACTIVITY_TYPE_WEBVIEW_HTML);
            intent.putExtra("html",html);
            intent.putExtra("title","获取源代码");
            context.startActivity(intent);
        });
        ViewSetting.setOnClickListener(settingsView, R.id.settings_share, v -> Share.readyToShareURL(context,
                "分享CAPUBBS", Constants.updateURL, "欢迎使用CAPUBBS Android "+Constants.updateVersion,
                "新版Android CAPUBBS，欢迎下载使用~", null));
        // 若已有新版本信息（由 checkUpdate 异步设置），立即显示红点徽标
        if (!"".equals(Constants.updateVersion) && Util.compareVersions(Constants.version, Constants.updateVersion) < 0) {
            ViewSetting.setTextView(settingsView, R.id.settings_update, "有新版本       ");
            BadgeView.show(context, settingsView.findViewById(R.id.settings_update), "new");
        }

        ViewSetting.setOnClickListener(settingsView, R.id.settings_update, v -> {
            if ("".equals(Constants.updateVersion) || Util.compareVersions(Constants.version, Constants.updateVersion) >= 0)
                CustomToast.showInfoToast(context, "已是最新版本！");
            else {
                new AlertDialog.Builder(context).setTitle("发现新版本 " + Constants.updateVersion + "，下载？")
                        .setMessage(Constants.updateMsg).setPositiveButton("下载", (dialog, which) -> {
                    String url = Constants.updateURL;
                    // 非 APK 链接（如 GitHub Release 页面）直接用浏览器打开
                    if (url == null || url.isEmpty() || !url.toLowerCase().endsWith(".apk")) {
                        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                        return;
                    }
                    Uri uri = Uri.parse(url);
                    try {
                        DownloadManager.Request request = new DownloadManager.Request(uri);
                        request.setTitle("正在下载CAPUBBS...");
                        File file = MyFile.getFile(context, null, "CAPUBBS.apk");
                        if (file.exists()) file.delete();
                        request.setDestinationUri(Uri.fromFile(file));
                        request.setDescription("文件保存在" + file.getAbsolutePath());
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        request.setMimeType("application/vnd.android.package-archive");
                        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) {
                            request.allowScanningByMediaScanner();
                        }
                        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                        downloadManager.enqueue(request);
                        CustomToast.showInfoToast(context, "文件下载中，稍后通知栏查看下载结果");
                    }
                    catch (Exception e) {
                        Log.e("SettingsFragment", "download error", e);
                        context.startActivity(new Intent(Intent.ACTION_VIEW, uri));
                    }
                }).setNegativeButton("取消", null).show();
            }
        });
    }

    public static void clearcache(Context context) {
        try {
            File file = MyFile.getCache(context, null);
            String msg = "清理目录\n" + file.getAbsolutePath() + "/\n\n文件数目" + MyFile.getFileCount(file) + "\n文件大小" + MyFile.getFileSizeString(file);
            new AlertDialog.Builder(context).setTitle("清除缓存").setMessage(msg)
                    .setPositiveButton("确定", (dialog, which) -> {
                        MyFile.clearCache(context);
                        CustomToast.showSuccessToast(context, "清理成功！");
                    }).setNegativeButton("取消", null).show();
        }
        catch (Exception e) {Log.e("SettingsFragment", "clearcache error", e);}
    }

    /**
     * 检查新版本（Kotlin UpdateChecker 实现）。
     * 优先 BBS API，兜底 GitHub Release API。
     * 发现新版本时在底部设置 Tab 和设置页同时显示红点 "new" 徽标。
     */
    public static void checkUpdate(BBSActivity bbsActivity) {
        UpdateChecker.checkForUpdate(info -> {
            if (info == null) return;

            // 写入 Constants 供 setOthers() / 点击下载使用
            Constants.updateVersion = info.getVersion();
            Constants.updateURL = info.getDownloadUrl();
            Constants.updateMsg = info.getReleaseNotes();

            // 底部 Tab 红点
            BadgeView.show(bbsActivity, bbsActivity.findViewById(R.id.bbs_bottom_img_settings), "new");
            // 设置页红点（若 Fragment 已创建）
            if (settingsView != null) {
                ViewSetting.setTextView(settingsView, R.id.settings_update, "有新版本       ");
                BadgeView.show(bbsActivity, settingsView.findViewById(R.id.settings_update), "new");
            }
        });
    }

}
