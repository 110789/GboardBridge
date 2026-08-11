package io.github.a110789.gboardhooker.ui

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.color.DynamicColors
import com.google.android.material.materialswitch.MaterialSwitch
import io.github.a110789.gboardhooker.R
import io.github.a110789.gboardhooker.hook.PrefKeys

/**
 * 设置界面——剪贴板三个功能：条数、有效期、字数上限。
 *
 * 读/写同一份 [SharedPreferences]（文件名 [PrefKeys.FILE]），hook 那边通过
 * [de.robv.android.xposed.XSharedPreferences] 读同一个文件。
 *
 * 预设按钮和按键滑动都已经整个移除了，现在就是三个开关 + 三个输入框，
 * 改完手动点保存才会写盘。
 */
class SettingsActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences

    private lateinit var switchCount: MaterialSwitch
    private lateinit var switchTtl: MaterialSwitch
    private lateinit var switchChars: MaterialSwitch

    private lateinit var inputMaxItems: EditText
    private lateinit var inputTtlMinutes: EditText
    private lateinit var inputCharLimit: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        // Material You 动态取色：Android 12+ 跟系统壁纸自动换色，跟 BiBi 那边注入
        // Gboard 键盘时用的是同一套系统机制；低版本或者厂商 ROM 没有这个能力时，
        // 这个调用什么都不做，自动回退到 themes.xml 里定义的中性色。必须在
        // super.onCreate() 之前调，Material 官方文档明确要求这个顺序。
        DynamicColors.applyToActivityIfAvailable(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        prefs = openModulePrefs()

        switchCount = findViewById(R.id.switchCount)
        switchTtl = findViewById(R.id.switchTtl)
        switchChars = findViewById(R.id.switchChars)

        inputMaxItems = findViewById(R.id.inputMaxItems)
        inputTtlMinutes = findViewById(R.id.inputTtlMinutes)
        inputCharLimit = findViewById(R.id.inputCharLimit)

        loadFromPrefs()

        findViewById<android.view.View>(R.id.saveButton).setOnClickListener { saveToPrefs() }

        // 只留一个桌面图标，BiBi 那套设置从这里跳过去——同一个 App 内部启动，
        // 那边 Activity 在 manifest 里已经改成 exported="false"。
        //
        // 用完整类名字符串指定组件，而不是 `XxxActivity::class.java`——后者会让
        // Kotlin 编译器在编译期直接引用那个 Java 类，跟同一个 sourceSet 里刚合并
        // 进来的 Java 源码撞上了编译顺序问题（compileDebugKotlin 先于
        // compileDebugJavaWithJavac 跑，报 "Unresolved reference: brycewg"）。
        // 字符串方式在运行时才解析，编译期两边完全不用互相知道对方存在。
        findViewById<android.view.View>(R.id.openBibiSettings).setOnClickListener {
            val intent = android.content.Intent()
            intent.setClassName(
                packageName,
                "com.brycewg.asrkb.imebridge.BridgeVisualSettingsActivity",
            )
            startActivity(intent)
        }

        // 每次改完设置界面反反复复被问"是不是装了新的"——这里直接把当前手机上
        // 真实安装的版本号打印出来，不用再靠肉眼比对颜色/布局细节：
        // 版本号跟上次的对不上，就说明确实是新包；对得上，就说明没装成功，
        // 而不是代码没生效。
        showInstalledVersion()
    }

    private fun showInstalledVersion() {
        val text = runCatching {
            val info = packageManager.getPackageInfo(packageName, 0)
            val code = if (android.os.Build.VERSION.SDK_INT >= 28) {
                info.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                info.versionCode.toLong()
            }
            "${info.versionName} (build $code)"
        }.getOrDefault("版本号读取失败")
        findViewById<android.widget.TextView>(R.id.versionText).text = text
    }

    /**
     * 用 `MODE_WORLD_READABLE` 打开这份设置文件。
     *
     * 这一步不是可选项——LSPosed 只在模块用这个 mode 打开 `SharedPreferences` 时，
     * 才会把它标记成 hook 那边（[de.robv.android.xposed.XSharedPreferences]）能读的
     * （见 LSPosed Wiki「New XSharedPreferences」），前提是 manifest 里
     * `xposedminversion` 至少是 93（本模块已经是）。用 `MODE_PRIVATE` 的话，界面上
     * 看着保存成功了，hook 那边其实永远读不到真实值，只能读到默认值。
     *
     * 普通 Android 上 `MODE_WORLD_READABLE` 从 API 24 起直接抛
     * `SecurityException`，只有被 LSPosed 接管、且上面条件都满足时才放行，
     * 所以这里必须 try/catch 兜底——不然脱离 LSPosed 环境（或者作用域还没配置好）
     * 打开这个界面就会直接崩溃。
     */
    private fun openModulePrefs(): SharedPreferences = try {
        @Suppress("DEPRECATION")
        getSharedPreferences(PrefKeys.FILE, Context.MODE_WORLD_READABLE)
    } catch (e: SecurityException) {
        getSharedPreferences(PrefKeys.FILE, Context.MODE_PRIVATE)
    }

    private fun loadFromPrefs() {
        switchCount.isChecked = prefs.getBoolean(PrefKeys.FEATURE_COUNT, true)
        switchTtl.isChecked = prefs.getBoolean(PrefKeys.FEATURE_TTL, true)
        switchChars.isChecked = prefs.getBoolean(PrefKeys.FEATURE_CHARS, true)

        inputMaxItems.setText(prefs.getInt(PrefKeys.MAX_ITEMS, PrefKeys.DEFAULT_MAX_ITEMS).toString())
        inputTtlMinutes.setText(prefs.getInt(PrefKeys.TTL_MINUTES, PrefKeys.DEFAULT_TTL_MINUTES).toString())
        inputCharLimit.setText(prefs.getInt(PrefKeys.CHAR_LIMIT, PrefKeys.DEFAULT_CHAR_LIMIT).toString())
    }

    private fun saveToPrefs() {
        prefs.edit()
            .putBoolean(PrefKeys.FEATURE_COUNT, switchCount.isChecked)
            .putBoolean(PrefKeys.FEATURE_TTL, switchTtl.isChecked)
            .putBoolean(PrefKeys.FEATURE_CHARS, switchChars.isChecked)
            .putInt(PrefKeys.MAX_ITEMS, intOf(inputMaxItems, PrefKeys.DEFAULT_MAX_ITEMS).coerceIn(1, 200))
            .putInt(
                PrefKeys.TTL_MINUTES,
                intOf(inputTtlMinutes, PrefKeys.DEFAULT_TTL_MINUTES).coerceIn(0, PrefKeys.MAX_TTL_MINUTES),
            )
            .putInt(PrefKeys.CHAR_LIMIT, intOf(inputCharLimit, PrefKeys.DEFAULT_CHAR_LIMIT).coerceIn(100, 2_000_000))
            .apply()

        Toast.makeText(this, R.string.hint_saved, Toast.LENGTH_LONG).show()
    }

    private fun intOf(view: EditText, default: Int): Int =
        view.text?.toString()?.trim()?.toIntOrNull() ?: default
}
