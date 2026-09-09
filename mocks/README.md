# MyEduApp Mock APIs

Use these when the Laravel backend is unavailable.

## Enable / disable

In `composeApp/.../core/network/ApiConfig.kt`:

```kotlin
const val USE_MOCKS: Boolean = true   // mock data (no backend)
const val USE_MOCKS: Boolean = false  // real Laravel API
```

Runtime payloads live in `MockResponses.kt` (must stay in sync with the JSON files in this folder).

## Test login

Any email/password works while mocks are on. Sample:

- email: `teacher@school.com`
- password: `Password@123`

## Covered endpoints

| Method | Path |
| :--- | :--- |
| POST | login, register, logout, leaves, attendance/submit |
| GET | me, dashboard, students, classes, assignments |
| GET | dashboard/upcoming-exams, dashboard/student-results |
| GET | attendance/student/{id}, attendance/student/{id}/overview, attendance/class/{id} |
| GET | fee-dues/student/{id}, students/{id}/fees, leaves/student/{id} |
| GET | communications/notifications, communications/announcements, holidays/upcoming |
| GET | timetables/class/{grade}/{section} |
| PUT | profile, change-password |
