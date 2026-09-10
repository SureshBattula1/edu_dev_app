package com.example.myeduapp.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.myeduapp.data.model.UserRole

data class NavItem(
    val label: String,
    val icon: ImageVector,
    val route: Route,
    val requiredPermission: String? = null
)

object AppNavigation {
    
    fun getBottomNavItems(role: UserRole): List<NavItem> {
        val items = mutableListOf<NavItem>()
        
        when (role) {
            UserRole.SUPER_ADMIN -> {
                items.add(NavItem("Branches", Icons.Default.Business, Route.Dashboard))
                items.add(NavItem("Students", Icons.Default.Group, Route.Dashboard))
                items.add(NavItem("Reports", Icons.Default.Assessment, Route.Dashboard))
            }
            UserRole.BRANCH_ADMIN -> {
                items.add(NavItem("Students", Icons.Default.Group, Route.Dashboard))
                items.add(NavItem("Fees", Icons.Default.Payments, Route.Fees))
            }
            UserRole.TEACHER -> {
                items.add(NavItem("Home", Icons.Default.Dashboard, Route.Dashboard))
                items.add(NavItem("Attendance", Icons.Default.CheckCircle, Route.Attendance))
                items.add(NavItem("Exams", Icons.Default.Quiz, Route.Exams))
            }
            UserRole.STAFF -> {
                items.add(NavItem("Students", Icons.Default.Group, Route.Dashboard))
                items.add(NavItem("Leaves", Icons.Default.EventNote, Route.Leaves))
            }
            UserRole.ACCOUNTANT -> {
                items.add(NavItem("Fees", Icons.Default.Payments, Route.Fees))
                items.add(NavItem("Payments", Icons.Default.Receipt, Route.Fees))
            }
            UserRole.STUDENT -> {
                items.add(NavItem("Home", Icons.Default.Dashboard, Route.Dashboard))
                items.add(NavItem("Attendance", Icons.Default.CheckCircle, Route.Attendance))
                items.add(NavItem("Fees", Icons.Default.Payments, Route.Fees))
                items.add(NavItem("Results", Icons.Default.Grade, Route.Exams))
            }
            UserRole.PARENT -> {
                items.add(NavItem("Children", Icons.Default.ChildCare, Route.Dashboard))
                items.add(NavItem("Fees", Icons.Default.Payments, Route.Fees))
                items.add(NavItem("Notices", Icons.Default.Campaign, Route.Notices))
            }
        }
        
        return items
    }

    fun getDrawerItems(role: UserRole): List<NavItem> {
        val items = mutableListOf<NavItem>()
        items.add(NavItem("Dashboard", Icons.Default.Dashboard, Route.Dashboard))
        
        when (role) {
            UserRole.TEACHER -> {
                items.add(NavItem("My Students", Icons.Default.Group, Route.MyStudents))
                items.add(NavItem("Attendance", Icons.Default.CheckCircle, Route.Attendance))
                items.add(NavItem("Assignments", Icons.Default.Assignment, Route.Assignments))
                items.add(NavItem("Exams", Icons.Default.Quiz, Route.Exams))
                items.add(NavItem("Marks", Icons.Default.Grade, Route.Marks))
                items.add(NavItem("Timetable", Icons.Default.Schedule, Route.Timetable))
                items.add(NavItem("My Leaves", Icons.Default.EventNote, Route.Leaves))
            }
            UserRole.STUDENT -> {
                items.add(NavItem("My Profile", Icons.Default.Person, Route.Student360))
                items.add(NavItem("My Attendance", Icons.Default.CheckCircle, Route.Attendance))
                items.add(NavItem("My Leaves", Icons.Default.EventNote, Route.Leaves))
            }
            UserRole.STAFF -> {
                items.add(NavItem("Leaves", Icons.Default.EventNote, Route.Leaves))
            }
            UserRole.BRANCH_ADMIN, UserRole.SUPER_ADMIN -> {
                items.add(NavItem("Students", Icons.Default.Group, Route.Dashboard))
                items.add(NavItem("Attendance", Icons.Default.CalendarToday, Route.Attendance))
                items.add(NavItem("Leaves", Icons.Default.EventNote, Route.Leaves))
                items.add(NavItem("Fees", Icons.Default.Payments, Route.Fees))
                items.add(NavItem("Exams", Icons.Default.Quiz, Route.Exams))
                items.add(NavItem("Timetable", Icons.Default.Schedule, Route.Timetable))
            }
            else -> {
                items.add(NavItem("Students", Icons.Default.Group, Route.Dashboard))
                items.add(NavItem("Attendance", Icons.Default.CalendarToday, Route.Attendance))
                items.add(NavItem("Fees", Icons.Default.Payments, Route.Fees))
                items.add(NavItem("Exams", Icons.Default.Quiz, Route.Exams))
                items.add(NavItem("Timetable", Icons.Default.Schedule, Route.Timetable))
            }
        }
        
        items.add(NavItem("Notices", Icons.Default.Campaign, Route.Notices))
        
        return items
    }

    fun defaultHomeRoute(role: UserRole): Route {
        return Route.Dashboard
    }
}
