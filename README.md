# BlindRunApp（助盲跑系统）

## 📌 Project Introduction

BlindRunApp is an Android-based accessibility running assistance platform designed for visually impaired users.

Blind users can publish running companion requests, while volunteers can accept nearby tasks and provide real-time assistance during outdoor running activities.

This project focuses on accessibility, real-time collaboration, and safety.

---

## 🚀 MVP Features

* 👤 User Login / Registration
* 🏃 Running Companion Recruitment
* 📍 Nearby Recruitment List
* 🤝 Volunteer Matching System
* 🗺 Real-time Location Sharing
* 🔊 TTS Voice Accessibility
* 🚨 SOS Emergency Assistance

---

## 🧠 Tech Stack

### Android

* Kotlin
* Jetpack Compose
* MVVM Architecture
* Hilt (Dependency Injection)

### Network

* Retrofit
* OkHttp
* WebSocket

### Location & Accessibility

* GPS / AMap SDK
* Android TextToSpeech (TTS)

### Tools

* Git + GitHub
* Android Studio

---

## 🏗 Architecture

The project follows the MVVM architecture pattern:

```text
UI → ViewModel → Repository → API/DataSource
```

### Project Structure

```text
com.blindrun.app
│
├── core
├── data
├── feature
├── ui
├── di
```

### Structure Description

* `core/` → network, websocket, location, tts
* `data/` → api, model, repository
* `feature/` → business modules
* `ui/` → navigation and theme
* `di/` → Hilt dependency injection

---

## 📂 Feature Modules

### Recruit Module

Blind users can publish running requests.

### Nearby Module

Volunteers can browse nearby recruitment tasks.

### Run Module

Supports real-time location synchronization.

### SOS Module

Emergency assistance and location upload.

---

## 📈 Current Progress

### ✅ Completed

* Android project initialization
* Git & GitHub setup
* MVVM architecture design
* Project module structure
* Gradle & dependency configuration

### 🚧 In Development

* Login System
* Recruit Publishing
* Nearby Matching
* WebSocket Location Sync

---

## 🔥 Next Development Plan

* Real-time map synchronization
* Distance matching algorithm
* Accessibility optimization
* Running state management
* Voice interaction support

---

## 👨‍💻 Development Environment

| Tool           | Version |
| -------------- | ------- |
| Android Studio | Latest  |
| Kotlin         | 2.x     |
| Min SDK        | API 26  |
| Target SDK     | Latest  |

---

## 📜 License

This project is currently for educational and research purposes.
