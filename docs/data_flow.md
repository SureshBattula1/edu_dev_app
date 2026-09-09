# Data Flow Architecture

This document describes how information moves between the Laravel backend and the MyEduApp mobile client.

## High-Level Flow
1. **Request:** The Mobile App (Ktor Client) sends an HTTP request to the Laravel API.
2. **Authentication:** The request includes a Bearer Token in the `Authorization` header.
3. **Processing:** Laravel (Sanctum Middleware) validates the token and the user's role/permissions.
4. **Response:** Laravel returns a JSON response containing DTO (Data Transfer Object) structures.
5. **Mapping:** The Mobile App deserializes JSON into Kotlin Data Classes using `kotlinx.serialization`.
6. **UI Update:** The UI reacts to the new state and displays the data.

## 1. REST API Integration (Ktor)
The app uses a centralized `ApiClient` configured with:
- **ContentNegotiation:** To handle JSON serialization.
- **Auth Plugin:** Automatically attaches the stored Sanctum token to every outgoing request.
- **Error Handling:** Maps HTTP status codes (401, 403, 422, 500) to internal application exceptions.

## 2. DTO Mapping & Serialization
Data is passed using structured JSON. Example mapping for a User:
- **Backend (JSON):** `{"id": 1, "name": "John Doe", "role": "Teacher"}`
- **Mobile (Kotlin):** `data class User(val id: Int, val name: String, val role: UserRole)`

## 3. Laravel Sanctum Token Handling
Authentication is handled via stateful API tokens:
1. **Login:** User submits credentials to `/api/login`.
2. **Issuance:** Backend validates and returns a `plainTextToken`.
3. **Secure Storage:** The app stores this token in platform-specific secure storage (Android Keystore / iOS Keychain).
4. **Session Persistence:** On app launch, the `SessionManager` checks for a valid token to skip the login screen.
5. **Expiration/Revocation:** If the API returns `401 Unauthorized`, the `SessionManager` clears the local token and redirects the user to the Login screen.

## 4. Environment Configuration
To bridge the gap between local development and emulators:
- **Localhost (Web):** `http://127.0.0.1:8000/api`
- **Android Emulator:** `http://10.0.2.2:8000/api`
- **Physical Device:** `http://[LAN_IP]:8000/api`

The `ApiConfig` object dynamically selects the correct base URL based on the build type and platform.
