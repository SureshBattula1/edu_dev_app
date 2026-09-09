package com.example.myeduapp.core.navigation

sealed class Route(val path: String) {
    object Login : Route("login")
    object Register : Route("register")
    object Dashboard : Route("dashboard")
    object MyClasses : Route("my_classes")
    object MyStudents : Route("my_students")
    object Student360 : Route("student360")
    object Assignments : Route("assignments")
    object Marks : Route("marks")
    object Attendance : Route("attendance")
    object Fees : Route("fees")
    object Exams : Route("exams")
    object Leaves : Route("leaves")
    object Timetable : Route("timetable")
    object Notices : Route("notices")
    object Profile : Route("profile")
    object EditProfile : Route("edit_profile")
    object ChangePassword : Route("change_password")
}
