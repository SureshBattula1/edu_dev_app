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
        return listOf(
            NavItem("Home", Icons.Default.Dashboard, Route.Dashboard),
            NavItem("Attendance", Icons.Default.CalendarToday, Route.Attendance),
            NavItem("Exams", Icons.Default.Quiz, Route.Exams),
            NavItem("Notices", Icons.Default.Campaign, Route.Notices)
        )
    }

    fun getDrawerItems(role: UserRole): List<NavItem> {
        val items = mutableListOf<NavItem>()
        items.add(NavItem("Dashboard", Icons.Default.Dashboard, Route.Dashboard))
        
        when (role) {
            UserRole.TEACHER -> {
                items.add(NavItem("My Classes", Icons.Default.Class, Route.MyClasses))
                items.add(NavItem("My Students", Icons.Default.Group, Route.MyStudents))
                items.add(NavItem("Attendance", Icons.Default.CheckCircle, Route.Attendance))
                items.add(NavItem("Assignments", Icons.Default.Assignment, Route.Assignments))
                items.add(NavItem("Exams", Icons.Default.Quiz, Route.Exams))
                items.add(NavItem("Marks", Icons.Default.Grade, Route.Marks))
                items.add(NavItem("Timetable", Icons.Default.Schedule, Route.Timetable))
                items.add(NavItem("My Leaves", Icons.Default.EventNote, Route.Leaves))
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
        items.add(NavItem("Profile", Icons.Default.Person, Route.Profile))
        
        return items
    }

    fun defaultHomeRoute(role: UserRole): Route {
        return Route.Dashboard
    }
}
