package com.example.myeduapp.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myeduapp.core.navigation.NavItem
import com.example.myeduapp.core.navigation.Route

@Composable
fun AppBottomBar(
    navItems: List<NavItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit
) {
    // Sunrise Academic: Floating white bottom navigation bar with 2 tabs (Home, Profile)
    // We filter items to only show Dashboard and Profile if requested, 
    // or just show the provided items in a floating container.
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp)
            .navigationBarsPadding(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .height(64.dp)
                .fillMaxWidth()
                .shadow(
                    elevation = 15.dp,
                    shape = RoundedCornerShape(32.dp),
                    ambientColor = Color(0xFF007CC4).copy(alpha = 0.25f),
                    spotColor = Color(0xFF007CC4).copy(alpha = 0.25f)
                ),
            shape = RoundedCornerShape(32.dp),
            color = Color.White,
            tonalElevation = 0.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                navItems.forEachIndexed { index, item ->
                    val isSelected = selectedIndex == index
                    val activeColor = MaterialTheme.colorScheme.primary
                    val inactiveColor = Color(0xFF64748B)

                    IconButton(
                        onClick = { onItemSelected(index) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (isSelected) item.icon else item.icon, // Ideally use outlined version
                                contentDescription = item.label,
                                tint = if (isSelected) activeColor else inactiveColor,
                                modifier = Modifier.size(24.dp)
                            )
                            if (isSelected) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .background(activeColor, RoundedCornerShape(50))
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
