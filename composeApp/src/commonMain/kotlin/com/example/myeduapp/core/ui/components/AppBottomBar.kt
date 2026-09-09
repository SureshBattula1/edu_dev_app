package com.example.myeduapp.core.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myeduapp.core.navigation.NavItem

@Composable
fun AppBottomBar(
    navItems: List<NavItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit
) {
    NavigationBar(
        modifier = Modifier.height(56.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary,
        windowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        navItems.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label, fontSize = 12.sp) },
                selected = selectedIndex == index,
                onClick = { onItemSelected(index) }
            )
        }
    }
}
