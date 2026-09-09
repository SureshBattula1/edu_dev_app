# MyEduApp — Mobile App Build Specification

**Version:** 1.0
**Date:** 2026-09-07
**Platform:** Kotlin Multiplatform + Compose Multiplatform
**Development OS:** Windows 11
**Primary Target:** Android
**Secondary Target:** iOS
**Backend:** Laravel 12 REST API
**Authentication:** Laravel Sanctum
**Database:** MySQL
**Existing Website:** Yes

---

# 1. IMPORTANT PROJECT DIRECTION

MyEduApp is a mobile companion application for an existing School Management Website.

The website already provides advanced administration and ERP functionality.

DO NOT attempt to duplicate the complete website inside the mobile application.

The mobile application must focus on:

- Daily school operations
- Important information
- Attendance
- Fees
- Exams/results
- Communication
- Leave management
- Timetable
- Notifications
- Role-based dashboards
- Secure self-service

The website remains the primary platform for:

- Advanced administration
- Complex configuration
- Bulk operations
- Advanced accounting
- Master data management
- Roles and permissions administration
- Admissions
- Advanced reports
- Library administration
- Transport administration
- System configuration

---

# 2. EXISTING BACKEND

Backend location:

```text
C:\xampp\htdocs\schools\laravel-demo-app-sc\
```

Backend:

```text
Laravel 12
Laravel Sanctum
MySQL
```

Database:

```text
demo_school_management
```

Development API:

```text
http://localhost:8000/api
```

Health endpoint:

```text
GET /api/health
```

The backend already provides role-scoped and branch-scoped access.

The mobile application MUST NOT replace backend authorization with client-side authorization.

The Laravel backend is the final security authority.

---

# 3. MOBILE PROJECT

Project:

```text
C:\xampp\htdocs\schools\MyEduApp\
```

Technology:

```text
Kotlin Multiplatform
Compose Multiplatform
Kotlin
Ktor Client
kotlinx.serialization
```

The project must support:

```text
Android
iOS
```

Development priority:

```text
Android first
iOS compatible architecture
```

Do not introduce unnecessary frameworks.

---

# 4. CORE PRINCIPLE

The mobile app must follow:

```text
Mobile UI
    ↓
Role-based navigation
    ↓
Ktor API Client
    ↓
Laravel REST API
    ↓
Sanctum authentication
    ↓
Permission middleware
    ↓
Branch/User scope
    ↓
MySQL
```

The mobile UI can hide features based on role.

However:

IMPORTANT:

Hiding a menu is NOT security.

Every protected API request must still be authorized by Laravel.

---

# 5. USER ROLES

The existing backend roles are:

1. Super Admin
2. Branch Admin
3. Teacher
4. Staff
5. Accountant
6. Student

Do not create additional roles unless the backend already supports them.

Do not create a Parent role in V1.

---

# 6. ROLE SECURITY

Create:

```text
data/model/UserRole.kt
```

Use:

```kotlin
enum class UserRole {
    SUPER_ADMIN,
    BRANCH_ADMIN,
    TEACHER,
    STAFF,
    ACCOUNTANT,
    STUDENT
}
```

Map backend role values:

```text
SuperAdmin  -> SUPER_ADMIN
BranchAdmin -> BRANCH_ADMIN
Teacher     -> TEACHER
Staff       -> STAFF
Accountant  -> ACCOUNTANT
Student     -> STUDENT
```

Unknown roles must NOT automatically receive administrator access.

Unknown roles should receive the safest restricted experience or force logout/error handling.

DO NOT use:

```kotlin
else -> SUPER_ADMIN
```

---

# 7. AUTHENTICATION

Login endpoint:

```text
POST /api/login
```

Login accepts:

```text
email
password
```

The backend may accept email or phone through the login field.

Successful response contains:

```text
success
message
user
access_token
token_type
expires_in
```

After login:

```text
1. Store access token securely
2. Store minimum required user information
3. Call GET /api/me
4. Refresh current user information
5. Determine role
6. Build role-specific navigation
7. Load dashboard
```

---

# 8. TOKEN STORAGE

DO NOT store authentication tokens in plain text preferences.

Use platform-secure storage.

Android:

```text
Android Keystore / encrypted storage
```

iOS:

```text
Keychain
```

The shared architecture must allow platform-specific secure storage.

Never:

