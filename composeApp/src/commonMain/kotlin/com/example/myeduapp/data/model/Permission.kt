package com.example.myeduapp.data.model

object Permission {
    // Dashboard
    const val DASHBOARD_VIEW = "dashboard.view"

    // Students
    const val STUDENTS_VIEW = "students.view"
    const val STUDENTS_CREATE = "students.create"
    const val STUDENTS_EDIT = "students.edit"
    const val STUDENTS_DELETE = "students.delete"
    const val STUDENTS_EXPORT = "students.export"
    const val STUDENTS_PROMOTE = "students.promote"

    // Teachers
    const val TEACHERS_VIEW = "teachers.view"
    const val TEACHERS_CREATE = "teachers.create"
    const val TEACHERS_EDIT = "teachers.edit"
    const val TEACHERS_DELETE = "teachers.delete"

    // Attendance
    const val ATTENDANCE_VIEW = "student_attendance.view"
    const val ATTENDANCE_MARK = "student_attendance.mark"
    const val ATTENDANCE_REPORT = "student_attendance.report"
    
    // Fees
    const val FEES_VIEW = "fees.view"
    const val FEES_COLLECT = "fees.collect"
    const val FEES_REPORT = "fees.report"

    // Exams
    const val EXAMS_VIEW = "exams.view"
    const val EXAMS_RESULTS = "exams.results"
    
    // Leaves
    const val LEAVES_VIEW = "leaves.view"
    const val LEAVES_CREATE = "leaves.create"
    const val LEAVES_APPROVE = "leaves.approve"
    const val LEAVES_REJECT = "leaves.reject"

    // Communication
    const val NOTICES_VIEW = "communications.notices"
    const val NOTICES_CREATE = "communications.notices.create"

    // System
    const val SEARCH_GLOBAL = "search.global"
    const val SETTINGS_VIEW = "settings.view"
    const val ROLES_VIEW = "roles.view"
    const val USERS_VIEW = "users.view"
}
