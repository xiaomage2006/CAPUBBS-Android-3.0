package com.capubbs.lib.subactivity;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView.ScaleType;

import com.capubbs.R;
import com.capubbs.lib.*;
import com.capubbs.lib.view.CustomToast;
import com.capubbs.lib.view.MyNotification;
import com.capubbs.lib.view.TouchImageView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Picture {
    SubActivity subActivity;
    String title;
    TouchImageView touchImageView;
    Bitmap bitmap = null;
    String filepath = null;

    public Picture(SubActivity s) {
        subActivity = s;
    }

    public Picture showPicture(Integer res, String _title) {
        init(_title);
        touchImageView.setImageResource(res);
        return this;
    }

    public Picture showPicture(String url) {
        File file = MyFile.getCache(subActivity, Util.getHash(url));
        if (MyFile.urlToFile(url, file)) {
            return showPicture(file.getAbsolutePath(), "查看图片");
        } else {
            CustomToast.showErrorToast(subActivity, "图片加载失败");
            subActivity.wantToExit();
            return this;
        }
    }

    public Picture showPicture(String filepath, String _title) {
        init(_title);
        this.filepath = filepath;
        bitmap = MyBitmapFactory.getCompressedBitmap(filepath, -1);
        touchImageView.setImageBitmap(bitmap);
        new Thread(() -> {
            if (bitmap != null) {
                subActivity.handler.sendMessage(Message.obtain(subActivity.handler,
                        Constants.MESSAGE_SUBACTIVITY_DECODE_PICTURE,
                        MyBitmapFactory.decodeQRCode(bitmap)));
            }
        }).start();
        return this;
    }

    void init(String _title) {
        title = _title;
        title = title.trim();
        if (title.endsWith(".jpg") || title.endsWith(".jpeg") || title.endsWith(".bmp")
                || title.endsWith(".png") || title.endsWith(".gif")) {
            title = title.substring(0, title.lastIndexOf("."));
        }
        if ("".equals(title)) title = "查看图片";
        subActivity.setTitle(title);
        subActivity.setContentView(R.layout.subactivity_imageview);
        touchImageView = (TouchImageView) subActivity.findViewById(R.id.subactivity_imageview);
        touchImageView.setScaleType(ScaleType.FIT_CENTER);
        touchImageView.setLongClickable(true);
        subActivity.registerForContextMenu(touchImageView);
    }

    public void savePicture() throws Exception {
        if (this.filepath == null || "".equals(this.filepath)) {
            CustomToast.showErrorToast(subActivity, "图片无法保存到本地");
            return;
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss", Locale.getDefault());
        String time = simpleDateFormat.format(new Date());
        File pictureDir = subActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File capubbsDir = new File(pictureDir, "capubbs");
        capubbsDir.mkdirs();
        String t = title;
        if (t.length() >= 15) t = t.substring(0, 13) + "...";
        String filepath = new File(capubbsDir, t + "__" + time + ".png").getAbsolutePath();
        if (MyFile.copyFile(this.filepath, filepath)) {
            if (android.os.Build.VERSION.SDK_INT < 16)
                CustomToast.showSuccessToast(subActivity, "图片已保存\n" + filepath, 3500);
            else {
                MyNotification.sendNotificationToOpenfile("图片已保存",
                        "图片已保存" + filepath, "图片已保存" + filepath, subActivity,
                        new File(filepath));
            }
        } else
            CustomToast.showErrorToast(subActivity, "保存失败");
		/*
		try {
			Bitmap bitmap=((BitmapDrawable)drawable).getBitmap();
			FileOutputStream fileOutputStream=new FileOutputStream(filepath);
			bitmap.compress(CompressFormat.PNG, 100, fileOutputStream);
			fileOutputStream.flush();
			fileOutputStream.close();
			CustomToast.showSuccessToast(subActivity, "ͼƬ������ "+filepath);
		}
		catch (Exception e) {e.printStackTrace();}
		*/
    }

    public void sharePicture() {
        if (bitmap == null) {
            CustomToast.showErrorToast(subActivity, "图片无法被分享");
            return;
        }
        Share.readyToShareImage(subActivity, "分享图片", bitmap);
        //CustomToast.showInfoToast(subActivity,"����������ʱ��δ���ߣ�");
    }

    public void decodePicture(final String string) {
        Log.w("qrcode", string);
        if (string.startsWith("http://")
                || string.startsWith("https://")) {
            if (string.startsWith("http://weixin.qq.com/")) {
                new AlertDialog.Builder(subActivity).setTitle("提示")
                        .setMessage("微信用户或微信群的二维码无法被正确识别，"
                                + "请使用微信客户端扫描微信二维码")
                        .setPositiveButton("确定", null).show();
                return;
            }
            Intent intent = new Intent(subActivity, SubActivity.class);
            intent.putExtra("type", Constants.SUBACTIVITY_TYPE_WEBVIEW);
            intent.putExtra("url", string);
            intent.putExtra("title", title);
            subActivity.startActivity(intent);
        } else {
            new AlertDialog.Builder(subActivity).setTitle("识别二维码")
                    .setMessage(string).setPositiveButton("确定", null).
                    setNegativeButton("复制", (dialog, which) -> {
                        ClipboardManager clipboardManager = (ClipboardManager) subActivity.getSystemService(Context.CLIPBOARD_SERVICE);
                        clipboardManager.setPrimaryClip(ClipData.newPlainText("text", string));
                        CustomToast.showSuccessToast(subActivity, "已复制到剪贴板！", 1500);
                    }).show();
        }
    }
}