```text
Log access tokens
Display access tokens
Store passwords
Send tokens to analytics
Store tokens in source code
```

---

# 9. LOGOUT

Endpoint:

```text
POST /api/logout
```

Logout must:

```text
1. Call logout API
2. Delete local token
3. Clear user session
4. Clear sensitive cached data
5. Navigate to Login
```

If API returns:

```text
401 Unauthorized
```

the application should automatically clear the session and return to Login.

If API returns:

```text
403 Forbidden
```

show:

```text
You don't have access to this feature.
```

Do not crash.

---

# 10. API BASE URL

Development:

```text
http://localhost:8000/api
```

IMPORTANT:

Android emulator localhost does NOT refer to the Windows host machine.

For Android Emulator development use:

```text
http://10.0.2.2:8000/api
```

For a physical Android phone, use the Windows machine's LAN IP.

Production MUST use HTTPS.

Do not hard-code production secrets.

Create:

```text
data/api/ApiConfig.kt
```

and centralize environment configuration.

---

# 11. REQUIRED API CLIENT

Create:

```text
data/api/ApiClient.kt
```

Use Ktor Client.

Requirements:

```text
ContentNegotiation
JSON
Timeout
Authorization header
HTTP status handling
Error handling
Logging only in debug builds
```

Authorization:

```http
Authorization: Bearer <token>
```

Do not log Authorization headers.

---

# 12. RECOMMENDED PROJECT STRUCTURE

Use:

```text
composeApp/
└── src/
    └── commonMain/
        └── kotlin/
            └── com/example/myeduapp/

                data/
                ├── api/
                │   ├── ApiClient.kt
                │   ├── ApiConfig.kt
                │   ├── AuthApi.kt
                │   ├── DashboardApi.kt
                │   ├── StudentApi.kt
                │   ├── AttendanceApi.kt
                │   ├── FeeApi.kt
                │   ├── ExamApi.kt
                │   ├── LeaveApi.kt
                │   └── CommunicationApi.kt
                │
                ├── model/
                │   ├── User.kt
                │   ├── Branch.kt
                │   ├── Dashboard.kt
                │   ├── Attendance.kt
                │   ├── Fee.kt
                │   ├── Exam.kt
                │   ├── Leave.kt
                │   └── Communication.kt
                │
                ├── repository/
                │   ├── AuthRepository.kt
                │   ├── DashboardRepository.kt
                │   ├── StudentRepository.kt
                │   ├── AttendanceRepository.kt
                │   ├── FeeRepository.kt
                │   ├── ExamRepository.kt
                │   ├── LeaveRepository.kt
                │   └── CommunicationRepository.kt
                │
                └── session/
                    ├── SessionManager.kt
                    └── AuthState.kt

                navigation/
                ├── AppNavigation.kt
                ├── Route.kt
                └── Module.kt

                ui/
                ├── auth/
                │   └── LoginScreen.kt
                │
                ├── dashboard/
                │   └── DashboardScreen.kt
                │
                ├── attendance/
                │   └── AttendanceScreen.kt
                │
                ├── students/
                │   └── StudentsScreen.kt
                │
                ├── fees/
                │   └── FeesScreen.kt
                │
                ├── exams/
                │   └── ExamsScreen.kt
                │
                ├── leaves/
                │   └── LeaveScreen.kt
                │
                ├── timetable/
                │   └── TimetableScreen.kt
                │
                ├── communications/
                │   └── NoticesScreen.kt
                │
                └── profile/
                    └── ProfileScreen.kt
```

---

# 13. MOBILE V1 FEATURES

The mobile app MUST include:

## Authentication

- Login
- Logout
- Session persistence
- Change password
- Profile
- Secure token storage

## Dashboard

- Role-specific dashboard
- Important statistics
- Attendance summary
- Fee information
- Upcoming exams
- Notifications
- Quick actions

## Attendance

- Student attendance
- Attendance percentage
- Monthly attendance
- Teacher attendance marking
- Class attendance where permitted

## Students

Only authorized staff/admin/teacher users.

Features:

- Student search
- Student basic information
- Class/section
- Basic academic information
- Attendance summary

Do not expose the complete student database to unauthorized users.

## Fees

- Pending fees
- Fee dues
- Payment history
- Total paid
- Outstanding balance
- Fee receipt/invoice view where supported

## Exams

- Upcoming exams
- Exam schedule
- Results
- Marks where authorized

## Leaves

