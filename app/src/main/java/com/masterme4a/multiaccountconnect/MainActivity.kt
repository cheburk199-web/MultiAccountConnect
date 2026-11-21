package com.masterme4a.multiaccountconnect

import android.app.AlertDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    private val bg = Color.rgb(13, 10, 23)
    private val panel = Color.rgb(29, 22, 43)
    private val panel2 = Color.rgb(39, 29, 57)
    private val purple = Color.rgb(157, 91, 255)
    private val muted = Color.rgb(174, 163, 194)
    private val white = Color.WHITE
    private val names = mutableListOf<String>()
    private lateinit var content: LinearLayout
    private lateinit var profileRows: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        names.addAll(getSharedPreferences("account_names", MODE_PRIVATE)
            .getStringSet("names", emptySet())?.toList()?.sorted() ?: emptyList())
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
        val gap = View(this)
        nav.addView(gap, LinearLayout.LayoutParams(10, 1))
        nav.addView(button("Настройки", false) { showSettings() }, LinearLayout.LayoutParams(0, -2, 1f))
        root.addView(nav)
        setContentView(root)
    }

    private fun header(subtitle: String) {
        content.removeAllViews()
        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val brand = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        brand.addView(label("MASTERME4A", 12f, purple, true))
        brand.addView(label("MultiVM", 30f, white, true))
        brand.addView(label(subtitle, 13f, muted))
        top.addView(brand, LinearLayout.LayoutParams(0, -2, 1f))
        val badge = label("BETA", 11f, white, true).apply {
            setPadding(12, 8, 12, 8)
            background = rounded(Color.rgb(79, 48, 119), 30f)
        }
        top.addView(badge)
        content.addView(top)
        spacer(22)
    }

    private fun spacer(height: Int) {
        content.addView(View(this), LinearLayout.LayoutParams(1, height))
    }

    private fun sectionTitle(title: String, trailing: String? = null) {
        val row = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        row.addView(label(title, 18f, white, true), LinearLayout.LayoutParams(0, -2, 1f))
        if (trailing != null) row.addView(label(trailing, 12f, muted))
        content.addView(row)
        spacer(12)
    }

    private fun card(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(16, 16, 16, 16)
        background = rounded(panel)
    }

    private fun showProfiles() {
        header("Три слота профилей Minecraft")
        val summary = card()
        summary.addView(label("ТВОИ ПРОФИЛИ", 11f, purple, true))
        spacerIn(summary, 6)
        summary.addView(label("Управляй никами и запускай MJLauncher.", 16f, white, true))
        spacerIn(summary, 5)
        summary.addView(label("Пока это отдельные карточки, а не изолированные копии лаунчера.", 12f, muted))
        content.addView(summary)
        spacer(22)
        sectionTitle("Профили", "3 слота")
        profileRows = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        content.addView(profileRows)
        renderProfiles()
        spacer(16)
        content.addView(button("+ Добавить никнейм", true) { addNickname() })
        spacer(18)
        val note = card()
        note.addView(label("ВАЖНО О ЗАПУСКЕ", 11f, purple, true))
        spacerIn(note, 7)
        note.addView(label("Android пока открывает установленный MJLauncher как одно приложение. Кнопка запуска не создаёт отдельную копию и не разделяет данные аккаунтов.", 13f, muted))
        content.addView(note)
    }

    private fun spacerIn(parent: LinearLayout, height: Int) {
        parent.addView(View(this), LinearLayout.LayoutParams(1, height))
    }

    private fun renderProfiles() {
        profileRows.removeAllViews()
        for (slot in 1..3) {
            val name = names.getOrNull(slot - 1)
            val item = card()
            val row = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
            val number = label("0$slot", 20f, purple, true).apply {
                gravity = Gravity.CENTER
                background = rounded(Color.rgb(54, 37, 78), 15f)
                setPadding(13, 12, 13, 12)
            }
            row.addView(number)
            val info = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(12, 0, 0, 0)
            }
            info.addView(label(name ?: "Профиль не настроен", 15f, white, true))
            info.addView(label(if (name == null) "Добавь никнейм в этот слот" else "Сохранённый никнейм", 12f, muted))
            row.addView(info, LinearLayout.LayoutParams(0, -2, 1f))
            item.addView(row)
            spacerIn(item, 14)
            item.addView(button(if (name == null) "Добавить никнейм" else "Открыть MJLauncher", true) {
                if (name == null) addNickname() else openLauncher(slot)
            })
            profileRows.addView(item)
            if (slot != 3) spacer(12)
        }
    }

    private fun addNickname() {
        val input = EditText(this).apply {
            hint = "Например, Player123"
            setSingleLine(true)
        }
        AlertDialog.Builder(this)
            .setTitle("Добавить профиль")
            .setMessage("Никнейм будет сохранён в карточке приложения.")
            .setView(input)
            .setPositiveButton("Сохранить") { _, _ ->
                val value = input.text.toString().trim()
                if (value.isNotEmpty() && value.length <= 32 && !names.contains(value)) {
                    if (names.size < 3) {
                        names.add(value)
                        saveNames()
                        showProfiles()
                    } else {
                        AlertDialog.Builder(this).setMessage("Все три слота заняты. Удали никнейм в настройках, чтобы заменить его.")
                            .setPositiveButton("OK", null).show()
                    }
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun saveNames() {
        getSharedPreferences("account_names", MODE_PRIVATE).edit()
            .putStringSet("names", names.toSet()).apply()
    }

    private fun openLauncher(slot: Int) {
        val intent = packageManager.getLaunchIntentForPackage("git.artdeell.mjlaunch")
        if (intent == null) {
            AlertDialog.Builder(this).setTitle("MJLauncher не найден")
                .setMessage("Установи MJLauncher из Google Play и попробуй снова.")
                .setPositiveButton("OK", null).show()
            return
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        try {
            startActivity(intent)
        } catch (_: Exception) {
            AlertDialog.Builder(this).setMessage("Не удалось открыть MJLauncher.")
                .setPositiveButton("OK", null).show()
        }
    }

    private fun showSettings() {
        header("Настройки приложения")
        val about = card()
        about.addView(label("ВНЕШНИЙ ВИД", 11f, purple, true))
        spacerIn(about, 8)
        about.addView(label("Тёмная тема • Фиолетовые акценты • Masterme4a MultiVM", 14f))
        content.addView(about)
        spacer(14)
        val accounts = card()
        accounts.addView(label("УПРАВЛЕНИЕ ПРОФИЛЯМИ", 11f, purple, true))
        spacerIn(accounts, 8)
        if (names.isEmpty()) {
            accounts.addView(label("Сохранённых никнеймов пока нет.", 13f, muted))
        } else {
            names.toList().forEach { name ->
                val row = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
                row.addView(label(name, 14f), LinearLayout.LayoutParams(0, -2, 1f))
                row.addView(button("Удалить", false) {
                    names.remove(name)
                    saveNames()
                    showSettings()
                })
                accounts.addView(row)
                spacerIn(accounts, 6)
            }
        }
        content.addView(accounts)
        spacer(14)
        val info = card()
        info.addView(label("О ПРОЕКТЕ", 11f, purple, true))
        spacerIn(info, 8)
        info.addView(label("Версия интерфейса 0.3 • Android", 13f, white))
        spacerIn(info, 6)
        info.addView(label("Функция настоящей изоляции данных MJLauncher находится в разработке и ещё не подтверждена на этом устройстве.", 12f, muted))
        content.addView(info)
    }
}
