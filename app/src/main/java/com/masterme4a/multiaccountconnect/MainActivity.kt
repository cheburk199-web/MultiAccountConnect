package com.masterme4a.multiaccountconnect

import android.app.AlertDialog
import android.content.ComponentName
import android.content.Intent
import android.content.pm.LauncherApps
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Process
import android.os.UserHandle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    private val bg = Color.rgb(13, 10, 23)
    private val panel = Color.rgb(29, 22, 43)
    private val panel2 = Color.rgb(39, 29, 57)
    private val purple = Color.rgb(157, 91, 255)
    private val muted = Color.rgb(174, 163, 194)
    private val white = Color.WHITE
    private val prefs by lazy { getSharedPreferences("multivm_profiles", MODE_PRIVATE) }
    private val launcherPackage = "git.artdeell.mjlaunch"
    private val names = MutableList<String?>(5) { null }
    private var selectedSlot = 0
    private lateinit var content: LinearLayout
    private lateinit var profileRows: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        for (i in 0..4) names[i] = prefs.getString("slot_${i + 1}", null)
        selectedSlot = prefs.getInt("selected_slot", 0).coerceIn(0, 4)
        buildShell()
        showProfiles()
    }

    private fun rounded(color: Int, radius: Float = 22f): GradientDrawable =
        GradientDrawable().apply { setColor(color); cornerRadius = radius }

    private fun label(text: String, size: Float, color: Int = white, bold: Boolean = false): TextView =
        TextView(this).apply {
            this.text = text
            textSize = size
            setTextColor(color)
            if (bold) setTypeface(typeface, Typeface.BOLD)
        }

    private fun button(text: String, primary: Boolean = true, action: () -> Unit): TextView =
        TextView(this).apply {
            this.text = text
            textSize = 14f
            gravity = Gravity.CENTER
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(white)
            setPadding(18, 14, 18, 14)
            background = rounded(if (primary) purple else panel2, 16f)
            isClickable = true
            isFocusable = true
            setOnClickListener { action() }
        }

    private fun buildShell() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
        }
        val scroll = ScrollView(this)
        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 22, 20, 24)
        }
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        val nav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(12, 10, 12, 14)
            setBackgroundColor(Color.rgb(22, 17, 33))
        }
        nav.addView(button("Профили", true) { showProfiles() }, LinearLayout.LayoutParams(0, -2, 1f))
        nav.addView(View(this), LinearLayout.LayoutParams(10, 1))
        nav.addView(button("Настройки", false) { showSettings() }, LinearLayout.LayoutParams(0, -2, 1f))
        root.addView(nav)
        setContentView(root)
    }

    private fun header(subtitle: String) {
        content.removeAllViews()
        val top = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val brand = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        brand.addView(label("MASTERME4A", 12f, purple, true))
        brand.addView(label("MultiVM", 30f, white, true))
        brand.addView(label(subtitle, 13f, muted))
        top.addView(brand, LinearLayout.LayoutParams(0, -2, 1f))
        top.addView(label("BETA", 11f, white, true).apply {
            setPadding(12, 8, 12, 8); background = rounded(Color.rgb(79, 48, 119), 30f)
        })
        content.addView(top)
        spacer(22)
    }

    private fun spacer(height: Int) { content.addView(View(this), LinearLayout.LayoutParams(1, height)) }
    private fun spacerIn(parent: LinearLayout, height: Int) { parent.addView(View(this), LinearLayout.LayoutParams(1, height)) }

    private fun card(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(16, 16, 16, 16)
        background = rounded(panel)
    }

    private fun showProfiles() {
        header("Быстрый выбор профиля")
        val summary = card()
        summary.addView(label("АКТИВНЫЙ СЛОТ", 11f, purple, true))
        spacerIn(summary, 7)
        summary.addView(label(names[selectedSlot] ?: "Профиль ${selectedSlot + 1} не настроен", 19f, white, true))
        spacerIn(summary, 5)
        summary.addView(label("Выбор здесь сохраняет никнейм и слот. Сам вход Microsoft/Ely.by меняется внутри MJLauncher.", 12f, muted))
        spacerIn(summary, 12)
        summary.addView(button("ЗАПУСТИТЬ ВЫБРАННЫЙ АККАУНТ", true) { launchSelected() })
        content.addView(summary)
        spacer(20)
        val env = card()
        val installed = try {
            @Suppress("DEPRECATION")
            packageManager.getApplicationInfo(launcherPackage, 0)
            true
        } catch (_: Exception) { false }
        val workInstalled = findWorkLauncher() != null
        env.addView(label("СОСТОЯНИЕ УСТРОЙСТВА", 11f, purple, true))
        spacerIn(env, 7)
        env.addView(label(if (installed) "Основной MJLauncher: найден" else "Основной MJLauncher: не найден", 14f, white, true))
        spacerIn(env, 4)
        env.addView(label(if (workInstalled) "Island: MJLauncher найден" else "Island: профиль или MJLauncher не обнаружен", 12f, if (workInstalled) purple else muted))
        content.addView(env)
        spacer(20)
        val title = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        title.addView(label("Пять аккаунтов", 18f, white, true), LinearLayout.LayoutParams(0, -2, 1f))
        title.addView(label("Слот ${selectedSlot + 1} выбран", 12f, muted))
        content.addView(title)
        spacer(12)
        profileRows = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        content.addView(profileRows)
        renderProfiles()
        spacer(16)
        content.addView(button("Как это работает", false) {
            AlertDialog.Builder(this)
                .setTitle("Переключение аккаунтов")
                .setMessage("MultiVM хранит три подписи/никнейма и запоминает выбранный слот. Он не получает пароли или токены и пока не может менять авторизованный аккаунт внутри MJLauncher автоматически. Выбери нужную учётную запись в самом MJLauncher. Чтобы не прерывать игру, не открывай лаунчер во время активной игровой сессии: Android может переключить приложение на передний план.")
                .setPositiveButton("Понятно", null).show()
        })
    }

    private fun renderProfiles() {
        profileRows.removeAllViews()
        for (slot in 0..4) {
            val name = names[slot]
            val active = slot == selectedSlot
            val item = card()
            val row = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
            row.addView(label("0${slot + 1}", 20f, purple, true).apply {
                gravity = Gravity.CENTER
                background = rounded(Color.rgb(54, 37, 78), 15f)
                setPadding(13, 12, 13, 12)
            })
            val info = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(12, 0, 0, 0) }
            info.addView(label(name ?: "Профиль ${slot + 1}", 15f, white, true))
            info.addView(label(if (active) "Выбран сейчас" else if (name == null) "Никнейм не задан" else "Сохранённый никнейм", 12f, if (active) purple else muted))
            row.addView(info, LinearLayout.LayoutParams(0, -2, 1f))
            item.addView(row)
            spacerIn(item, 12)
            val actions = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            actions.addView(button(if (active) "Выбран" else "Выбрать", !active) {
                selectedSlot = slot
                prefs.edit().putInt("selected_slot", selectedSlot).apply()
                showProfiles()
            }, LinearLayout.LayoutParams(0, -2, 1f))
            actions.addView(View(this), LinearLayout.LayoutParams(8, 1))
            actions.addView(button(if (name == null) "Добавить ник" else "Изменить", false) { editNickname(slot) },
                LinearLayout.LayoutParams(0, -2, 1f))
            item.addView(actions)
            profileRows.addView(item)
            if (slot < 4) spacer(10)
        }
    }

    private fun editNickname(slot: Int) {
        val input = EditText(this).apply {
            hint = "Например, Player123"
            setSingleLine(true)
            setText(names[slot] ?: "")
        }
        AlertDialog.Builder(this)
            .setTitle("Профиль ${slot + 1}")
            .setMessage("Сохраняется только подпись для удобства. Пароль и вход в аккаунт здесь не хранятся.")
            .setView(input)
            .setPositiveButton("Сохранить") { _, _ ->
                val value = input.text.toString().trim()
                if (value.isEmpty()) names[slot] = null
                else if (value.length <= 32) names[slot] = value
                prefs.edit().putString("slot_${slot + 1}", names[slot]).apply()
                showProfiles()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun launchSelected() {
        when (selectedSlot) {
            0 -> openLauncher()
            1 -> openWorkLauncher()
            else -> AlertDialog.Builder(this)
                .setTitle("Аккаунт ${selectedSlot + 1} пока не подключён")
                .setMessage("Слот и никнейм уже можно сохранить. Для запуска нужен отдельный профиль Android с установленным MJLauncher. Сейчас подтверждены только обычный профиль и Island.")
                .setPositiveButton("Понятно", null)
                .show()
        }
    }

    private fun openLauncher() {
        val intent = packageManager.getLaunchIntentForPackage(launcherPackage)
        if (intent == null) {
            AlertDialog.Builder(this).setTitle("MJLauncher не найден")
                .setMessage("Установи MJLauncher из Google Play и попробуй снова.")
                .setPositiveButton("OK", null).show()
            return
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(intent)
        } catch (_: Exception) {
            AlertDialog.Builder(this).setMessage("Не удалось открыть MJLauncher.")
                .setPositiveButton("OK", null).show()
        }
    }

    private fun findWorkLauncher(): Pair<UserHandle, ComponentName>? {
        return try {
            val launcherApps = getSystemService(LauncherApps::class.java)
            val currentUser = Process.myUserHandle()
            launcherApps.getProfiles()
                .firstOrNull { it != currentUser }
                ?.let { profile ->
                    launcherApps.getActivityList(launcherPackage, profile)
                        .firstOrNull()
                        ?.let { Pair(profile, it.componentName) }
                }
        } catch (_: Exception) {
            null
        }
    }

    private fun openWorkLauncher() {
        val target = findWorkLauncher()
        if (target == null) {
            AlertDialog.Builder(this)
                .setTitle("MJLauncher в Island не найден")
                .setMessage("Открой Island, перейди во вкладку ISLAND и убедись, что MJLauncher установлен и рабочий профиль включён. Затем вернись сюда.")
                .setPositiveButton("Понятно", null)
                .show()
            return
        }
        try {
            val launcherApps = getSystemService(LauncherApps::class.java)
            launcherApps.startMainActivity(target.second, target.first, null, null)
        } catch (_: Exception) {
            AlertDialog.Builder(this)
                .setTitle("Не удалось открыть второй профиль")
                .setMessage("Android не разрешил запуск через Island. Запусти второй MJLauncher напрямую из вкладки ISLAND приложения Island.")
                .setPositiveButton("Понятно", null)
                .show()
        }
    }

    private fun showSettings() {
        header("Настройки приложения")
        val appearance = card()
        appearance.addView(label("ВНЕШНИЙ ВИД", 11f, purple, true))
        spacerIn(appearance, 8)
        appearance.addView(label("Тёмная тема • Фиолетовые акценты • Masterme4a MultiVM", 14f))
        content.addView(appearance)
        spacer(14)
        val info = card()
        info.addView(label("УПРАВЛЕНИЕ АККАУНТАМИ", 11f, purple, true))
        spacerIn(info, 8)
        info.addView(label("Сохранённые никнеймы: ${names.count { it != null }} из 5", 14f))
        spacerIn(info, 6)
        info.addView(label("Выбран слот ${selectedSlot + 1}. Для реального переключения авторизованного аккаунта используй меню аккаунтов MJLauncher.", 12f, muted))
        content.addView(info)
        spacer(14)
        val about = card()
        about.addView(label("О ПРОЕКТЕ", 11f, purple, true))
        spacerIn(about, 8)
        about.addView(label("Интерфейс 0.5 • Пять сохраняемых слотов • Запоминание выбранного слота", 13f, white))
        spacerIn(about, 6)
        about.addView(label("Приложение не читает пароли, токены или приватные данные MJLauncher. Сохранение игры в памяти и подключение к серверу зависят от Android и самого Minecraft.", 12f, muted))
        content.addView(about)
    }
}
