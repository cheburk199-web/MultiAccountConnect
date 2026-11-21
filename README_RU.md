# MultiAccount Connect — Android prototype

Android-проект на Kotlin для телефона. Сборка APK запускается через GitHub Actions.

## Возможности прототипа
- Фиолетовый интерфейс.
- Локальный список никнеймов Minecraft (не авторизация Microsoft).
- Открытие установленного Pojav Launcher / Amethyst.
- Выбор папки mods через системный выбор папки.
- Загрузка JAR по прямой HTTPS-ссылке и копирование в выбранную папку mods.

## Сборка APK через GitHub
1. Открой вкладку Actions.
2. Выбери Build MultiAccountConnect APK.
3. Нажми Run workflow.
4. После зелёного статуса открой запуск и скачай artifact MultiAccountConnect-debug-apk.
5. Распакуй artifact ZIP; APK будет называться app-debug.apk.

## Пока не реализовано
- Вход через Microsoft/Minecraft OAuth.
- Сохранение настоящих Minecraft-аккаунтов.
- Запуск нескольких независимых Minecraft-клиентов одновременно.
- Каталог Modrinth внутри приложения (сейчас доступна ссылка на каталог и загрузка по прямой HTTPS-ссылке).

Не вводи пароль Microsoft в приложение. Одновременная игра с несколькими аккаунтами зависит от возможности запустить отдельные экземпляры Minecraft и ограничений Android/Pojav.


## Masterme4a MultiVM 0.3.0
- Отдельная кнопка запуска основного MJLauncher.
- Отдельная кнопка запуска MJLauncher из рабочего профиля Android (Island), если система разрешает межпрофильный запуск через LauncherApps.
- Проверка обнаружения MJLauncher в рабочем профиле.
- Если Android блокирует межпрофильный запуск, приложение показывает инструкцию открыть MJLauncher напрямую через Island.
- MultiVM не хранит пароли и токены и не управляет авторизацией Minecraft.
