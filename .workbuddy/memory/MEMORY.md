# CAPUBBS-Android-3.0 项目记忆

## 项目概况
- 北大车协 BBS Android 客户端，纯 Java 项目
- 2.0 原作者：维茨C (ckcz123)，3.0 由马Boy 升级适配
- 包名：`com.capubbs`，minSdk 21，targetSdk 33，versionName 3.0
- 签名密钥：`my-release-key`，配置在 `keystore.properties`

## 构建环境
- JDK: `D:\Android\Android Studio\jbr` (JDK 21)
- SDK: `D:\Android\SDK`
- Gradle wrapper jar 需通过 `java -classpath gradle/wrapper/gradle-wrapper.jar org.gradle.wrapper.GradleWrapperMain` 运行（gradlew 脚本有 PATH 问题）
- 编译命令：`export JAVA_HOME="/d/Android/Android Studio/jbr" && java -classpath gradle/wrapper/gradle-wrapper.jar org.gradle.wrapper.GradleWrapperMain assembleDebug --no-daemon`

## 代码结构
- 主包 `com.capubbs`：14个业务类（Activity/Fragment/Model）
- `lib/`：工具类（Util, Constants, Editor, MyFile 等）
- `lib/json/`：内置 JSON 库（原18个，清理后保留9个核心文件）
- `lib/view/`：自定义视图
- `lib/webconnection/`：网络层（WebConnection 基于 OkHttp）
- `lib/subactivity/`：子页面（图片查看、GIF、WebView）

## 已知问题（待整理）
- Util.java 630行过大，需拆分
- ViewActivity.java 451行职责过重
- ListView 适配器未使用 ViewHolder（性能问题）
- WebConnection 信任所有 SSL 证书（安全风险）
- Constants.github_repo 仍指向 2.0 仓库
