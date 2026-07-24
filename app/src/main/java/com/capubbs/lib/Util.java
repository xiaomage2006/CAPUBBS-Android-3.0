package com.capubbs.lib;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import android.webkit.WebView;

import java.security.MessageDigest;
import java.util.Random;

public class Util {

    @SuppressWarnings("deprecation")
    public static Bitmap captureWebView(WebView webView) {
        if (webView == null) return null;
        Picture snapShot = webView.capturePicture();
        Bitmap bmp = Bitmap.createBitmap(snapShot.getWidth(), snapShot.getHeight(), Bitmap.Config.ARGB_8888);
        Bitmap bg=null;
        try {
            Bitmap bgr = ((BitmapDrawable) webView.getBackground()).getBitmap();
            bg=Bitmap.createScaledBitmap(bgr, snapShot.getWidth(), snapShot.getHeight(), true);
        }
        catch (Exception | OutOfMemoryError e) {bg=null;}
        Canvas canvas = new Canvas(bmp);
        if (bg!=null)
            canvas.drawBitmap(bg,0,0,new Paint());
        snapShot.draw(canvas);
        return bmp;
    }

    /**
     * Compatible Html.fromHtml for all API levels
     */
    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String html) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        }
        return Html.fromHtml(html);
    }

    public static String getHash(String string) {
        return getHash(string, "");
    }

    public static String getHash(String string, String defaultString) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return bytes2Hex(md.digest(string.getBytes()));
        } catch (Exception e) {
            return defaultString;
        }
    }

    public static String md5(String string) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return bytes2Hex(md.digest(string.getBytes()));
        } catch (Exception e) {
            return "";
        }
    }

    private static String bytes2Hex(byte[] bts) {
        String des = "";
        for (int i = 0; i < bts.length; i++) {
            String tmp = (Integer.toHexString(bts[i] & 0xFF));
            if (tmp.length() == 1)
                des += "0";
            des += tmp;
        }
        return des;
    }

    public static int sp2px(Context context, int sp) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (sp * fontScale + 0.5f);
    }

    public static int px2sp(Context context, int px) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (px / fontScale + 0.5f);
    }

    public static int generateColorInt() {
        return Color.parseColor(generateColorString());
    }

    public static String generateColorString() {
        Random random = new Random();
        int[] x = new int[6];
        while (true) {
            for (int i=0;i<6;i++)
                x[i]=random.nextInt(16);
            int val=x[0]+x[2]+x[4];
            if (val>=27 && val<=40) break;
        }
        String string = "#";
        for (int i = 0; i < 6; i++)
            string += intToHex(x[i]);
        return string;
    }

    private static String intToHex(int i) {
        if (i <= 9) return i + "";
        if (i == 10) return "a";
        if (i == 11) return "b";
        if (i == 12) return "c";
        if (i == 13) return "d";
        if (i == 14) return "e";
        return "f";
    }

    public static String getValue(String url, String name) {
        int pos=url.lastIndexOf("?");
        int anchor=url.lastIndexOf("#");
        if (pos<0) return "";
        String string;
        if (anchor<=pos+1)
            string=url.substring(pos+1);
        else string=url.substring(pos+1, anchor-pos-1);
        if ("".equals(string)) return "";
        String[] strings=string.split("&");
        for (String str: strings) {
            String[] para=str.split("=");
            if (name.equals(para[0].trim()))
                return para[1].trim();
        }
        return "";
    }

    public static String cleanHtmlContent(String html) {
        if (html == null || html.isEmpty()) return "";
        
        String cleaned = html;
        
        cleaned = cleaned.replaceAll("<style[^>]*>[\\s\\S]*?</style>", "");
        cleaned = cleaned.replaceAll("<script[^>]*>[\\s\\S]*?</script>", "");
        cleaned = cleaned.replaceAll("<head[^>]*>[\\s\\S]*?</head>", "");
        cleaned = cleaned.replaceAll("<link[^>]*/?>", "");
        cleaned = cleaned.replaceAll("<meta[^>]*/?>", "");
        cleaned = cleaned.replaceAll("<title[^>]*>[\\s\\S]*?</title>", "");
        cleaned = cleaned.replaceAll("<!DOCTYPE[^>]*>", "");
        cleaned = cleaned.replaceAll("<html[^>]*>", "");
        cleaned = cleaned.replaceAll("</html>", "");
        cleaned = cleaned.replaceAll("<body[^>]*>", "");
        cleaned = cleaned.replaceAll("</body>", "");
        cleaned = cleaned.replaceAll("<!--[\\s\\S]*?-->", "");
        
        // Remove webpage structural elements that leak into post content
        cleaned = cleaned.replaceAll("<nav[^>]*>[\\s\\S]*?</nav>", "");
        cleaned = cleaned.replaceAll("<form[^>]*>[\\s\\S]*?</form>", "");
        cleaned = cleaned.replaceAll("<input[^>]*/?>", "");
        cleaned = cleaned.replaceAll("<button[^>]*>[\\s\\S]*?</button>", "");
        cleaned = cleaned.replaceAll("<button[^>]*/?>", "");
        cleaned = cleaned.replaceAll("<select[^>]*>[\\s\\S]*?</select>", "");
        cleaned = cleaned.replaceAll("<option[^>]*>[\\s\\S]*?</option>", "");
        cleaned = cleaned.replaceAll("<textarea[^>]*>[\\s\\S]*?</textarea>", "");
        cleaned = cleaned.replaceAll("<label[^>]*>[\\s\\S]*?</label>", "");
        cleaned = cleaned.replaceAll("<iframe[^>]*>[\\s\\S]*?</iframe>", "");
        cleaned = cleaned.replaceAll("<iframe[^>]*/?>", "");
        cleaned = cleaned.replaceAll("<header[^>]*>[\\s\\S]*?</header>", "");
        cleaned = cleaned.replaceAll("<footer[^>]*>[\\s\\S]*?</footer>", "");
        cleaned = cleaned.replaceAll("<aside[^>]*>[\\s\\S]*?</aside>", "");
        cleaned = cleaned.replaceAll("<\\?[^?]*\\?>", "");
        
        // 移除空段落 / 空 div（含 &nbsp;），避免 Word 用它们制造垂直空白。
        // 必须在 text-align 转 <center>/<right> 之前执行，否则空段落会先被转成 <center>&nbsp;</center>。
        cleaned = cleaned.replaceAll("<p[^>]*>\\s*</p>", "");
        cleaned = cleaned.replaceAll("<p[^>]*>\\s*&nbsp;\\s*</p>", "");
        cleaned = cleaned.replaceAll("<div[^>]*>\\s*</div>", "");
        cleaned = cleaned.replaceAll("<div[^>]*>\\s*&nbsp;\\s*</div>", "");
        
        java.util.regex.Pattern fontSizePattern = java.util.regex.Pattern.compile("<(\\w+)([^>]*)style=\"([^\"]*?)font-size\\s*:\\s*(\\d+)px([^\"]*?)\"([^>]*)>", java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher fontSizeMatcher = fontSizePattern.matcher(cleaned);
        StringBuffer sb = new StringBuffer();
        while (fontSizeMatcher.find()) {
            int pxSize = Integer.parseInt(fontSizeMatcher.group(4));
            int htmlSize = pxSize / 4;
            if (htmlSize < 1) htmlSize = 1;
            if (htmlSize > 7) htmlSize = 7;
            fontSizeMatcher.appendReplacement(sb, "<font" + fontSizeMatcher.group(2) + "size=\"" + htmlSize + "\"" + fontSizeMatcher.group(6) + ">");
        }
        cleaned = fontSizeMatcher.appendTail(sb).toString();
        
        fontSizePattern = java.util.regex.Pattern.compile("<(\\w+)([^>]*)style='([^']*?)font-size\\s*:\\s*(\\d+)px([^']*?)'([^>]*)>", java.util.regex.Pattern.CASE_INSENSITIVE);
        fontSizeMatcher = fontSizePattern.matcher(cleaned);
        sb = new StringBuffer();
        while (fontSizeMatcher.find()) {
            int pxSize = Integer.parseInt(fontSizeMatcher.group(4));
            int htmlSize = pxSize / 4;
            if (htmlSize < 1) htmlSize = 1;
            if (htmlSize > 7) htmlSize = 7;
            fontSizeMatcher.appendReplacement(sb, "<font" + fontSizeMatcher.group(2) + "size=\"" + htmlSize + "\"" + fontSizeMatcher.group(6) + ">");
        }
        cleaned = fontSizeMatcher.appendTail(sb).toString();
        
        // 居中对齐：p 标签，[\s\S]*? 惰性匹配防止跨标签
        java.util.regex.Pattern centerParaPattern = java.util.regex.Pattern.compile("<p([^>]*)style=\"[^\"]*text-align\\s*:\\s*center[^\"]*\"([^>]*)>([\\s\\S]*?)</p>", java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher centerParaMatcher = centerParaPattern.matcher(cleaned);
        sb = new StringBuffer();
        while (centerParaMatcher.find()) {
            centerParaMatcher.appendReplacement(sb, "<center>" + centerParaMatcher.group(3) + "</center>");
        }
        cleaned = centerParaMatcher.appendTail(sb).toString();
        
        centerParaPattern = java.util.regex.Pattern.compile("<p([^>]*)style='[^']*text-align\\s*:\\s*center[^']*'([^>]*)>([\\s\\S]*?)</p>", java.util.regex.Pattern.CASE_INSENSITIVE);
        centerParaMatcher = centerParaPattern.matcher(cleaned);
        sb = new StringBuffer();
        while (centerParaMatcher.find()) {
            centerParaMatcher.appendReplacement(sb, "<center>" + centerParaMatcher.group(3) + "</center>");
        }
        cleaned = centerParaMatcher.appendTail(sb).toString();
        
        // 居中对齐：div 标签，[\s\S]*? 惰性匹配防止跨标签
        java.util.regex.Pattern centerDivPattern = java.util.regex.Pattern.compile("<div([^>]*)style=\"[^\"]*text-align\\s*:\\s*center[^\"]*\"([^>]*)>([\\s\\S]*?)</div>", java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher centerDivMatcher = centerDivPattern.matcher(cleaned);
        sb = new StringBuffer();
        while (centerDivMatcher.find()) {
            centerDivMatcher.appendReplacement(sb, "<center>" + centerDivMatcher.group(3) + "</center>");
        }
        cleaned = centerDivMatcher.appendTail(sb).toString();
        
        centerDivPattern = java.util.regex.Pattern.compile("<div([^>]*)style='[^']*text-align\\s*:\\s*center[^']*'([^>]*)>([\\s\\S]*?)</div>", java.util.regex.Pattern.CASE_INSENSITIVE);
        centerDivMatcher = centerDivPattern.matcher(cleaned);
        sb = new StringBuffer();
        while (centerDivMatcher.find()) {
            centerDivMatcher.appendReplacement(sb, "<center>" + centerDivMatcher.group(3) + "</center>");
        }
        cleaned = centerDivMatcher.appendTail(sb).toString();
        
        // 右对齐：p 标签
        java.util.regex.Pattern rightParaPattern = java.util.regex.Pattern.compile("<p([^>]*)style=\"[^\"]*text-align\\s*:\\s*right[^\"]*\"([^>]*)>([\\s\\S]*?)</p>", java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher rightParaMatcher = rightParaPattern.matcher(cleaned);
        sb = new StringBuffer();
        while (rightParaMatcher.find()) {
            rightParaMatcher.appendReplacement(sb, "<right>" + rightParaMatcher.group(3) + "</right>");
        }
        cleaned = rightParaMatcher.appendTail(sb).toString();
        
        rightParaPattern = java.util.regex.Pattern.compile("<p([^>]*)style='[^']*text-align\\s*:\\s*right[^']*'([^>]*)>([\\s\\S]*?)</p>", java.util.regex.Pattern.CASE_INSENSITIVE);
        rightParaMatcher = rightParaPattern.matcher(cleaned);
        sb = new StringBuffer();
        while (rightParaMatcher.find()) {
            rightParaMatcher.appendReplacement(sb, "<right>" + rightParaMatcher.group(3) + "</right>");
        }
        cleaned = rightParaMatcher.appendTail(sb).toString();
        
        // 右对齐：div 标签
        java.util.regex.Pattern rightDivPattern = java.util.regex.Pattern.compile("<div([^>]*)style=\"[^\"]*text-align\\s*:\\s*right[^\"]*\"([^>]*)>([\\s\\S]*?)</div>", java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher rightDivMatcher = rightDivPattern.matcher(cleaned);
        sb = new StringBuffer();
        while (rightDivMatcher.find()) {
            rightDivMatcher.appendReplacement(sb, "<right>" + rightDivMatcher.group(3) + "</right>");
        }
        cleaned = rightDivMatcher.appendTail(sb).toString();
        
        rightDivPattern = java.util.regex.Pattern.compile("<div([^>]*)style='[^']*text-align\\s*:\\s*right[^']*'([^>]*)>([\\s\\S]*?)</div>", java.util.regex.Pattern.CASE_INSENSITIVE);
        rightDivMatcher = rightDivPattern.matcher(cleaned);
        sb = new StringBuffer();
        while (rightDivMatcher.find()) {
            rightDivMatcher.appendReplacement(sb, "<right>" + rightDivMatcher.group(3) + "</right>");
        }
        cleaned = rightDivMatcher.appendTail(sb).toString();
        
        // 移除所有内联 style/class/lang/dir/align 属性。
        // Word 粘贴会留下大量 mso-* CSS（如 mso-ascii-theme-font、mso-hansi-font-family），
        // 这些属性会被 Android Html.fromHtml 当成文本显示出来。
        // text-align:center/right 已在上一步转为 <center>/<right> 标签，此处安全移除剩余 style。
        cleaned = cleaned.replaceAll("\\s+style\\s*=\\s*\"[^\"]*\"", " ");
        cleaned = cleaned.replaceAll("\\s+style\\s*=\\s*'[^']*'", " ");
        cleaned = cleaned.replaceAll("\\s+class\\s*=\\s*\"[^\"]*\"", " ");
        cleaned = cleaned.replaceAll("\\s+class\\s*=\\s*'[^']*'", " ");
        cleaned = cleaned.replaceAll("\\s+(lang|dir|align)\\s*=\\s*\"[^\"]*\"", " ");
        cleaned = cleaned.replaceAll("\\s+(lang|dir|align)\\s*=\\s*'[^']*'", " ");
        
        cleaned = cleaned.replaceAll("<h1([^>]*)>", "<font size=\"7\"><b>");
        cleaned = cleaned.replaceAll("</h1>", "</b></font><br>");
        cleaned = cleaned.replaceAll("<h2([^>]*)>", "<font size=\"6\"><b>");
        cleaned = cleaned.replaceAll("</h2>", "</b></font><br>");
        cleaned = cleaned.replaceAll("<h3([^>]*)>", "<font size=\"5\"><b>");
        cleaned = cleaned.replaceAll("</h3>", "</b></font><br>");
        cleaned = cleaned.replaceAll("<h4([^>]*)>", "<font size=\"4\"><b>");
        cleaned = cleaned.replaceAll("</h4>", "</b></font><br>");
        cleaned = cleaned.replaceAll("<h5([^>]*)>", "<font size=\"3\"><b>");
        cleaned = cleaned.replaceAll("</h5>", "</b></font><br>");
        cleaned = cleaned.replaceAll("<h6([^>]*)>", "<font size=\"2\"><b>");
        cleaned = cleaned.replaceAll("</h6>", "</b></font><br>");
        
        // 归一化字号：移除所有 font size 标签本身，避免混合字号 AbsoluteSizeSpan 导致换行不一致。
        // h1-h6 的加粗效果由 <b> 标签保留，字号由 TextView 的 textSize 统一控制。
        cleaned = cleaned.replaceAll("</?font\\b[^>]*>", "");
        
        // <p> / <div> 作为段落分隔（居中的 p/div 已转为 <center>，不会到这里）
        // 段落开头不额外加 <br>，只在段落结尾加一个 <br>，这样相邻段落之间只有一个换行
        cleaned = cleaned.replaceAll("<p\\s*/?>", "");
        cleaned = cleaned.replaceAll("</p>", "<br>");
        cleaned = cleaned.replaceAll("<div\\s*/?>", "");
        cleaned = cleaned.replaceAll("</div>", "<br>");
        cleaned = cleaned.replaceAll("<span\\s*/?>", "");
        cleaned = cleaned.replaceAll("</span>", "");
        cleaned = cleaned.replaceAll("<tr\\s*/?>", "<br>");
        cleaned = cleaned.replaceAll("</tr>", "<br>");
        cleaned = cleaned.replaceAll("<td\\s*/?>", " ");
        cleaned = cleaned.replaceAll("</td>", " ");
        cleaned = cleaned.replaceAll("<th\\s*/?>", " ");
        cleaned = cleaned.replaceAll("</th>", " ");
        cleaned = cleaned.replaceAll("<li\\s*/?>", "<br>• ");
        cleaned = cleaned.replaceAll("</li>", "");
        cleaned = cleaned.replaceAll("<ul\\s*/?>", "<br>");
        cleaned = cleaned.replaceAll("</ul>", "<br>");
        cleaned = cleaned.replaceAll("<ol\\s*/?>", "<br>");
        cleaned = cleaned.replaceAll("</ol>", "<br>");
        cleaned = cleaned.replaceAll("<hr\\s*/?>", "<br><br>");
        
        // 移除居中对齐/右对齐转换后产生的空块（Word 常用 <p>&nbsp;</p> 做空行）
        cleaned = cleaned.replaceAll("<center>\\s*</center>", "");
        cleaned = cleaned.replaceAll("<center>\\s*&nbsp;\\s*</center>", "");
        cleaned = cleaned.replaceAll("<right>\\s*</right>", "");
        cleaned = cleaned.replaceAll("<right>\\s*&nbsp;\\s*</right>", "");
        
        // <center> / <right> 保留给 CenterTagHandler 处理对齐，
        // 在结束标签后加 <br> 保证每个对齐块独立成行
        cleaned = cleaned.replaceAll("</center>", "</center><br>");
        cleaned = cleaned.replaceAll("</right>", "</right><br>");
        
        // 移除 <img> 前后紧邻的 <br>，避免图片上下出现多余空白行。
        // 图片本身是块级 Drawable，额外的 <br> 会在其上/下方产生视觉空行。
        // 注意：不把 center/right 加入白名单，因为 Html.fromHtml(LEGACY) 模式下
        // 某些 Android 版本会因 <center>/<right> 抛异常，触发 ImageGetter 返回 null 的降级路径。
        cleaned = cleaned.replaceAll("<br>\\s*(<img\\b[^>]*>)", "$1");
        cleaned = cleaned.replaceAll("(<img\\b[^>]*>)\\s*<br>", "$1");
        cleaned = cleaned.replaceAll("(</img>)\\s*<br>", "$1");
        
        cleaned = cleaned.replaceAll("<(?!/?(a|img|br|b|i|strong|em|u|big|small|sup|sub)\\b)[^>]+>", "");
        
        // 清理被白名单过滤后残留的 </center> / </right>（它们不匹配上面的 tag 通配符，但它们的闭合标签需要通过正则移除）
        cleaned = cleaned.replaceAll("</?center>", "");
        cleaned = cleaned.replaceAll("</?right>", "");
        
        cleaned = cleaned.replaceAll("<br\\s*/?>", "<br>");
        cleaned = cleaned.replaceAll("<br>\\s*<br>", "<br><br>");
        
        while (cleaned.contains("<br><br><br>")) {
            cleaned = cleaned.replace("<br><br><br>", "<br><br>");
        }
        
        cleaned = cleaned.replace("&nbsp;", " ");
        cleaned = cleaned.replace("&amp;", "&");
        cleaned = cleaned.replace("&lt;", "<");
        cleaned = cleaned.replace("&gt;", ">");
        cleaned = cleaned.replace("&quot;", "\"");
        
        cleaned = cleaned.replaceAll("(?<!<br>)\\s{2,}(?!<br>)", " ");
        
        cleaned = cleaned.replaceAll("^<br>", "");
        cleaned = cleaned.replaceAll("<br>$", "");
        
        return cleaned.trim();
    }

    public static String stripHtmlTags(String html) {
        if (html == null || html.isEmpty()) return "";
        return html.replaceAll("<[^>]+>", "").trim();
    }

    /**
     * 移除 HTML 中混入的网页结构元素（导航栏、表单、页脚等），
     * 用于签名档等从网页端抓取的 HTML 内容清理。
     */
    public static String cleanWebpageStructure(String html) {
        if (html == null || html.isEmpty()) return "";
        String cleaned = html;
        cleaned = cleaned.replaceAll("<style[^>]*>[\\s\\S]*?</style>", "");
        cleaned = cleaned.replaceAll("<script[^>]*>[\\s\\S]*?</script>", "");
        cleaned = cleaned.replaceAll("<head[^>]*>[\\s\\S]*?</head>", "");
        cleaned = cleaned.replaceAll("<link[^>]*/?>", "");
        cleaned = cleaned.replaceAll("<meta[^>]*/?>", "");
        cleaned = cleaned.replaceAll("<title[^>]*>[\\s\\S]*?</title>", "");
        cleaned = cleaned.replaceAll("<!DOCTYPE[^>]*>", "");
        cleaned = cleaned.replaceAll("<html[^>]*>", "");
        cleaned = cleaned.replaceAll("</html>", "");
        cleaned = cleaned.replaceAll("<body[^>]*>", "");
        cleaned = cleaned.replaceAll("</body>", "");
        cleaned = cleaned.replaceAll("<!--[\\s\\S]*?-->", "");
        cleaned = cleaned.replaceAll("<nav[^>]*>[\\s\\S]*?</nav>", "");
        cleaned = cleaned.replaceAll("<form[^>]*>[\\s\\S]*?</form>", "");
        cleaned = cleaned.replaceAll("<input[^>]*/?>", "");
        cleaned = cleaned.replaceAll("<button[^>]*>[\\s\\S]*?</button>", "");
        cleaned = cleaned.replaceAll("<button[^>]*/?>", "");
        cleaned = cleaned.replaceAll("<select[^>]*>[\\s\\S]*?</select>", "");
        cleaned = cleaned.replaceAll("<option[^>]*>[\\s\\S]*?</option>", "");
        cleaned = cleaned.replaceAll("<textarea[^>]*>[\\s\\S]*?</textarea>", "");
        cleaned = cleaned.replaceAll("<label[^>]*>[\\s\\S]*?</label>", "");
        cleaned = cleaned.replaceAll("<iframe[^>]*>[\\s\\S]*?</iframe>", "");
        cleaned = cleaned.replaceAll("<iframe[^>]*/?>", "");
        cleaned = cleaned.replaceAll("<header[^>]*>[\\s\\S]*?</header>", "");
        cleaned = cleaned.replaceAll("<footer[^>]*>[\\s\\S]*?</footer>", "");
        cleaned = cleaned.replaceAll("<aside[^>]*>[\\s\\S]*?</aside>", "");
        cleaned = cleaned.replaceAll("<\\?[^?]*\\?>", "");
        
        // 移除登录/注册相关的链接和文本
        cleaned = cleaned.replaceAll("<a[^>]*href=\"[^\"]*(?:login|register|signup)[^\"]*\"[^>]*>[\\s\\S]*?</a>", "");
        cleaned = cleaned.replaceAll("<a[^>]*href='[^']*(?:login|register|signup)[^']*'[^>]*>[\\s\\S]*?</a>", "");
        cleaned = cleaned.replaceAll("用户登录", "");
        cleaned = cleaned.replaceAll("没有账号[，,]?立即注册[！!]?", "");
        cleaned = cleaned.replaceAll("忘记密码[？?]?", "");
        cleaned = cleaned.replaceAll("记住我", "");
        cleaned = cleaned.replaceAll("登\\s*录", "");
        cleaned = cleaned.replaceAll("注\\s*册", "");
        
        // 移除空的 div/p 标签
        cleaned = cleaned.replaceAll("<div[^>]*>\\s*</div>", "");
        cleaned = cleaned.replaceAll("<p[^>]*>\\s*</p>", "");
        cleaned = cleaned.replaceAll("<span[^>]*>\\s*</span>", "");
        
        return cleaned;
    }

    public static String extractSigUrl(String sig) {
        if (sig == null || sig.isEmpty()) return "";
        int start = sig.indexOf("$.get(\"");
        if (start == -1) start = sig.indexOf("$.get('");
        if (start == -1) return "";
        
        int quoteStart = start + 7;
        char quoteChar = sig.charAt(quoteStart - 1);
        int quoteEnd = sig.indexOf(quoteChar, quoteStart);
        if (quoteEnd == -1) return "";
        
        return sig.substring(quoteStart, quoteEnd);
    }

    /**
     * 判断签名 URL 是否为 API 格式（直接返回内容）
     */
    public static boolean isSigApiUrl(String sigUrl) {
        return sigUrl != null && sigUrl.contains("/api/");
    }

    /**
     * 从签名 JS 中提取楼层 ID，如 .find("#floor1") 提取出 "floor1"
     */
    public static String extractSigFloorId(String sig) {
        if (sig == null || sig.isEmpty()) return "";
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\.find\\(\"#(floor\\d+)\"\\)").matcher(sig);
        if (m.find()) return m.group(1);
        m = java.util.regex.Pattern.compile("\\.find\\('#(floor\\d+)'\\)").matcher(sig);
        if (m.find()) return m.group(1);
        return "";
    }

    /**
     * 从 HTML 中提取指定 ID 元素的内部 HTML（简化版，基于标签嵌套计数）
     */
    public static String extractElementById(String html, String id) {
        if (html == null || html.isEmpty() || id == null || id.isEmpty()) return null;
        
        String quotedId = java.util.regex.Pattern.quote(id);
        // 查找包含该 id 的开始标签，支持双引号和单引号
        java.util.regex.Pattern openPattern = java.util.regex.Pattern.compile(
            "<(\\w+)[^>]*\\bid=[\"']" + quotedId + "[\"'][^>]*>",
            java.util.regex.Pattern.CASE_INSENSITIVE
        );
        java.util.regex.Matcher openMatcher = openPattern.matcher(html);
        
        if (!openMatcher.find()) return null;
        
        String tagName = openMatcher.group(1).toLowerCase();
        int contentStart = openMatcher.end();
        
        // 通过标签嵌套计数找到匹配的关闭标签
        int depth = 1;
        int pos = contentStart;
        String openTag = "<" + tagName;
        String closeTag = "</" + tagName;
        
        while (depth > 0 && pos < html.length()) {
            int nextOpen = html.toLowerCase().indexOf(openTag, pos);
            int nextClose = html.toLowerCase().indexOf(closeTag, pos);
            
            if (nextClose == -1) break;
            
            // 检查找到的开标签是否完整（以 > 或空格结尾，避免匹配 <divx 这种情况）
            if (nextOpen != -1 && nextOpen < nextClose) {
                int afterOpen = nextOpen + openTag.length();
                if (afterOpen < html.length()) {
                    char c = html.charAt(afterOpen);
                    if (c == ' ' || c == '>' || c == '/' || c == '\t' || c == '\n' || c == '\r') {
                        depth++;
                        pos = nextOpen + 1;
                    } else {
                        // 不是真正的开标签，跳过
                        pos = nextOpen + 1;
                    }
                } else {
                    pos = nextOpen + 1;
                }
            } else {
                // 关闭标签在前
                int afterClose = nextClose + closeTag.length();
                if (afterClose < html.length() && html.charAt(afterClose) == '>') {
                    depth--;
                    if (depth == 0) {
                        return html.substring(contentStart, nextClose);
                    }
                }
                pos = nextClose + 1;
            }
        }
        
        // 如果找不到匹配的关闭标签，返回从内容开始到下一个同类关闭标签之间的内容
        int nextClose = html.toLowerCase().indexOf(closeTag, contentStart);
        if (nextClose != -1) {
            return html.substring(contentStart, nextClose);
        }
        
        return null;
    }

    /**
     * 将 Html.ImageGetter 回调中的相对图片路径补全为绝对 URL。
     * - "/" 开头 → base_url + source
     * - "../" 开头 → base_url + "/bbs" + source.substring(2)
     */
    public static String fixRelativeUrl(String source) {
        if (source == null) return null;
        if (source.startsWith("/"))
            return Constants.base_url + source;
        if (source.startsWith("../"))
            return Constants.base_url + "/bbs" + source.substring(2);
        return source;
    }

    /**
     * 将 HTML 内容中 src="../" 的相对图片路径补全为绝对 URL。
     * 用于签名档等从网页端抓取的 HTML 片段。
     */
    public static String fixRelativeImgSrc(String html) {
        if (html == null || html.isEmpty()) return html;
        String b = Constants.base_url + "/bbs/";
        return html.replaceAll("src=\"\\.\\./", "src=\"" + b)
                   .replaceAll("src='\\.\\./", "src='" + b);
    }

    public static String prepareHtmlContent(String html) {
        if (html == null || html.isEmpty()) return "";
        
        String prepared = html;
        
        prepared = prepared.replaceAll("<script[^>]*>[\\s\\S]*?</script>", "");
        prepared = prepared.replaceAll("<link[^>]*>", "");
        prepared = prepared.replaceAll("<meta[^>]*>", "");
        prepared = prepared.replaceAll("<head[^>]*>[\\s\\S]*?</head>", "");
        prepared = prepared.replaceAll("<title[^>]*>[\\s\\S]*?</title>", "");
        prepared = prepared.replaceAll("<html[^>]*>", "");
        prepared = prepared.replaceAll("</html>", "");
        prepared = prepared.replaceAll("<body[^>]*>", "");
        prepared = prepared.replaceAll("</body>", "");
        prepared = prepared.replaceAll("<!DOCTYPE[^>]*>", "");
        
        // 将相对路径补全为绝对路径，统一使用 https
        String b = Constants.base_url;
        // ../ → base_url/bbs/  (相对于 /bbs/content/ 等页面)
        prepared = prepared.replaceAll("(href|src)=(['\"])\\.\\./", "$1=$2" + b + "/bbs/");
        // / → base_url/  (根路径，同时覆盖 /bbs、/bbsimg 等)
        prepared = prepared.replaceAll("(href|src)=(['\"])/", "$1=$2" + b + "/");
        
        String css = "<style>" +
                "body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; font-size: 16px; line-height: 1.6; color: #333; padding: 0; margin: 0; word-wrap: break-word; }" +
                "p { margin: 0 0 10px 0; }" +
                "br { line-height: 1.5; }" +
                "a { color: #1a73e8; text-decoration: none; }" +
                "a:hover { text-decoration: underline; }" +
                "img { max-width: 100%; height: auto; display: block; margin: 5px 0; }" +
                "blockquote { border-left: 4px solid #ddd; padding-left: 12px; margin: 10px 0; color: #666; background: #f9f9f9; padding: 10px 15px; }" +
                "code { font-family: monospace; background: #f4f4f4; padding: 2px 6px; border-radius: 3px; font-size: 14px; }" +
                "pre { background: #f4f4f4; padding: 15px; border-radius: 5px; overflow-x: auto; font-family: monospace; font-size: 14px; }" +
                "table { border-collapse: collapse; width: 100%; margin: 10px 0; font-size: 14px; }" +
                "th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }" +
                "th { background: #f5f5f5; }" +
                "ul, ol { margin: 10px 0; padding-left: 25px; }" +
                "li { margin: 5px 0; }" +
                "strong, b { font-weight: 600; }" +
                "em, i { font-style: italic; }" +
                "h1, h2, h3, h4, h5, h6 { margin: 15px 0 10px 0; font-weight: 600; }" +
                "h1 { font-size: 24px; }" +
                "h2 { font-size: 20px; }" +
                "h3 { font-size: 18px; }" +
                "h4, h5, h6 { font-size: 16px; }" +
                ".quote { border-left: 4px solid #1a73e8; padding-left: 12px; margin: 10px 0; color: #555; background: #f0f7ff; padding: 10px 15px; }" +
                ".card { background: #fff; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); padding: 15px; margin: 10px 0; }" +
                "</style>";
        
        return css + "<div style='padding: 5px;'>" + prepared + "</div>";
    }

    /**
     * 语义版本号比较（支持 "3.0", "3.0.1", "v3.1", "3.1.0-beta" 等格式）。
     * @return 负数表示 v1 < v2，0 表示相等，正数表示 v1 > v2
     */
    public static int compareVersions(String v1, String v2) {
        if (v1 == null) v1 = "";
        if (v2 == null) v2 = "";
        v1 = v1.replaceAll("^[vV]", "").trim();
        v2 = v2.replaceAll("^[vV]", "").trim();
        String[] p1 = v1.split("[-+]", 2)[0].split("\\.");
        String[] p2 = v2.split("[-+]", 2)[0].split("\\.");
        int len = Math.max(p1.length, p2.length);
        for (int i = 0; i < len; i++) {
            int n1 = i < p1.length ? parseIntSafe(p1[i]) : 0;
            int n2 = i < p2.length ? parseIntSafe(p2[i]) : 0;
            if (n1 != n2) return n1 - n2;
        }
        return 0;
    }

    private static int parseIntSafe(String s) {
        try { return Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { return 0; }
    }

}