package com.example.myeduapp.features.notifications

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.myeduapp.core.datastore.SessionManager
import com.example.myeduapp.core.ui.components.AppLoaderCompact
import com.example.myeduapp.core.ui.theme.OutlineSoft
import com.example.myeduapp.core.ui.theme.PrimaryBlue
import com.example.myeduapp.core.ui.theme.PrimaryText
import com.example.myeduapp.core.ui.theme.SecondaryText
import com.example.myeduapp.core.ui.theme.SuccessColor
import com.example.myeduapp.core.util.MediaUrlResolver
import com.example.myeduapp.data.model.Notification
import com.example.myeduapp.data.model.NotificationReceipts
import com.example.myeduapp.data.model.UserRole
import com.example.myeduapp.data.repository.CommunicationRepository

private val ModalWhite = Color.White

@Composable
fun NotificationDetailDialog(
    item: Notification?,
    onDismiss: () -> Unit
) {
    item ?: return
    val uriHandler = LocalUriHandler.current
    val repository = remember { CommunicationRepository() }
    val staffRoles = listOf(UserRole.TEACHER, UserRole.BRANCH_ADMIN, UserRole.SUPER_ADMIN, UserRole.STAFF)
    val showViews = SessionManager.user?.userRole in staffRoles &&
        item.can_view_receipts &&
        !item.group_key.isNullOrBlank()
    var receipts by remember(item.id) { mutableStateOf<NotificationReceipts?>(null) }
    var loadingViews by remember(item.id) { mutableStateOf(false) }

    LaunchedEffect(item.id, item.group_key, showViews) {
        if (!showViews) return@LaunchedEffect
        loadingViews = true
        repository.getNotificationReceipts(item.group_key!!)
            .onSuccess { receipts = it }
        loadingViews = false
    }

    val sourceLabel = item.source?.takeIf { it.isNotBlank() }?.replaceFirstChar { it.uppercase() }
    val eventLabel = item.event?.takeIf { it.isNotBlank() }?.replaceFirstChar { it.uppercase() }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = ModalWhite,
            shadowElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth().background(ModalWhite)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ModalWhite)
                        .heightIn(max = 460.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(36.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = PrimaryBlue.copy(alpha = 0.1f)
                        ) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                item.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = PrimaryText,
                                lineHeight = 20.sp
                            )
                            val meta = listOfNotNull(sourceLabel, item.type.takeIf { it.isNotBlank() }, eventLabel)
                                .joinToString("  ·  ")
                            if (meta.isNotBlank()) {
                                Text(meta, fontSize = 11.sp, color = PrimaryBlue, fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    HorizontalDivider(color = OutlineSoft)

                    val descriptionText = item.description?.trim().orEmpty().ifBlank { item.message }
                    DetailBlock(label = "Description") {
                        Text(
                            descriptionText,
                            fontSize = 13.sp,
                            color = PrimaryText,
                            lineHeight = 18.sp
                        )
                    }

                    val optionalText = item.optional_description?.trim().orEmpty()
                    if (optionalText.isNotBlank()) {
                        DetailBlock(label = "Optional description") {
                            Text(
                                optionalText,
                                fontSize = 13.sp,
                                color = PrimaryText,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(14.dp), tint = SecondaryText)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(item.displayDate.ifBlank { "—" }, fontSize = 12.sp, color = SecondaryText)
                    }

                    DetailBlock(label = "Attachments (optional)") {
                        if (item.attachments.isEmpty()) {
                            Text("No files attached", fontSize = 12.sp, color = SecondaryText)
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                item.attachments.forEach { file ->
                                    Surface(
                                        onClick = {
                                            MediaUrlResolver.resolve(file.file_url ?: file.file_path)?.let { url ->
                                                runCatching { uriHandler.openUri(url) }
                                            }
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        color = ModalWhite,
                                        border = BorderStroke(1.dp, OutlineSoft),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.InsertDriveFile,
                                                contentDescription = null,
                                                tint = PrimaryBlue,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    file.displayName,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = PrimaryText,
                                                    maxLines = 1
                                                )
                                                file.file_size?.let {
                                                    Text("${(it / 1024).coerceAtLeast(1)} KB", fontSize = 10.sp, color = SecondaryText)
                                                }
                                            }
                                            Icon(
                                                Icons.Default.AttachFile,
                                                contentDescription = null,
                                                tint = SecondaryText,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (showViews) {
                        DetailBlock(label = "Views") {
                            when {
                                loadingViews -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                    AppLoaderCompact(size = 22.dp)
                                }
                                receipts != null -> {
                                    val data = receipts!!
                                    Text(
                                        "${data.viewed} of ${data.total} viewed · ${data.pending} pending",
                                        fontSize = 12.sp,
                                        color = PrimaryText
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    LinearProgressIndicator(
                                        progress = if (data.total == 0) 0f else data.viewed / data.total.toFloat(),
                                        modifier = Modifier.fillMaxWidth().height(5.dp),
                                        color = PrimaryBlue,
                                        trackColor = OutlineSoft
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    data.viewers.take(40).forEach { viewer ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                if (viewer.viewed) Icons.Default.CheckCircle else Icons.Default.Visibility,
                                                contentDescription = null,
                                                tint = if (viewer.viewed) SuccessColor else SecondaryText,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    viewer.name.ifBlank { "Member" },
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = PrimaryText
                                                )
                                                val sub = listOfNotNull(viewer.role, viewer.audience).joinToString(" · ")
                                                if (sub.isNotBlank()) {
                                                    Text(sub, fontSize = 10.sp, color = SecondaryText)
                                                }
                                            }
                                            Text(
                                                if (viewer.viewed) "Viewed" else "Pending",
                                                fontSize = 10.sp,
                                                color = if (viewer.viewed) SuccessColor else SecondaryText,
                                                textAlign = TextAlign.End
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = OutlineSoft)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ModalWhite)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Close", color = PrimaryBlue, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailBlock(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            label.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = SecondaryText,
            letterSpacing = 0.6.sp
        )
        content()
    }
}