- Apply leave
- Leave history
- Leave status

## Timetable

- My timetable
- Class timetable where permitted

## Communication

- Announcements
- Notifications
- Circulars
- Events
- Holidays

## Profile

- Name
- Phone
- Avatar
- Branch
- Role
- Change password
- Logout

---

# 14. FEATURES NOT REQUIRED IN MOBILE V1

DO NOT implement these unless specifically requested later:

```text
Branch Management
Role Management
Permission Management
Full User Management
Admissions
Bulk Import
Bulk Management
Advanced Accounting
Complex Transactions
Advanced Financial Reports
Comparative Analytics
Library Management
Transport Management
Advanced Master Data
Global Search
Complex System Settings
```

These remain website features.

---

# 15. SUPER ADMIN MOBILE

Super Admin should receive a monitoring-focused app.

Navigation:

```text
Home
Branches
Students
Reports
More
```

Dashboard:

```text
Total Students
Total Teachers
Total Branches
Attendance Rate
Fee Collection
Pending Fees
Upcoming Exams
Low Attendance
Important Alerts
```

Super Admin should NOT receive all website administration screens.

---

# 16. BRANCH ADMIN MOBILE

Navigation:

```text
Home
Students
Attendance
Fees
More
```

Features:

```text
Branch dashboard
Students
Teachers
Attendance
Fees
Exams
Leaves
Announcements
Holidays
Basic reports
```

All data must remain restricted to the administrator's authorized branch scope.

---

# 17. TEACHER MOBILE

Teacher is a high-priority mobile role.

Navigation:

```text
Home
Classes
Attendance
Exams
Profile
```

Features:

```text
My Classes
My Students
Student Attendance
Mark Attendance
Attendance History
Marks / Exams
Timetable
My Leaves
Holidays
Announcements
Notifications
```

Attendance flow:

```text
Select Class
    ↓
Select Section
    ↓
Load Students
    ↓
Present / Absent
    ↓
Submit
    ↓
Success confirmation
```

Teacher must not access students outside authorized classes/branches.

---

# 18. STAFF MOBILE

Navigation:

```text
Home
Students
Attendance
Leaves
Profile
```

Features:

```text
Student search
Student basic details
Attendance
Leave information
Basic fee information
Announcements
Holidays
Notifications
```

No accounting administration.

No roles/permissions administration.

---

# 19. ACCOUNTANT MOBILE

Navigation:

```text
Home
Fees
Payments
Invoices
Profile
```

Dashboard:

```text
Today's Collection
Pending Fees
Total Collection
Payment Count
Outstanding Fees
```

Features:

```text
Student fee dues
Payments
Fee history
Invoices
Basic collection information
Basic reports
```

Advanced accounting remains on the website.

---

# 20. STUDENT MOBILE

Student must have the simplest interface.

Navigation:

```text
Home
Attendance
Fees
Results
Profile
```

Dashboard:

```text
Attendance %
Pending Fee
Upcoming Exam
Latest Notification
```

Student features:

```text
My Attendance
My Fees
My Exams
My Results
My Leaves
My Timetable
Announcements
Notifications
Holidays
Events
```

Students must never be able to access another student's information.

Never accept arbitrary student IDs from the client as proof of authorization.

The backend must enforce ownership.

---

# 21. ROLE-BASED NAVIGATION

Create:

```text
navigation/AppNavigation.kt
```

Example:

```kotlin
fun modulesFor(role: UserRole): List<Module> {
    return when (role) {
        UserRole.SUPER_ADMIN -> superAdminModules()
        UserRole.BRANCH_ADMIN -> branchAdminModules()
        UserRole.TEACHER -> teacherModules()
        UserRole.STAFF -> staffModules()
        UserRole.ACCOUNTANT -> accountantModules()
        UserRole.STUDENT -> studentModules()
    }
}
```

Navigation must never show unauthorized modules.

---

# 22. DASHBOARD DESIGN

Dashboard should be simple and mobile-friendly.

Do NOT reproduce the entire website dashboard.

Use:

```text
Header
    ↓
Welcome message
    ↓
Important statistics
    ↓
Quick actions
    ↓
Upcoming events/exams
    ↓
Notifications
```

Cards should be:

- Simple
- Fast
- Readable
- Touch friendly

Avoid excessive charts.

Only show charts when they provide meaningful information.

---

# 23. UI DESIGN

Use a clean modern education application design.

