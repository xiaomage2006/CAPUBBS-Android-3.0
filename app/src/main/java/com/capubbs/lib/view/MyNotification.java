package com.capubbs.lib.view;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import com.capubbs.R;

import java.io.File;
import java.util.Locale;
import java.util.Random;

public class MyNotification {

    private static final String CHANNEL_ID = "capubbs_notification";
    private static final String CHANNEL_NAME = "CAPUBBS通知";
    private static final String CHANNEL_DESCRIPTION = "CAPUBBS应用通知";

    private static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private static int getPendingIntentFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        }
        return PendingIntent.FLAG_UPDATE_CURRENT;
    }

    public static void sendNotification(String title,
                                        String content, String ticker, Context context, String type, Class<?> cls) {
        if (Build.VERSION.SDK_INT < 16) return;

        createNotificationChannel(context);

        Notification.Builder builder = new Notification.Builder(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID);
        }
        builder.setAutoCancel(true);
        builder.setSmallIcon(R.drawable.logo2);
        builder.setContentTitle(title);
        builder.setContentText(content);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder.setTicker(ticker);
        }

        Intent resultIntent = new Intent(context, cls);
        if (type != null && !"".equals(type)) {
            resultIntent.setAction(context.getPackageName() + ".action." + type);
            resultIntent.putExtra("type", type);
        }
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                new Random().nextInt(),
                resultIntent,
                getPendingIntentFlags()
        );
        builder.setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(new Random().nextInt(), builder.build());
    }

    public static void sendNotificationToOpenfile(String title,
                                                  String content, String ticker, Context context, File file) {
        if (Build.VERSION.SDK_INT < 16) return;
        if (file == null || !file.exists()) return;

        createNotificationChannel(context);

        Notification.Builder builder = new Notification.Builder(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID);
        }
        builder.setAutoCancel(true);
        builder.setSmallIcon(R.drawable.logo2);
        builder.setContentTitle(title);
        builder.setContentText(content);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder.setTicker(ticker);
        }

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        String type = getMIMEType(file);

        Uri fileUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            fileUri = Uri.parse("file://" + file.getAbsolutePath());
        } else {
            fileUri = Uri.fromFile(file);
        }

        intent.setDataAndType(fileUri, type);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                new Random().nextInt(),
                intent,
                getPendingIntentFlags()
        );
        builder.setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = builder.build();
        notificationManager.notify(new Random().nextInt(), notification);
    }

    private static String getMIMEType(File file) {
        String[][] MIME_TABLE = {
                {".3gp", "video/3gpp"}, {".apk", "application/vnd.android.package-archive"},
                {".asf", "video/x-ms-asf"}, {".avi", "video/x-msvideo"},
                {".bmp", "image/bmp"}, {".txt", "text/plain"}, {".jpg", "image/jpeg"},
                {".jpeg", "image/jpeg"}, {".jpz", "image/jpeg"},
                {".doc", "application/msword"}, {".docx", "application/msword"},
                {".png", "image/png"}, {".gif", "image/gif"},
                {".html", "text/html"}, {".htm", "text/html"}, {".ico", "image/x-icon"},
                {".js", "application/x-javascript"}, {".mp3", "audio/x-mpeg"},
                {".mov", "video/quicktime"}, {".mpg", "video/x-mpeg"}, {".mp4", "video/mp4"},
                {".pdf", "application/pdf"}, {".ppt", "application/vnd.ms-powerpoint"},
                {".swf", "application/x-shockwave-flash"}, {".wav", "audio/x-wav"},
                {".zip", "application/zip"}, {".rar", "application/x-rar-compressed"},
                {".rm", "audio/x-pn-realaudio"}, {".rmvb", "audio/x-pn-realaudio"}
        };
        String type = "*/*";
        String end = getSuffix(file);
        if (end.equals("")) return type;
        for (int i = 0; i < MIME_TABLE.length; i++) {
            if (end.equals(MIME_TABLE[i][0])) type = MIME_TABLE[i][1];
        }
        return type;
    }

    private static String getSuffix(File file) {
        String fname = file.getName();
        int doIndex = fname.lastIndexOf(".");
        if (doIndex < 0) return "";
        return fname.substring(doIndex, fname.length()).toLowerCase(Locale.getDefault());
    }

}