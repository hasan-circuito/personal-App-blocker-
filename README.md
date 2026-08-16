# FocusGuard 🛡️

A modern, privacy-focused Android application designed to promote digital wellbeing and focus by providing intelligent content filtering and application blocking capabilities.

![Android](https://img.shields.io/badge/Android-Kotlin-brightgreen.svg)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-blue.svg)
![Room Database](https://img.shields.io/badge/Storage-Room%20DB-orange.svg)
![License](https://img.shields.io/badge/License-MIT-lightgrey.svg)

---

## 🌟 Key Features

- **🛡️ Intelligent Content Filtering**: Heuristic detection engine that identifies prohibited adult search terms in real-time.
- **🌐 Browser Input Protection**: Automatically clears prohibited input in browsers (e.g. Google Chrome, Firefox, Edge) without locking out or closing the browser application, allowing safe browsing to continue uninterrupted.
- **📱 Application Lockout Engine**: Intercepts monitored non-browser apps and applies a temporary 2-hour lockout overlay with countdown timers.
- **📊 Real-time Dashboard**: Overview of protection state, total blocked events, active lockouts, and monitored applications.
- **📜 Block History & Analytics**: Log of past blocked events, confidence scores, and trigger reasons.
- **⚙️ Customizable Sensitivity**: Adjustable detection levels (Strict, Standard, Relaxed) and lockout duration settings.

---

## 🏗️ Architecture & Technology Stack

FocusGuard is built following **Clean Architecture** and **MVVM** principles using modern Android development practices:

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material Design 3
- **Local Database**: [Room Database](https://developer.android.com/training/data-storage/room) with KSP
- **Background Engine**: Android `AccessibilityService`
- **Testing**: Robolectric local JVM testing & Roborazzi screenshot verification

---

## 📂 Project Structure

```
app/src/main/java/com/example/
├── data/
│   ├── db/          # Room Entities, DAOs, and Database
│   ├── model/       # Data models & ProtectionState
│   └── repository/  # Thread-safe FocusGuardRepository
├── detection/       # HeuristicContentDetector engine
├── policy/          # BlockingPolicyManager & Domain Extractor
├── service/         # FocusGuardAccessibilityService & Overlay Manager
├── ui/
│   ├── components/  # Reusable Compose components
│   ├── screens/     # Dashboard, MonitoredApps, Settings, History
│   ├── theme/       # Material Design 3 Theme & Colors
│   └── viewmodel/   # StateFlow ViewModels
└── util/            # App helpers & Browser utilities
```

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug (or newer)
- JDK 17+
- Android SDK 26+ (Android 8.0 Oreo or higher)

### Build & Run
1. Clone the repository:
   ```bash
   git clone https://github.com/hasan-circuito/personal-App-blocker-.git
   ```
2. Open the project in Android Studio.
3. Build and run on an Android device or emulator.
4. Enable the **FocusGuard Accessibility Service** in System Settings when prompted by the setup wizard.

---

## 📄 License

This project is open-source and available under the [MIT License](LICENSE).
