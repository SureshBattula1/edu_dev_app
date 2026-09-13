package com.example.myeduapp.core.ui.icons

import org.jetbrains.compose.resources.DrawableResource
import myeduapp.composeapp.generated.resources.*

/**
 * Centralized, sealed icon definitions for the BigBridz 3D Icon System.
 * Composables reference logical icons (e.g. BigBridzIcon.Students) and NEVER raw resource names.
 */
sealed class BigBridzIcon(val resource: DrawableResource, val defaultDescription: String) {

    // Major Domain Modules
    data object Dashboard : BigBridzIcon(Res.drawable.ic_3d_dashboard, "Dashboard")
    data object Students : BigBridzIcon(Res.drawable.ic_3d_students, "Students")
    data object Teachers : BigBridzIcon(Res.drawable.ic_3d_teachers, "Teachers")
    data object Classes : BigBridzIcon(Res.drawable.ic_3d_classes, "Classes")
    data object Attendance : BigBridzIcon(Res.drawable.ic_3d_attendance, "Attendance")
    data object Assignments : BigBridzIcon(Res.drawable.ic_3d_assignments, "Assignments")
    data object Submissions : BigBridzIcon(Res.drawable.ic_3d_submissions, "Submissions")
    data object Exams : BigBridzIcon(Res.drawable.ic_3d_exams, "Exams")
    data object Marks : BigBridzIcon(Res.drawable.ic_3d_marks, "Marks")
    data object Performance : BigBridzIcon(Res.drawable.ic_3d_performance, "Performance")
    data object Timetable : BigBridzIcon(Res.drawable.ic_3d_timetable, "Timetable")
    data object Notices : BigBridzIcon(Res.drawable.ic_3d_notices, "Notices")
    data object Communication : BigBridzIcon(Res.drawable.ic_3d_communication, "Communication")
    data object Fees : BigBridzIcon(Res.drawable.ic_3d_fees, "Fees")
    data object Payments : BigBridzIcon(Res.drawable.ic_3d_payments, "Payments")
    data object Reports : BigBridzIcon(Res.drawable.ic_3d_reports, "Reports")
    data object Library : BigBridzIcon(Res.drawable.ic_3d_library, "Library")
    data object Profile : BigBridzIcon(Res.drawable.ic_3d_profile, "Profile")
    data object Settings : BigBridzIcon(Res.drawable.ic_3d_settings, "Settings")
    data object Notifications : BigBridzIcon(Res.drawable.ic_3d_notifications, "Notifications")
    data object Parents : BigBridzIcon(Res.drawable.ic_3d_parents, "Parents")
    data object Staff : BigBridzIcon(Res.drawable.ic_3d_staff, "Staff")
    data object Branches : BigBridzIcon(Res.drawable.ic_3d_branches, "Branches")
    data object Users : BigBridzIcon(Res.drawable.ic_3d_users, "Users")
    data object Roles : BigBridzIcon(Res.drawable.ic_3d_roles, "Roles & Permissions")
    data object AuditLogs : BigBridzIcon(Res.drawable.ic_3d_audit_logs, "Audit Logs")

    // Empty & State Illustrations
    data object EmptyStudents : BigBridzIcon(Res.drawable.ic_3d_empty_students, "No Students Found")
    data object EmptyAssignments : BigBridzIcon(Res.drawable.ic_3d_empty_assignments, "No Assignments Found")
    data object EmptySubmissions : BigBridzIcon(Res.drawable.ic_3d_submissions, "No Submissions Found")
    data object EmptyAttendance : BigBridzIcon(Res.drawable.ic_3d_attendance, "No Attendance Records")
    data object EmptyExams : BigBridzIcon(Res.drawable.ic_3d_exams, "No Upcoming Exams")
    data object EmptyNotifications : BigBridzIcon(Res.drawable.ic_3d_notifications, "No Notifications")
    data object EmptyResults : BigBridzIcon(Res.drawable.ic_3d_marks, "No Results Available")
    data object NoInternet : BigBridzIcon(Res.drawable.ic_3d_no_internet, "No Internet Connection")
    data object Error : BigBridzIcon(Res.drawable.ic_3d_error, "Something Went Wrong")
    data object Success : BigBridzIcon(Res.drawable.ic_3d_success, "Action Successful")
    data object ComingSoon : BigBridzIcon(Res.drawable.ic_3d_coming_soon, "Feature Coming Soon")
}
