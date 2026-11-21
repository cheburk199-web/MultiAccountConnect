# Masterme4a MultiAccount website

## Что уже подготовлено
- Регистрация и вход по email/паролю через Firebase Authentication.
- Пять слотов на каждого пользователя.
- Данные профиля хранятся в Firestore под UID пользователя.
- Правила Firestore запрещают читать/менять чужой профиль.
- Кнопка открытия установленного MJLauncher (Android).

## Подключение перед публикацией
1. Создай проект в Firebase Console: https://console.firebase.google.com/
2. В Authentication -> Sign-in method включи Email/Password.
3. Создай Firestore Database.
4. В Project settings -> Your apps создай Web app и скопируй firebaseConfig.
5. Вставь реальные значения вместо PASTE_* в app.js.
6. Примени firestore.rules к Firestore.
7. Установи Firebase CLI и выполни из папки website: firebase deploy --only hosting,firestore:rules

Важно: Firebase web apiKey не является паролем; не публикуй service-account JSON или приватные ключи. Пароли Microsoft/Minecraft сайт не собирает. Пять слотов — это сохранённые профили/ники, а не пять гарантированно независимых процессов MJLauncher.