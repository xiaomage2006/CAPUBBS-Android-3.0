package com.capubbs.lib;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;

import com.capubbs.lib.subactivity.SubActivity;
import com.capubbs.lib.view.CustomToast;

import java.io.File;
import java.util.Locale;

@SuppressWarnings("deprecation")
public class ImageRequest extends AsyncTask<String, String, File> {
    ProgressDialog progressDialog;
    String title;
    String url;
    Context context;

    public static void showImage(Context context, String url) {
        showImage(context, url, "图片");
    }

    public static void showImage(Context context, String url, String title) {
        File file = MyFile.getCache(context, Util.getHash(url));
        if (!file.exists())
            new ImageRequest(context, url, title).execute("");
        else {
            Intent intent = new Intent(context, SubActivity.class);
            if (title.toLowerCase(Locale.getDefault()).endsWith(".gif")
                    || url.toLowerCase(Locale.getDefault()).endsWith(".gif"))
                intent.putExtra("type", Constants.SUBACTIVITY_TYPE_PICTURE_GIF);
            else
                intent.putExtra("type", Constants.SUBACTIVITY_TYPE_PICTURE_FILE);
            intent.putExtra("title", title.toLowerCase(Locale.getDefault()));
            intent.putExtra("file", file.getAbsolutePath());
            context.startActivity(intent);
        }
    }

    public ImageRequest(Context _context, String _url, String _title) {
        context = _context;
        title = _title;
        url = _url;
        progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setTitle("正在加载图片");
        progressDialog.setMessage("请稍候…");
        progressDialog.setIndeterminate(false);
        progressDialog.setCancelable(true);
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                cancel(true);
            }
        });
    }

    @Override
    protected void onPreExecute() {
        progressDialog.show();
    }

    @Override
    protected File doInBackground(String... params) {
        try {
            if (isCancelled()) return null;
            File file = MyFile.getCache(context, Util.getHash(url));
            if (file.exists()) return file;
            if (MyFile.urlToFile(url, file))
                return file;
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void onPostExecute(File file) {
        if (progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();
        if (file == null) {
            CustomToast.showErrorToast(context, "图片加载失败，请检查网络后重试");
            return;
        }
        showImage(context, url, title);
    }

    @Override
    protected void onCancelled() {
        if (progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();
    }

}
