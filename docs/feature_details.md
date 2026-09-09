# Feature Details

This document provides a breakdown of each module available in MyEduApp, detailing functionality, technical implementation, and security requirements.

---

## 1. Authentication (Core)
- **Functionality:** Secure Login, Logout, Session Persistence, and Profile management.
- **Technical Working:** `AuthRepository` interacts with `/api/login` and `/api/logout`. Uses `SessionManager` to handle token lifecycle.
- **Requirements:**
    - Endpoints: `POST /api/login`, `POST /api/logout`, `GET /api/me`.
    - Inputs: Email/Phone, Password.
- **Role Access:** All Roles.

## 2. Dashboard
- **Functionality:** Role-specific summaries (e.g., Attendance stats for Students, Collection stats for Accountants).
- **Technical Working:** `DashboardRepository` fetches data from `/api/dashboard` and `/api/dashboard/stats`.
- **Requirements:**
    - Endpoints: `GET /api/dashboard`.
    - Permissions: Role-specific stats permissions.
- **Role Access:** Customized for all 6 roles.

## 3. Attendance
- **Functionality:** 
    - **Teachers:** Mark daily attendance for assigned classes.
    - **Students/Admins:** View monthly/daily attendance history and percentages.
- **Technical Working:** `AttendanceRepository` uses `GET /api/attendance` for history and `POST /api/attendance` for marking.
- **Requirements:**
    - Endpoints: `/api/attendance/student/{userId}`, `/api/attendance/mark`.
    - Permissions: `mark_attendance` (Teacher/Admin), `view_attendance` (Student/All).
- **Role Access:** Teacher, Student, Admin, Staff.

## 4. Student Management
- **Functionality:** Search students, view academic profiles, and basic contact info.
- **Technical Working:** `StudentRepository` fetches lists and details from `/api/students`.
- **Requirements:**
    - Endpoints: `GET /api/students`, `GET /api/students/{id}`.
    - Permissions: `view_students`.
- **Role Access:** Admin, Teacher, Staff, Accountant.

## 5. Fees & Finance
- **Functionality:** View pending dues, payment history, and total collections.
- **Technical Working:** `FeeRepository` calls `/api/fees` and `/api/fee-dues`.
- **Requirements:**
    - Endpoints: `GET /api/fee-dues/student/{userId}`, `GET /api/payments`.
    - Permissions: `view_fees`, `manage_fees` (Accountant).
- **Role Access:** Accountant, Admin, Student (own fees).

## 6. Exams & Results
- **Functionality:** View upcoming exam schedules and published academic results.
- **Technical Working:** `ExamRepository` fetches data from `/api/exams` and `/api/results`.
- **Requirements:**
    - Endpoints: `GET /api/results/student/{userId}`, `GET /api/exams/schedule`.
- **Role Access:** Student, Teacher, Admin.

## 7. Leave Management
- **Functionality:** Apply for leave, check application status, and view leave balance.
- **Technical Working:** `LeaveRepository` interacts with `/api/leaves`.
- **Requirements:**
    - Endpoints: `POST /api/leaves`, `GET /api/leaves/user/{userId}`.
- **Role Access:** Teacher, Student, Staff (All for own leaves).

## 8. Timetable
- **Functionality:** View daily and weekly class schedules.
- **Technical Working:** Fetches structured schedule data from `/api/timetable`.
- **Role Access:** Student, Teacher.

## 9. Communication (Notices & Events)
- **Functionality:** Announcements, circulars, holiday calendars, and school events.
- **Technical Working:** `CommunicationApi` fetches from `/api/notifications` and `/api/events`.
- **Role Access:** All Roles.

## 10. Profile & Settings
- **Functionality:** View personal details (Branch, Role, Contact), Change Password, and App Theme settings.
- **Technical Working:** Uses `AuthRepository` for password updates and `SessionManager` for profile data.
- **Role Access:** All Roles.