Requirements:

```text
Material 3
Rounded cards
Clear typography
Consistent spacing
Responsive layouts
Light theme
Dark theme support
Loading states
Empty states
Error states
Pull-to-refresh where useful
```

Do not use excessive animations.

Do not create a complicated dashboard.

Focus on usability.

---

# 24. LOADING STATES

Every API-driven screen must have:

```text
Loading
Success
Empty
Error
```

Example:

```text
Loading:
CircularProgressIndicator

Empty:
No attendance records found.

Error:
Unable to load attendance.
Retry
```

Never leave a blank screen.

---

# 25. API ERROR HANDLING

Handle:

```text
200 Success
201 Created
400 Bad Request
401 Unauthorized
403 Forbidden
404 Not Found
422 Validation Error
429 Too Many Requests
500 Server Error
503 Service Unavailable
```

Show user-friendly messages.

Do not expose Laravel stack traces.

Do not expose SQL errors.

---

# 26. SECURITY REQUIREMENTS

Mandatory:

```text
HTTPS in production
Sanctum Bearer token
Secure token storage
No password storage
No token logging
No sensitive debug logs in release
Role-aware UI
Server-side authorization
Branch-level authorization
Student ownership authorization
401 handling
403 handling
Input validation
Secure logout
```

Do not trust:

```text
role from local storage
student ID from UI
branch ID from UI
permission flags from UI
```

The server must validate them.

---

# 27. LOCAL DEVELOPMENT API

Laravel backend:

```powershell
cd C:\xampp\htdocs\schools\laravel-demo-app-sc

php artisan serve --host=127.0.0.1 --port=8000
```

Health check:

```text
GET http://127.0.0.1:8000/api/health
```

For Android Emulator:

```text
http://10.0.2.2:8000/api
```

For physical Android device:

```text
http://YOUR_WINDOWS_LAN_IP:8000/api
```

Do not use:

```text
localhost
```

inside the Android emulator API configuration.

---

# 28. BACKEND API PRIORITY

Implement API integration in this order:

## Priority 1

```text
POST /api/login
GET /api/me
POST /api/logout
```

## Priority 2

```text
GET /api/dashboard
GET /api/dashboard/stats
```

## Priority 3

```text
GET /api/attendance/student/{userId}/overview
GET /api/attendance/student/{userId}
```

## Priority 4

```text
GET /api/students/{userId}/fees
GET /api/fee-dues/student/{userId}
```

## Priority 5

```text
GET /api/students/{userId}/results
GET /api/dashboard/upcoming-exams
```

## Priority 6

```text
GET /api/leaves/student/{userId}
POST /api/leaves
```

## Priority 7

```text
GET /api/communications/notifications
GET /api/communications/announcements
GET /api/communications/circulars
GET /api/events
GET /api/holidays
GET /api/holidays/upcoming
```

These APIs are already documented as available in the existing backend guide.

---

# 29. DEVELOPMENT ORDER

Do NOT build all screens simultaneously.

Build in this order:

```text
STEP 1
Project structure
        ↓
STEP 2
Ktor API client
        ↓
STEP 3
Secure session
        ↓
STEP 4
Login
        ↓
STEP 5
Role detection
        ↓
STEP 6
Role-based navigation
        ↓
STEP 7
Dashboard
        ↓
STEP 8
Student features
        ↓
STEP 9
Attendance
        ↓
STEP 10
Fees
        ↓
STEP 11
Exams/results
        ↓
STEP 12
Leaves
        ↓
STEP 13
Timetable
        ↓
STEP 14
Communication
        ↓
STEP 15
Profile/settings
        ↓
STEP 16
Security testing
        ↓
STEP 17
Android release build
        ↓
STEP 18
iOS build/testing on macOS/Xcode
```

---

# 30. DO NOT BREAK EXISTING CODE

Before changing existing files:

1. Inspect the current project.
2. Understand the current architecture.
3. Reuse existing working components.
4. Do not replace working Gradle configuration unnecessarily.
5. Do not upgrade Kotlin/Gradle/Compose versions unless required.
6. Do not remove existing dependencies without checking usage.
7. Build after each major change.

Current environment:

```text
Windows 11
JDK 17.0.20.1
Gradle 9.6.0
Kotlin 2.x
Compose Multiplatform
```

---

# 31. BUILD VERIFICATION

From:

