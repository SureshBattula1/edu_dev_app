# MyEduApp Project Blueprint

This document provides a high-level overview of the **MyEduApp** project structure, architecture, and design principles.

## 🚀 High-Level Overview
MyEduApp is a **Kotlin Multiplatform (KMP)** application designed for Education Management. It targets both Android and iOS from a single codebase located in the `composeApp` module.

---

## 📂 Project Structure

### 1. Root Directory
- `/app`: Android-specific host application.
- `/composeApp`: **The core of the project.** Contains all shared logic and UI.
- `/docs`: Project documentation and specifications.
- `/branding`: Assets related to app identity and icons.
- `/gradle`: Gradle wrapper and configuration files.

### 2. `composeApp` (Shared Module)
Path: `composeApp/src/commonMain/kotlin/com/example/myeduapp/`

#### 🛠️ `/core` (Cross-cutting Concerns)
- **`/network`**: API Client (Ktor), Endpoints configuration, and Interceptors.
- **`/datastore`**: Session management and secure local storage.
- **`/navigation`**: Route definitions and App navigation logic.
- **`/ui`**: Global theme (Sunrise Academic), shared components, and colors.
- **`/util`**: Helper functions (Dates, Formatting, etc.).

#### 📦 `/data` (Data Layer)
- **`/model`**: Kotlin Serialization DTOs and internal domain models.
- **`/repository`**: Implementation of data fetching logic (Network + Cache).

#### 🏗️ `/features` (Feature-based Modules)
Organized by functional areas:
- **`/auth`**: Login, Splash, and Session validation.
- **`/teacher`**: Teacher-specific screens (Dashboard, Classes, Students).
- **`/attendance`**: Attendance marking and viewing hub.
- **`/leaves`**: Leave management and approval workflows.
- **`/notifications`**: Inbox and alert system.

#### 🎨 `/ui` (Shared UI Elements)
- Global components used across multiple features (Loaders, TopBars).

---

## 🏛️ Architecture
The project follows a **Layered Architecture** pattern:

1.  **UI Layer (Compose)**: Uses the `features` package. It observes state and handles user interaction.
2.  **Navigation (Voyager)**: Uses a Screen-based navigation stack for multiplatform support.
3.  **Domain/Repository Layer**: Repositories provide a clean API for features to access data without knowing the source (Network).
4.  **Network Layer (Ktor)**: Handles all REST communication with the Laravel backend.

---

## 🎨 Design System: "Sunrise Academic"
The app adheres to a modern, clean, and spacious aesthetic:
- **Primary Color**: Vibrant Academic Blue (`#007CC4`).
- **Typography**: Plus Jakarta Sans (Geometric clarity).
- **Shapes**: High corner radius (`rounded-3xl` / `24dp`) for a "pill" and "card" feel.
- **Surfaces**: Floating navigation elements and soft micro-shadows.

---

## 🛠️ Technical Stack
- **UI**: Jetpack Compose Multiplatform
- **Networking**: Ktor Client
- **Navigation**: Voyager
- **JSON Serialization**: Kotlinx Serialization
- **Image Loading**: Coil (via shared wrappers)
- **Concurrency**: Kotlin Coroutines & Flow
