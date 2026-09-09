# Setup & Environment Requirements

Follow these requirements to ensure a functional development environment for MyEduApp.

## 1. System Requirements (Mobile)
- **Operating System:** Windows 11 (Android Development), macOS (iOS Development).
- **Java Development Kit (JDK):** JDK 17.0.20.1 (Mandatory).
- **Android SDK:** API Level 34+ (Android 14+).
- **Gradle:** version 9.6.0.
- **Kotlin:** version 2.x (Multiplatform enabled).
- **IDE:** Android Studio Ladybug or newer with KMP plugins.

## 2. Backend Requirements (Laravel)
- **PHP:** 8.2 or higher.
- **Web Server:** XAMPP (Apache + MySQL/MariaDB).
- **Composer:** For dependency management.
- **Laravel:** Version 12.
- **Sanctum:** For API Authentication.

## 3. Local API Setup
The backend must be running locally to serve the mobile app.

### Start Laravel Server:
```powershell
cd C:\xampp\htdocs\schools\laravel-demo-app-sc
php artisan serve --host=127.0.0.1 --port=8000
```

### Localhost Configuration (Critical):
Due to how emulators handle networking, the `baseUrl` must be configured correctly in `data/api/ApiConfig.kt`:

- **Physical Device:** Use your machine's LAN IP (e.g., `http://192.168.1.10:8000/api`). Ensure the phone and PC are on the same Wi-Fi.
- **Android Emulator:** Use `http://10.0.2.2:8000/api`. This is a special alias for the host machine's `127.0.0.1`.
- **iOS Simulator:** Use `http://127.0.0.1:8000/api`.

## 4. Database Setup
- **Tool:** phpMyAdmin or MySQL Workbench.
- **Database Name:** `demo_school_management`.
- Ensure migrations are seeded to have test users for all 6 roles (Super Admin, Branch Admin, Teacher, Staff, Accountant, Student).

## 5. Build Verification
To verify the mobile environment, run the following from the project root:
```powershell
.\gradlew.bat build
.\gradlew.bat :composeApp:assembleDebug
```