```text
C:\Users\user\AndroidStudioProjects\MyEduApp
```

run:

```powershell
.\gradlew.bat --version
```

Then:

```powershell
.\gradlew.bat build
```

Android build:

```powershell
.\gradlew.bat :composeApp:assembleDebug
```

If the module is different, inspect:

```powershell
.\gradlew.bat projects
```

Do not assume the module name.

---

# 32. ANDROID TESTING

Use Android Emulator.

Recommended:

```text
Pixel device
Recent Android API
```

Verify:

```text
Login
Dashboard
Role navigation
API requests
Attendance
Fees
Results
Leave
Logout
401 handling
403 handling
Rotation/configuration changes
Network failure
Empty API response
```

---

# 33. IOS COMPATIBILITY

The shared code must remain Kotlin Multiplatform compatible.

Do not use Android-only APIs inside commonMain.

Correct:

```text
commonMain
    shared business logic
    API models
    repositories
    networking
    shared Compose UI where appropriate
```

Android-only:

```text
androidMain
```

iOS-only:

```text
iosMain
```

Windows can be used for development.

Actual iOS Simulator/Xcode build requires macOS.

Do not attempt to fake an iOS build on Windows.

---

# 34. WEBSITE VS MOBILE RESPONSIBILITY

## Website

```text
Advanced ERP
Administration
Master data
Branches
Users
Roles
Permissions
Admissions
Bulk operations
Advanced accounting
Advanced reports
Library administration
Transport administration
System configuration
```

## Mobile

```text
Daily operations
Attendance
Student information
Fees
Results
Leave
Timetable
Announcements
Notifications
Personal profile
Role dashboards
```

The two applications should complement each other.

---

# 35. IMPORTANT UX RULE

Never show a large list of modules to every user.

Bad:

```text
Transport
Library
Finance
SIS
Academics
Food
Branches
Reports
Admissions
Users
Roles
Permissions
Settings
...
```

Good:

Student:

```text
Home
Attendance
Fees
Results
Profile
```

Teacher:

```text
Home
Classes
Attendance
Exams
Profile
```

Accountant:

```text
Home
Fees
Payments
Invoices
Profile
```

---

# 36. IMPLEMENTATION RULE FOR AI CODING AGENT

Before coding:

```text
1. Inspect existing MyEduApp source.
2. Inspect current Gradle configuration.
3. Inspect existing Compose screens.
4. Inspect existing API/backend routes where available.
5. Do not overwrite working project files blindly.
6. Make incremental changes.
7. Build after every major implementation step.
8. Fix compilation errors before proceeding.
```

When creating a file:

```text
Explain why the file is needed.
Follow the existing project package name.
Follow the existing coding style.
```

Do not generate fake API responses unless explicitly requested.

Use the actual Laravel API.

---

# 37. CURRENT DEVELOPMENT GOAL

The immediate goal is NOT to finish the entire application.

First create a working vertical slice:

```text
Login
   ↓
Secure Token
   ↓
GET /api/me
   ↓
Role Detection
   ↓
Role-specific Dashboard
   ↓
Role-specific Navigation
   ↓
Logout
```

Once this works for all six roles, implement individual modules.

---

# 38. SUCCESS CRITERIA

The first milestone is successful when:

```text
✓ App launches
✓ Login works
✓ Token is securely stored
✓ /api/me works
✓ Correct role detected
✓ Correct dashboard appears
✓ Correct menu appears
✓ Unauthorized menu is hidden
✓ API 401 handled
✓ API 403 handled
✓ Logout works
✓ Android emulator runs successfully
✓ Gradle build succeeds
```

Then continue module-by-module.

---

# 39. FINAL INSTRUCTION TO AI AGENT

You are working on an existing Kotlin Multiplatform + Compose Multiplatform project called MyEduApp.

DO NOT rebuild the project from scratch.

DO NOT create a complete ERP clone.

The website already handles advanced school-management functionality.

Your job is to build a secure, lightweight, professional mobile companion app.

Prioritize:

```text
Security
Performance
Simple UX
Role-based access
Daily-use features
API correctness
Maintainability
Android-first development
iOS-compatible shared architecture
```

Start by inspecting the existing project and then implement:

```text
Authentication
→ Session
→ Role Detection
→ Role Navigation
→ Dashboard
```

Do not proceed to advanced modules until this foundation builds successfully.

Always preserve the existing working project configuration unless a change is technically required.
