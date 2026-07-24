# CAPUBBS ProGuard / R8 Rules

# === OkHttp (4.x) ===
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep class okio.** { *; }

# === JSON (内置 org.json) ===
-keep class org.json.** { *; }

# === Retrofit (如未来引入) ===
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

# === Gson ===
-dontwarn com.google.gson.**
-keep class com.google.gson.** { *; }
# 保留数据类字段名（Gson 反射用）
-keep class com.capubbs.update.** { *; }

# === AppCompat / AndroidX ===
-keep class androidx.appcompat.** { *; }
-keep class androidx.viewpager.** { *; }
-keep class androidx.legacy.** { *; }

# === 自定义类（避免反射/序列化使用时被混淆移除） ===
-keep class com.capubbs.lib.Constants { *; }
-keep class com.capubbs.lib.json.** { *; }
-keep class com.capubbs.lib.webconnection.** { *; }

# === WebView JavaScript 接口 ===
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# === 保持 Activity / Fragment 名称（AndroidManifest 引用） ===
-keep public class com.capubbs.*Activity { *; }
-keep public class com.capubbs.*Fragment { *; }

# === 第三方 SDK 旧字节码 StackMapTable 警告抑制 ===
# 这些库编译目标较老，R8 会报 "Expected stack map table for method with non-linear control flow"
# 用 -dontwarn 抑制，并用 -keep 保留类成员，防止未来 R8 误判为不可达

# 微信 SDK (libammsdk.jar) — 注意项目里用的是旧包名 com.tencent.mm.sdk
-dontwarn com.tencent.mm.sdk.**
-keep class com.tencent.mm.sdk.** { *; }
-dontwarn com.tencent.mm.opensdk.**
-keep class com.tencent.mm.opensdk.** { *; }

# AndroidX Media (media-1.0.0-runtime.jar)
-dontwarn androidx.media.**
-keep class androidx.media.** { *; }

# core-3.2.0.jar — 通常是 ZXing 扫码库，若你项目中不是，请按实际包名替换
-dontwarn com.google.zxing.**
-keep class com.google.zxing.** { *; }

# === 序列化相关 ===
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# === 避免 Service / BroadcastReceiver 被移除 ===
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
