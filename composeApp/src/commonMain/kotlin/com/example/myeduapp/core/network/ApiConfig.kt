package com.example.myeduapp.core.network

object ApiConfig {
    // For Android Emulator use 10.0.2.2
    // For iOS and physical devices, use your computer's LAN IP
    private const val BASE_URL_EMULATOR = "http://10.0.2.2:8000/api/"
    private const val BASE_URL_LOCAL = "http://localhost:8000/api/"
    
    val BASE_URL: String = BASE_URL_EMULATOR // Default to emulator for now
    
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val LOGOUT = "logout"
    const val ME = "me"
    const val DASHBOARD = "dashboard"
    const val UPDATE_PROFILE = "profile"
    const val CHANGE_PASSWORD = "change-password"
    
    // Feature Routes
    const val ATTENDANCE_STUDENT = "attendance/student"
    const val ATTENDANCE_CLASS = "attendance/class"
    const val ATTENDANCE_SUBMIT = "attendance/submit"
    const val STUDENTS = "students"
    const val CLASSES = "classes"
    const val BRANCH_CLASSES = "branch-classes"
    const val ASSIGNMENTS = "assignments"
    const val EXAMS_UPCOMING = "dashboard/upcoming-exams"
    const val EXAMS_RESULTS = "dashboard/student-results"
    const val FEE_DUES = "fee-dues/student"
    const val FEE_PAYMENTS = "students" // students/{id}/fees
    const val LEAVES = "leaves"
    const val COMMUNICATIONS = "communications"
    const val HOLIDAYS = "holidays/upcoming"
    const val TIMETABLES = "timetables/class"
}
