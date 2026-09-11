# DoseBloom

[🇬🇧 English](#english-version) | [🇷🇺 Русский](#русская-версия)

---

<a name="english-version"></a>
## 🇬🇧 DoseBloom: Simple Android Medication Tracker & Pill Reminder

**DoseBloom** is a lightweight, private, and open-source Android application for tracking medications and scheduled doses locally on your device. 

Designed for users who need a straightforward way to keep track of pill schedules without unnecessary complexity, advertising, subscriptions, or mandatory cloud services.

### Key Features
- **Pill Schedules & Reminders**: Create medication courses, get exact alarm notifications, and record taken/skipped doses.
- **Daily Adherence Tracking**: Track today's doses with an animated progress card and review monthly adherence via the built-in calendar.
- **Inventory Management**: Track medication stock with low-stock warnings and quick restock buttons (+10, +30, +50).
- **As-Needed Medications**: Manage PRN (pro re nata) medications with a quick "Take now" button.
- **Privacy First**: 100% local storage. No accounts required. Complete JSON data export/import (including intake history).
- **Modern Adaptive UI**: Botanical color palette, edge-to-edge rendering, dark/light themes, and responsive navigation for phones, foldables, and tablets.
- **Home Screen Widget**: View your next scheduled dose directly from your launcher.

### Development & Technology
I created DoseBloom for my own needs with heavy assistance from ChatGPT, as this is my first serious Android project. 

- **Stack**: Kotlin 2.4, Jetpack Compose 1.12, Material 3, Room 2.8 with KSP.
- **Architecture**: MVI/MVVM with Coroutines, StateFlow, and a clean Repository layer.
- **Build**: Gradle 9.5, GitHub Actions CI/CD for signed release APKs.

### Contributions & Feedback
Bug reports, feature requests, and pull requests are welcome! If you are an experienced Android developer, constructive feedback on architecture or Compose performance is greatly appreciated.
[Open a new issue](https://github.com/poorangryman/dosebloom-public/issues/new)

**Disclaimer**: DoseBloom is a personal tracking tool and is not a substitute for medical advice or professional healthcare.

---

<a name="русская-версия"></a>
## 🇷🇺 DoseBloom: Простой трекер приема лекарств для Android

**DoseBloom** — это легкое, приватное приложение с открытым исходным кодом для отслеживания графика приема таблеток и лекарств прямо на вашем устройстве.

Создано для тех, кому нужна простая и понятная таблетница без рекламы, платных подписок, перегруженного интерфейса и обязательной регистрации в облаке.

### Основные возможности
- **Расписание и напоминания**: Создавайте курсы приема, получайте точные уведомления и отмечайте выпитые или пропущенные дозы (с возможностью отмены случайного клика).
- **Прогресс и статистика**: Следите за выполнением плана на сегодня с помощью виджета прогресса и просматривайте историю в календаре.
- **Контроль запасов**: Приложение предупредит, когда таблетки заканчиваются. Быстрое пополнение запасов в один клик (+10, +30, +50).
- **Прием "по необходимости"**: Отдельный режим для лекарств без жесткого графика с кнопкой «Принять сейчас».
- **Полная приватность**: Все данные хранятся только на телефоне. Поддерживается полный бэкап и перенос данных в формате JSON.
- **Современный интерфейс**: Адаптивный Material 3 дизайн, поддержка тёмной темы, планшетов и экранов с вырезами. Удобный виджет для рабочего стола.

### Технологии и разработка
Проект разрабатывался для личного использования при активной помощи ChatGPT. 

- **Стек**: Kotlin 2.4, Jetpack Compose, Material 3, Room (KSP), Coroutines/Flow.
- **Сборка**: Gradle 9.5, автоматическая публикация релизных APK через GitHub Actions.

### Обратная связь
Если вы пользуетесь приложением — буду рад отзывам и баг-репортам. Если вы Android-разработчик — конструктивные пулл-реквесты и советы по архитектуре горячо приветствуются!
[Открыть issue](https://github.com/poorangryman/dosebloom-public/issues/new)

**Отказ от ответственности**: DoseBloom является вспомогательным инструментом и не заменяет профессиональные медицинские назначения.
