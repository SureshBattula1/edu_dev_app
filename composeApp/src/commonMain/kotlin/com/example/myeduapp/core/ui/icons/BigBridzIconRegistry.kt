package com.example.myeduapp.core.ui.icons

import com.example.myeduapp.data.model.UserRole

/**
 * Single source of truth registry for discovery and mapping of BigBridz 3D Icons.
 * Scatter mappings must never exist throughout individual screens.
 */
object BigBridzIconRegistry {

    // Direct Accessors
    val Dashboard: BigBridzIcon get() = BigBridzIcon.Dashboard
    val Students: BigBridzIcon get() = BigBridzIcon.Students
    val Teachers: BigBridzIcon get() = BigBridzIcon.Teachers
    val Classes: BigBridzIcon get() = BigBridzIcon.Classes
    val Attendance: BigBridzIcon get() = BigBridzIcon.Attendance
    val Assignments: BigBridzIcon get() = BigBridzIcon.Assignments
    val Submissions: BigBridzIcon get() = BigBridzIcon.Submissions
    val Exams: BigBridzIcon get() = BigBridzIcon.Exams
    val Marks: BigBridzIcon get() = BigBridzIcon.Marks
    val Performance: BigBridzIcon get() = BigBridzIcon.Performance
    val Timetable: BigBridzIcon get() = BigBridzIcon.Timetable
    val Notices: BigBridzIcon get() = BigBridzIcon.Notices
    val Communication: BigBridzIcon get() = BigBridzIcon.Communication
    val Fees: BigBridzIcon get() = BigBridzIcon.Fees
    val Payments: BigBridzIcon get() = BigBridzIcon.Payments
    val Reports: BigBridzIcon get() = BigBridzIcon.Reports
    val Library: BigBridzIcon get() = BigBridzIcon.Library
    val Profile: BigBridzIcon get() = BigBridzIcon.Profile
    val Settings: BigBridzIcon get() = BigBridzIcon.Settings
    val Notifications: BigBridzIcon get() = BigBridzIcon.Notifications
    val Parents: BigBridzIcon get() = BigBridzIcon.Parents
    val Staff: BigBridzIcon get() = BigBridzIcon.Staff
    val Branches: BigBridzIcon get() = BigBridzIcon.Branches
    val Users: BigBridzIcon get() = BigBridzIcon.Users
    val Roles: BigBridzIcon get() = BigBridzIcon.Roles
    val AuditLogs: BigBridzIcon get() = BigBridzIcon.AuditLogs

    // Route / Path Mapping
    fun forRoute(routePath: String): BigBridzIcon {
        return when (routePath.lowercase()) {
            "dashboard" -> Dashboard
            "branches" -> Branches
            "students", "my_students" -> Students
            "teachers", "my_teachers" -> Teachers
            "classes", "my_classes" -> Classes
            "attendance", "my_attendance" -> Attendance
            "assignments", "my_assignments" -> Assignments
            "submissions" -> Submissions
            "exams", "my_exams" -> Exams
            "marks", "results", "my_results" -> Marks
            "performance", "student_performance" -> Performance
            "timetable", "my_timetable" -> Timetable
            "notices" -> Notices
            "communication" -> Communication
            "fees" -> Fees
            "payments", "receipts" -> Payments
            "reports", "financial_reports" -> Reports
            "library" -> Library
            "profile", "my_profile", "student360" -> Profile
            "settings" -> Settings
            "notifications" -> Notifications
            "parents" -> Parents
            "staff" -> Staff
            "users" -> Users
            "roles" -> Roles
            "audit_logs" -> AuditLogs
            else -> Dashboard
        }
    }

    // Role-Based Module Icons List
    fun modulesForRole(role: UserRole): List<BigBridzModuleInfo> {
        return when (role) {
            UserRole.SUPER_ADMIN -> listOf(
                BigBridzModuleInfo("Branches", Branches, "branches"),
                BigBridzModuleInfo("Students", Students, "students"),
                BigBridzModuleInfo("Teachers", Teachers, "teachers"),
                BigBridzModuleInfo("Attendance", Attendance, "attendance"),
                BigBridzModuleInfo("Assignments", Assignments, "assignments"),
                BigBridzModuleInfo("Exams", Exams, "exams"),
                BigBridzModuleInfo("Fees", Fees, "fees"),
                BigBridzModuleInfo("Timetable", Timetable, "timetable"),
                BigBridzModuleInfo("Notices", Notices, "notices")
            )
            UserRole.BRANCH_ADMIN -> listOf(
                BigBridzModuleInfo("Students", Students, "students"),
                BigBridzModuleInfo("Teachers", Teachers, "teachers"),
                BigBridzModuleInfo("Attendance", Attendance, "attendance"),
                BigBridzModuleInfo("Assignments", Assignments, "assignments"),
                BigBridzModuleInfo("Exams", Exams, "exams"),
                BigBridzModuleInfo("Fees", Fees, "fees"),
                BigBridzModuleInfo("Timetable", Timetable, "timetable"),
                BigBridzModuleInfo("Notices", Notices, "notices")
            )
            UserRole.TEACHER -> listOf(
                BigBridzModuleInfo("My Classes", Classes, "classes"),
                BigBridzModuleInfo("My Students", Students, "students"),
                BigBridzModuleInfo("Attendance", Attendance, "attendance"),
                BigBridzModuleInfo("Assignments", Assignments, "assignments"),
                BigBridzModuleInfo("Exams", Exams, "exams"),
                BigBridzModuleInfo("Marks", Marks, "marks"),
                BigBridzModuleInfo("Timetable", Timetable, "timetable"),
                BigBridzModuleInfo("Communication", Communication, "notices")
            )
            UserRole.STUDENT -> listOf(
                BigBridzModuleInfo("My Attendance", Attendance, "attendance"),
                BigBridzModuleInfo("My Assignments", Assignments, "assignments"),
                BigBridzModuleInfo("My Exams", Exams, "exams"),
                BigBridzModuleInfo("My Results", Marks, "exams"),
                BigBridzModuleInfo("My Timetable", Timetable, "timetable"),
                BigBridzModuleInfo("Fees & Dues", Fees, "fees")
            )
            UserRole.PARENT -> listOf(
                BigBridzModuleInfo("Children", Students, "students"),
                BigBridzModuleInfo("Attendance", Attendance, "attendance"),
                BigBridzModuleInfo("Results", Marks, "exams"),
                BigBridzModuleInfo("Fees", Fees, "fees"),
                BigBridzModuleInfo("Notices", Notices, "notices")
            )
            UserRole.STAFF -> listOf(
                BigBridzModuleInfo("Attendance", Attendance, "attendance"),
                BigBridzModuleInfo("Leaves", Reports, "leaves"),
                BigBridzModuleInfo("Notices", Notices, "notices"),
                BigBridzModuleInfo("Notifications", Notifications, "notifications")
            )
            UserRole.ACCOUNTANT -> listOf(
                BigBridzModuleInfo("Fees", Fees, "fees"),
                BigBridzModuleInfo("Payments", Payments, "fees"),
                BigBridzModuleInfo("Receipts", Reports, "fees"),
                BigBridzModuleInfo("Financial Reports", Reports, "reports")
            )
        }
    }
}

data class BigBridzModuleInfo(
    val title: String,
    val icon: BigBridzIcon,
    val routePath: String
)
