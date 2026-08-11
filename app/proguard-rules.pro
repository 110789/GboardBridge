# Xposed 入口类与回调方法不能被混淆/裁剪,LSPosed 是按名字反射调用的。
-keep class io.github.a110789.gboardhooker.hook.HookEntry { *; }
-keep class * implements de.robv.android.xposed.IXposedHookLoadPackage { *; }
-keep class * implements de.robv.android.xposed.IXposedHookZygoteInit { *; }

# DexKit 的 JNI 回调按签名调用,整体保留。
-keep class org.luckypray.dexkit.** { *; }
-keepclassmembers class org.luckypray.dexkit.** { native <methods>; }

# 本模块自己的 hook 逻辑大量依赖反射按「形状」认类/成员,
# 混淆器可能改变字段/方法的可见性判断,统一保留。
-keep class io.github.a110789.gboardhooker.hook.gboard.** { *; }

-dontwarn org.luckypray.dexkit.**

# BiBi IME Bridge 自己要求保留的类（合并自它原来的 proguard-rules.pro）。
# ImeBridgeHook 已经被上面 "implements IXposedHookLoadPackage" 那条通配规则
# 覆盖了，这里列出来只是保持跟它原始规则文件一致，不算多余。
-keep class com.brycewg.asrkb.imebridge.ImeBridgeHook { *; }
-keep class com.brycewg.asrkb.imebridge.BridgeVisualPrefsProvider { *; }
-keep class com.brycewg.asrkb.imebridge.BridgeVisualPrefs { *; }
-keep class com.brycewg.asrkb.imebridge.BridgeVisualPrefs$VisualConfig { *; }
-keep class com.brycewg.asrkb.imebridge.BridgeContract { *; }
