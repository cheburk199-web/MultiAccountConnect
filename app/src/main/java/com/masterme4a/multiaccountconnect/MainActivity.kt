package com.masterme4a.multiaccountconnect

import android.app.AlertDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.documentfile.provider.DocumentFile
import java.io.File
import java.net.URL
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {
    private val purple = Color.rgb(145, 72, 220)
    private val dark = Color.rgb(24, 15, 39)
    private val accounts = mutableListOf<String>()
    private var activeAccount: String? = null
    private val downloaded = mutableListOf<File>()
    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var page: LinearLayout
    private lateinit var accountList: LinearLayout
    private lateinit var modsList: LinearLayout
    private lateinit var status: TextView
    private var modsFolderUri: Uri? = null

    private val folderPicker = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            try {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            } catch (_: SecurityException) { }
            modsFolderUri = uri
            getSharedPreferences("mods", MODE_PRIVATE).edit().putString("folder", uri.toString()).apply()
            if (::status.isInitialized) status.text = "Папка mods выбрана."
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        accounts.addAll(
            getSharedPreferences("account_names", MODE_PRIVATE)
                .getStringSet("names", emptySet())?.toList()?.sorted() ?: emptyList()
        )
        activeAccount = getSharedPreferences("account_names", MODE_PRIVATE)
            .getString("active", null)?.takeIf { it in accounts }
        modsFolderUri = getSharedPreferences("mods", MODE_PRIVATE)
            .getString("folder", null)?.let { Uri.parse(it) }
        val modsDir = getExternalFilesDir("mods")
        if (modsDir != null) {
            modsDir.mkdirs()
            downloaded.addAll(modsDir.listFiles()?.filter { it.isFile && it.extension.equals("jar", true) } ?: emptyList())
        }

        page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 18, 18, 18)
            setBackgroundColor(dark)
        }
        page.addView(TextView(this).apply {
            text = "MULTIACCOUNT\nCONNECT"
            textSize = 26f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(8, 12, 8, 16)
        }, LinearLayout.LayoutParams(-1, -2))

        val tabs = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        tabs.addView(makeButton("Аккаунты") { showAccounts() }, LinearLayout.LayoutParams(0, -2, 1f))
        tabs.addView(makeButton("Моды") { showMods() }, LinearLayout.LayoutParams(0, -2, 1f))
        page.addView(tabs)
        setContentView(page)
        showAccounts()
    }

    private fun makeButton(label: String, action: () -> Unit): Button {
        return Button(this).apply {
            text = label
            setTextColor(Color.WHITE)
            backgroundTintList = ColorStateList.valueOf(purple)
            setOnClickListener { action() }
        }
    }

    private fun clearContent() {
        if (page.childCount > 2) page.removeViews(2, page.childCount - 2)
    }

    private fun showAccounts() {
        clearContent()
        page.addView(makeButton("+ Добавить никнейм") { addNickname() })
        accountList = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        page.addView(ScrollView(this).apply { addView(accountList) }, LinearLayout.LayoutParams(-1, 0, 1f))
        page.addView(makeButton("Запустить MJLauncher") { openLauncher() })
        renderAccounts()
    }

    private fun addNickname() {
        val input = EditText(this).apply { hint = "Minecraft nickname" }
        AlertDialog.Builder(this).setTitle("Добавить никнейм").setView(input)
            .setPositiveButton("Добавить") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty() && name.length <= 32 && !accounts.contains(name)) {
                    accounts.add(name)
                    saveNames()
                    renderAccounts()
                }
            }.setNegativeButton("Отмена", null).show()
    }

    private fun renderAccounts() {
        accountList.removeAllViews()
        if (accounts.isEmpty()) {
            accountList.addView(TextView(this).apply {
                text = "Список пока пуст. Добавь никнейм."
                setTextColor(Color.WHITE)
                setPadding(8, 20, 8, 20)
            })
        }
        accounts.forEach { name ->
            val row = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
            row.addView(TextView(this).apply {
                text = if (name == activeAccount) "✓ $name (выбран)" else name
                textSize = 16f
                setTextColor(Color.WHITE)
            }, LinearLayout.LayoutParams(0, -2, 1f))
            row.addView(makeButton(if (name == activeAccount) "Выбран" else "Выбрать") {
                activeAccount = name
                getSharedPreferences("account_names", MODE_PRIVATE).edit()
                    .putString("active", name).apply()
                renderAccounts()
            })
            row.addView(makeButton("Удалить") {
                accounts.remove(name)
                if (activeAccount == name) {
                    activeAccount = accounts.firstOrNull()
                    getSharedPreferences("account_names", MODE_PRIVATE).edit()
                        .putString("active", activeAccount).apply()
                }
                saveNames()
                renderAccounts()
            })
            accountList.addView(row, ViewGroup.LayoutParams(-1, -2))
        }
    }

    private fun saveNames() {
        getSharedPreferences("account_names", MODE_PRIVATE).edit()
            .putStringSet("names", accounts.toSet()).apply()
    }

    private fun showMods() {
        clearContent()
        status = TextView(this).apply {
            text = "Загружай только моды из доверенных источников."
            setTextColor(Color.LTGRAY)
            textSize = 13f
            setPadding(4, 8, 4, 8)
        }
        page.addView(status)
        page.addView(makeButton("Выбрать папку mods") { folderPicker.launch(null) })
        page.addView(makeButton("Скачать мод по HTTPS-ссылке (.jar)") { askDownloadUrl() })
        page.addView(makeButton("Открыть каталог Modrinth") {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://modrinth.com/mods")))
        })
        modsList = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        page.addView(ScrollView(this).apply { addView(modsList) }, LinearLayout.LayoutParams(-1, 0, 1f))
        renderDownloads()
    }

    private fun askDownloadUrl() {
        val input = EditText(this).apply {
            hint = "https://site.example/mod.jar"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_URI
        }
        AlertDialog.Builder(this).setTitle("Ссылка на JAR-мод")
            .setMessage("Нужна прямая HTTPS-ссылка на файл .jar.")
            .setView(input).setPositiveButton("Скачать") { _, _ ->
                val link = input.text.toString().trim()
                val path = link.substringBefore("?").substringBefore("#")
                if (link.startsWith("https://", true) && path.endsWith(".jar", true)) {
                    downloadJar(link)
                } else {
                    status.text = "Проверь ссылку: HTTPS и расширение .jar."
                }
            }.setNegativeButton("Отмена", null).show()
    }

    private fun downloadJar(link: String) {
        status.text = "Загрузка началась…"
        executor.execute {
            try {
                val connection = URL(link).openConnection()
                connection.connectTimeout = 15000
                connection.readTimeout = 30000
                connection.setRequestProperty("User-Agent", "MultiAccountConnect/0.2")
                connection.connect()
                val length = connection.contentLengthLong
                if (length > 100L * 1024L * 1024L) throw IllegalArgumentException("Файл больше 100 МБ")
                val rawName = Uri.parse(link).lastPathSegment ?: "downloaded-mod.jar"
                val safeName = rawName.replace(Regex("[^A-Za-z0-9._-]"), "_")
                    .takeIf { it.endsWith(".jar", true) } ?: "downloaded-mod.jar"
                val dest = File(getExternalFilesDir("mods") ?: filesDir, safeName)
                connection.getInputStream().use { input ->
                    dest.outputStream().use { output ->
                        val buffer = ByteArray(8192)
                        var total = 0L
                        while (true) {
                            val count = input.read(buffer)
                            if (count < 0) break
                            total += count
                            if (total > 100L * 1024L * 1024L) throw IllegalArgumentException("Файл больше 100 МБ")
                            output.write(buffer, 0, count)
                        }
                    }
                }
                runOnUiThread {
                    downloaded.removeAll { it.name == dest.name }
                    downloaded.add(dest)
                    status.text = "Скачано: " + dest.name
                    renderDownloads()
                }
            } catch (e: Exception) {
                runOnUiThread { status.text = "Не удалось скачать: " + (e.message ?: "ошибка сети") }
            }
        }
    }

    private fun renderDownloads() {
        modsList.removeAllViews()
        if (downloaded.isEmpty()) {
            modsList.addView(TextView(this).apply {
                text = "Скачанные моды появятся здесь."
                setTextColor(Color.WHITE)
                setPadding(6, 16, 6, 16)
            })
        }
        downloaded.forEach { file ->
            val row = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
            row.addView(TextView(this).apply {
                text = file.name
                setTextColor(Color.WHITE)
            }, LinearLayout.LayoutParams(0, -2, 1f))
            row.addView(makeButton("В mods") { copyToMods(file) })
            modsList.addView(row, ViewGroup.LayoutParams(-1, -2))
        }
    }

    private fun copyToMods(file: File) {
        val uri = modsFolderUri
        if (uri == null) {
            status.text = "Сначала выбери папку mods."
            return
        }
        try {
            val root = DocumentFile.fromTreeUri(this, uri)
            val target = root?.findFile(file.name) ?: root?.createFile("application/java-archive", file.name)
            if (target == null) {
                status.text = "Не удалось создать файл в выбранной папке."
                return
            }
            contentResolver.openOutputStream(target.uri, "wt")?.use { output ->
                file.inputStream().use { input -> input.copyTo(output) }
            }
            status.text = "Скопировано: " + file.name
        } catch (e: Exception) {
            status.text = "Ошибка копирования: " + (e.message ?: "ошибка")
        }
    }

    private fun openLauncher() {
        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val matches = packageManager.queryIntentActivities(launcherIntent, 0)
            .mapNotNull { info ->
                val label = info.loadLabel(packageManager)?.toString() ?: return@mapNotNull null
                val normalized = label.lowercase().replace(" ", "")
                if (normalized.contains("mjlauncher")) {
                    label to info.activityInfo.packageName
                } else null
            }
            .distinctBy { it.second }

        when {
            matches.size == 1 -> {
                val intent = packageManager.getLaunchIntentForPackage(matches[0].second)
                if (intent != null) startActivity(intent) else showLauncherNotFound()
            }
            matches.size > 1 -> {
                AlertDialog.Builder(this)
                    .setTitle("Выбери MJLauncher")
                    .setItems(matches.map { it.first }.toTypedArray()) { _, which ->
                        packageManager.getLaunchIntentForPackage(matches[which].second)?.let {
                            startActivity(it)
                        } ?: showLauncherNotFound()
                    }
                    .setNegativeButton("Отмена", null)
                    .show()
            }
            else -> showLauncherNotFound()
        }
    }

    private fun showLauncherNotFound() {
        AlertDialog.Builder(this)
            .setTitle("MJLauncher не найден")
            .setMessage("Установи MJLauncher и попробуй снова. Приложение ищет лаунчер по названию установленного приложения.")
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onDestroy() {
        executor.shutdownNow()
        super.onDestroy()
    }
}
