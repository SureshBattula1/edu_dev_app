package com.example.myeduapp.features.teacher.leaves

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.myeduapp.features.leaves.LeaveHubScreenContent

/** @deprecated Use LeaveHubScreen directly. Kept for existing navigation references. */
class TeacherLeaveScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        LeaveHubScreenContent(
            onBack = { navigator.pop() },
            onNavigate = { navigator.push(it) }
        )
    }
}
