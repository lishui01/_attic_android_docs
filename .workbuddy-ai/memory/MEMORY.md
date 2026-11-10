# 项目长期记忆（MudZJ / 武学大道 Android 客户端）

## 构建（重要）
- AGP 7.4.1 拒绝非 ASCII 项目路径，必须在纯 ASCII 路径下构建。标准入口：`C:\android-build\build.bat <gradle任务>`（自带 JDK `C:\android-build\jdkhome`、SDK `C:\android-build\sdk`）。
- `C:\mudbuild` 是指向本工作区的 junction（2026-11-05 重建过；此前指向 Downloads 旧路径已失效）。PowerShell 工具不能显式调用 `cmd /c`，可直接 `& '.\gradlew.bat'` 并先设置 JAVA_HOME/ANDROID_HOME。
- 产物在 `app/build/outputs/apk/{debug,release}/`；release 启用 R8 + shrinkResources + 内嵌签名 `app/keystore/zjapp.jks`（密码写在 app/build.gradle，勿外泄），约 1.1MB。覆盖安装必须沿用同一签名。

## 架构要点
- 纯 Java 8，27 个源文件约 8500 行；唯一依赖 androidx.appcompat:1.0.0；minSdk 24 / compileSdk 34。
- 线路编码 UTF-8（已从 gb2312 改造）；zj 协议为 ESC + 3 位码，字段分隔符 `$zj#`；JCrypt 是 DES-crypt 变体，用于登录握手；ZJKEY 在 mudsocketd。
- 存在两套连接层：`mudsocketd`（老，Application 全局状态 + 主 socket）与 `GmSession`（新，GM 专用，含 telnet 协商状态机）。
- `mudmaind.java` 是 3600+ 行主 Activity，主线程 mHandler 承担全部协议解析与 View 构建；聊天列表用 LinearLayout 堆 TextView（上限 500），是主要卡顿来源。
- 结论记录：全量 Rust 重写不划算；若要 Rust，宜只做协议/加密 core（JCrypt + GmSession）经 JNI/UniFFI 供 Java UI 调用，并注意 R8 需为 JNI 加 keep 规则。
