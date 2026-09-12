package com.example.myeduapp.features.notifications

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.myeduapp.core.datastore.SessionManager
import com.example.myeduapp.core.ui.components.AppLoaderCompact
import com.example.myeduapp.core.ui.components.NetworkAvatar
import com.example.myeduapp.core.ui.theme.OutlineSoft
import com.example.myeduapp.core.ui.theme.PrimaryBlue
import com.example.myeduapp.core.ui.theme.PrimaryText
import com.example.myeduapp.core.ui.theme.SecondaryText
import com.example.myeduapp.core.ui.theme.SuccessColor
import com.example.myeduapp.core.util.MediaUrlResolver
import com.example.myeduapp.data.model.AssignmentAttachment
import com.example.myeduapp.data.model.Notification
import com.example.myeduapp.data.model.NotificationReceipts
import com.example.myeduapp.data.model.UserRole
import com.example.myeduapp.data.repository.CommunicationRepository

@Composable
fun NotificationDetailDialog(
    item: Notification?,
    onDismiss: () -> Unit
) {
    item ?: return
    val uriHandler = LocalUriHandler.current
    val repository = remember { CommunicationRepository() }
    val colorScheme = MaterialTheme.colorScheme
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
            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
            shape = RoundedCornerShape(28.dp),
            color = colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 560.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(44.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = colorScheme.primary.copy(alpha = 0.1f)
                        ) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = null,
                                tint = colorScheme.primary,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                item.title,
                                style = MaterialTheme.typography.titleLarge,
                                color = colorScheme.onSurface,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 24.sp
                            )
                            val meta = listOfNotNull(sourceLabel, item.type.takeIf { it.isNotBlank() }, eventLabel)
                                .joinToString("  ·  ")
                            if (meta.isNotBlank()) {
                                Text(meta, style = MaterialTheme.typography.labelMedium, color = colorScheme.primary)
                            }
                        }
                    }

                    HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.6f))

                    // Description
                    val descriptionText = item.description?.trim().orEmpty().ifBlank { item.message }
                    DetailBlock(label = "Message Details") {
                        Text(
                            descriptionText,
                            style = MaterialTheme.typography.bodyLarge,
                            color = colorScheme.onSurface,
                            lineHeight = 22.sp
                        )
                    }

                    // Optional Description
                    val optionalText = item.optional_description?.trim().orEmpty()
                    if (optionalText.isNotBlank()) {
                        DetailBlock(label = "Additional Information") {
                            Text(
                                optionalText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorScheme.onSurface,
                                lineHeight = 20.sp
                            )
                        }
                    }

                    // Date
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp), tint = colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(item.displayDate.ifBlank { "—" }, style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                    }

                    // Attachments with Preview
                    DetailBlock(label = "Attachments") {
                        if (item.attachments.isEmpty()) {
                            Text("No files attached", style = MaterialTheme.typography.bodyMedium, color = colorScheme.onSurfaceVariant)
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                item.attachments.forEach { file ->
                                    val isImage = file.file_type?.contains("image", true) == true || 
                                                 listOf("jpg", "jpeg", "png", "webp").any { file.file_path.lowercase().endsWith(it) }
                                    
                                    AttachmentCard(
                                        file = file, 
                                        isImage = isImage,
                                        onClick = {
                                            MediaUrlResolver.resolve(file.file_url ?: file.file_path)?.let { url ->
                                                runCatching { uriHandler.openUri(url) }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }

                    if (showViews) {
                        DetailBlock(label = "Recipient Views") {
                            when {
                                loadingViews -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                    AppLoaderCompact(size = 24.dp)
                                }
                                receipts != null -> {
                                    val data = receipts!!
                                    Text(
                                        "${data.viewed} of ${data.total} viewed · ${data.pending} pending",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LinearProgressIndicator(
                                        progress = { if (data.total == 0) 0f else data.viewed / data.total.toFloat() },
                                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                                        color = colorScheme.primary,
                                        trackColor = colorScheme.outlineVariant
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    data.viewers.take(40).forEach { viewer ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                if (viewer.viewed) Icons.Default.CheckCircle else Icons.Default.Visibility,
                                                contentDescription = null,
                                                tint = if (viewer.viewed) SuccessColor else colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    viewer.name.ifBlank { "Member" },
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = colorScheme.onSurface
                                                )
                                                val sub = listOfNotNull(viewer.role, viewer.audience).joinToString(" · ")
                                                if (sub.isNotBlank()) {
                                                    Text(sub, style = MaterialTheme.typography.labelSmall, color = colorScheme.onSurfaceVariant)
                                                }
                                            }
                                            Text(
                                                if (viewer.viewed) "Viewed" else "Pending",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (viewer.viewed) SuccessColor else colorScheme.onSurfaceVariant,
                                                textAlign = TextAlign.End
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.5f))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("DISMISS", style = MaterialTheme.typography.titleMedium, color = colorScheme.primary)
                    }
                }
            }
        }
    }
}


@Composable
private fun AttachmentCard(
    file: AssignmentAttachment,
    isImage: Boolean,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.8f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            if (isImage) {
                NetworkAvatar(
                    url = file.file_url ?: file.file_path,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentDescription = file.displayName,
                    placeholder = {
                        Box(Modifier.fillMaxSize().background(colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.InsertDriveFile, contentDescription = null, tint = colorScheme.onSurfaceVariant)
                        }
                    }
                )
            }
            
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (isImage) Icons.Default.Image else Icons.Default.InsertDriveFile,
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        file.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    file.file_size?.let {
                        Text("${(it / 1024).coerceAtLeast(1)} KB", style = MaterialTheme.typography.labelSmall, color = colorScheme.onSurfaceVariant)
                    }
                }
                Icon(
                    Icons.Default.AttachFile,
                    contentDescription = null,
                    tint = colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun DetailBlock(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.8.sp,
            fontWeight = FontWeight.Bold
        )
        content()
    }
}
