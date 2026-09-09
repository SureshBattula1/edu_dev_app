# Security Architecture

Security is a primary pillar of MyEduApp, ensuring that sensitive student and financial data is protected across all roles.

## 1. Secure Token Storage
The application NEVER stores authentication tokens or passwords in plain text `SharedPreferences` or `UserDefaults`.

- **Android Implementation:** Uses the **Android Keystore System** to encrypt the token. Specifically, `EncryptedSharedPreferences` or a custom Keystore wrapper is used to ensure data is encrypted at rest.
- **iOS Implementation:** Uses the **Keychain Services API**, which provides a secure, encrypted container for sensitive data, persisting even if the app is deleted and reinstalled.

## 2. Role-Based Access Control (RBAC)
The application recognizes 6 distinct roles, mapped from the backend:
1. **Super Admin:** Global monitoring and cross-branch insights.
2. **Branch Admin:** Full management within a single branch scope.
3. **Teacher:** Academic management, attendance marking, and class-specific data.
4. **Staff:** General student and record lookups.
5. **Accountant:** Financial record management and fee collection monitoring.
6. **Student:** Personal academic and financial self-service.

### Role Synchronization
- Roles are assigned in the Laravel Backend (Voyager/Laravel Auth).
- Upon login, the `/api/me` endpoint returns the user's role.
- The `AppNavigation` system dynamically builds the UI modules based on this role.
- **Hard Rule:** UI-side role hiding is for user experience only. The backend MUST still validate permissions for every API request.

## 3. Backend Authorization Middleware
The mobile app relies on the Laravel backend to enforce:
- **Branch Scoping:** Users cannot see data belonging to other school branches.
- **Student Ownership:** Students/Parents can only see their own records.
- **Method Security:** Only Accountants/Admins can access Fee management endpoints.

## 4. Secure Session Lifecycle
- **Automatic Logout:** If a token is revoked or expires (HTTP 401), the app forces a logout.
- **Protected Actions:** Sensitive actions (like changing passwords) require the current password or a valid session.
- **No Sensitive Logs:** Authorization headers and access tokens are explicitly excluded from logs in release builds.
