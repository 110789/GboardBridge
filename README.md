# GboardBridge

合并了两个独立 LSPosed 模块的功能，打包成一个 APK：

- **GboardHooker**（本仓库原有部分）—— 解开 Gboard 剪贴板的条数/有效期/字数限制
- **BiBi IME Bridge**（合并自 [BryceWG/bibi-keyboard-lsposed-bridge](https://github.com/BryceWG/bibi-keyboard-lsposed-bridge)）—— 把 BiBi 语音输入 App 识别出的文字桥接进 Gboard，附带流式预览、录音、剪贴板同步

两边的 hook 逻辑完全独立，各自的入口类都在 `assets/xposed_init` 里注册：

```
io.github.a110789.gboardhooker.hook.HookEntry
com.brycewg.asrkb.imebridge.ImeBridgeHook
```

## 用法

1. 装上 APK，LSPosed 管理器里给它勾选 Gboard 的作用域
2. 完整重启手机
3. 桌面上会出现**两个**图标——"GboardHooker"（剪贴板设置）和"Bridge module settings"（BiBi 桥接设置），分别配置各自的功能
4. BiBi 语音识别文字要真正回填进 Gboard，还需要在"说点啥"（BiBi 主 App）里把桥接目标设成 Gboard，具体见 BiBi 原仓库的说明

## 这是怎么合并的

BiBi 那部分是把 [原仓库](https://github.com/BryceWG/bibi-keyboard-lsposed-bridge) `app/src/main/java/com/brycewg/asrkb/imebridge/` 下的 30 个 Java 文件原样搬进来的，包名不变（`com.brycewg.asrkb.imebridge`，跟本模块的 Kotlin 代码是两棵独立的包树，互不干扰）。改动只有几处、都是因为两个项目合并成一个 APK 之后**共用同一个 applicationId**（`io.github.a110789.gboardhooker`）带来的：

- `BridgeVisualSettingsActivity.java` / `BridgePcmSessionClient.java` / `BridgeUserNotifier.java`：加了 `import io.github.a110789.gboardhooker.R;`——这三个文件原来靠"同包默认可见"访问自己项目的 `R` 类，合并后只有一个全局 `R`（属于本模块的 namespace），得显式 import
- `BridgeUserNotifier.java` 的 `MODULE_PACKAGE` 常量：从 `"com.brycewg.asrkb.imebridge"` 改成 `"io.github.a110789.gboardhooker"`——这个常量原本是用来 `createPackageContext()` 找**自己**这个包已安装在哪，取它自己的字符串资源；合并后 `com.brycewg.asrkb.imebridge` 不再是一个独立安装的包，这个查找会直接失败，导致所有提示 Toast 静默不显示
- `ImeBridgeHook.java` 里防止 hook 自己进程的判断，同样的包名问题，同样改法
- `AndroidManifest.xml`：BiBi 原来的 `.BridgeVisualSettingsActivity` 这种相对于自己包名的简写类名，改成完整类名 `com.brycewg.asrkb.imebridge.BridgeVisualSettingsActivity`；`xposedminversion`/`xposedsharedprefs` 这类模块级 meta-data 两边各有一份，合并时取了更严格的那个
- `strings.xml`：BiBi 的 `app_name` 改名成 `bibi_app_name`，避免跟本模块自己的 `app_name` 撞名；其余字符串/颜色资源都带 `bridge_`/`feature_bridge_` 前缀，没有冲突

BiBi 那边的 `app/src/test` 单元测试目录没有搬过来（不影响功能，只是它自己的开发期测试）。

## 已知限制

跟原来的 GboardHooker 一样，这套合并没有在真机上跑过完整编译验证（当前环境没有 Android SDK），建议先跑一次 CI 构建确认没有编译期问题，再装到手机上测试两边的功能是否都正常。BiBi 那部分的功能验证尤其要重点看：桥接是否真的收得到语音识别文字、录音波形是否显示、悬浮提示 Toast 是否正常弹出（这几个恰好是合并时改动过的地方）。
