# Project Overview: MyEduApp

## Project Purpose
MyEduApp is a professional mobile companion application designed to extend the functionality of an existing School Management ERP system. It provides a lightweight, secure, and role-based interface for daily school operations, focusing on accessibility for students, teachers, and administrative staff.

The application serves as a "mobile-first" experience, while complex administrative tasks, bulk operations, and system configuration remain handled by the primary Laravel-based web platform.

## Architecture
The project follows a modern, decoupled architecture:

### 1. Mobile Front-end (Kotlin Multiplatform)
- **Shared Logic:** A single Kotlin codebase handles networking (Ktor), serialization (kotlinx.serialization), data modeling, and business logic across Android and iOS.
- **UI Framework:** Compose Multiplatform allows for a unified UI declaration while maintaining platform-specific performance and accessibility.
- **Platform Specifics:** Uses `expect/actual` patterns for secure storage (Keystore for Android, Keychain for iOS).

### 2. Backend (Laravel REST API)
- **Framework:** Laravel 12.
- **Administration:** Voyager Admin Panel for backend data management.
- **Database:** MySQL (managed via XAMPP/MariaDB).
- **API Design:** RESTful endpoints providing JSON responses.

## Technical Stack
| Layer | Technology |
| :--- | :--- |
| **Mobile Language** | Kotlin |
| **Mobile UI** | Compose Multiplatform (Material 3) |
| **Networking** | Ktor Client |
| **Serialization** | kotlinx.serialization |
| **Backend Framework** | Laravel 12 |
| **Authentication** | Laravel Sanctum (Token-based) |
| **Database** | MySQL / MariaDB |
| **Development OS** | Windows 11 |
| **JDK Version** | 17.0.20.1 |
| **Gradle Version** | 9.6.0 |

## Core Implementation Principles
1. **Role-Based Experience:** The UI and navigation are dynamically generated based on the authenticated user's role.
2. **Security First:** No sensitive data (passwords, tokens) is stored in plain text. Backend remains the final authority for all authorization.
3. **Data Integrity:** Branch-scoping and user-ownership are enforced at the API level to prevent unauthorized data access.
4. **Resiliency:** Standardized handling of loading, empty, and error states for all API-driven components.
