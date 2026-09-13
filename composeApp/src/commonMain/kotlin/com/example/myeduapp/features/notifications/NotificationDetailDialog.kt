package com.example.myeduapp.features.notifications

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import cafe.adriel.voyager.navigator.LocalNavigator
import com.example.myeduapp.core.datastore.SessionManager
import com.example.myeduapp.core.ui.components.AppLoaderCompact
import com.example.myeduapp.core.ui.components.NetworkAvatar
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
    onDismiss: () -> Unit,
    onMarkRead: ((String) -> Unit)? = null
) {
    item ?: return
    val navigator = LocalNavigator.current
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
        val key = item.group_key ?: return@LaunchedEffect
        loadingViews = true
        repository.getNotificationReceipts(key)
            .onSuccess { receipts = it }
        loadingViews = false
    }

    val sourceLabel = item.source?.takeIf { it.isNotBlank() }?.replaceFirstChar { it.uppercase() }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 24.dp)
                .widthIn(max = 500.dp),
            shape = RoundedCornerShape(28.dp),
            color = colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // HEADER
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 12.dp, top = 16.dp, bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = colorScheme.primary.copy(alpha = 0.1f)
                        ) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = null,
                                tint = colorScheme.primary,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = item.type.ifBlank { "Notification" }.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.primary,
                                letterSpacing = 0.8.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (sourceLabel != null) {
                                Text(
                                    text = "From $sourceLabel",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.5f))

                // SCROLLABLE CONTENT
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 500.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // TITLE & MESSAGE
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 26.sp
                        )
                        
                        Text(
                            text = item.description?.trim()?.ifBlank { item.message } ?: item.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = colorScheme.onSurface,
                            lineHeight = 22.sp
                        )
                    }

                    // ADDITIONAL INFO
                    val optionalText = item.optional_description?.trim().orEmpty()
                    if (optionalText.isNotBlank()) {
                        DetailBlock(label = "Additional Info") {
                            Text(
                                optionalText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorScheme.onSurfaceVariant,
                                lineHeight = 20.sp
                            )
                        }
                    }

                    // DATE
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Schedule, 
                            contentDescription = null, 
                            modifier = Modifier.size(16.dp), 
                            tint = colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = item.displayDate.ifBlank { "—" }, 
                            style = MaterialTheme.typography.bodySmall, 
                            color = colorScheme.onSurfaceVariant
                        )
                    }

                    // ATTACHMENTS
                    if (item.attachments.isNotEmpty()) {
                        DetailBlock(label = "Attachments") {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                item.attachments.forEach { file ->
                                    val isImage = file.file_type?.contains("image", true) == true || 
                                                 listOf("jpg", "jpeg", "png", "webp").any { file.file_path.lowercase().endsWith(it) }
                                    
                                    AttachmentCard(
                                        file = file, 
                                        isImage = isImage,
                                        onClick = {
                                            val resolvedUrl = MediaUrlResolver.resolve(file.file_url ?: file.file_path)
                                            if (resolvedUrl != null) {
                                                if (navigator != null) {
                                                    onDismiss()
                                                    navigator.push(FilePreviewScreen(item.title, resolvedUrl, file.displayName))
                                                } else {
                                                    runCatching { uriHandler.openUri(resolvedUrl) }
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // VIEW TRACKING (STAFF ONLY)
                    if (showViews) {
                        DetailBlock(label = "Delivery Status") {
                            when {
                                loadingViews -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                    AppLoaderCompact(size = 24.dp)
                                }
                                receipts != null -> {
                                    val data = receipts!!
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            "${data.viewed} of ${data.total} recipients viewed",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = colorScheme.onSurface,
                                            fontWeight = FontWeight.Medium
                                        )
                                        LinearProgressIndicator(
                                            progress = { if (data.total == 0) 0f else data.viewed / data.total.toFloat() },
                                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                                            color = colorScheme.primary,
                                            trackColor = colorScheme.outlineVariant
                                        )
                                        
                                        data.viewers.take(20).forEach { viewer ->
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
                                                        viewer.name.ifBlank { "Student/Parent" },
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = colorScheme.onSurface
                                                    )
                                                    if (!viewer.role.isNullOrBlank()) {
                                                        Text(viewer.role!!, style = MaterialTheme.typography.labelSmall, color = colorScheme.onSurfaceVariant)
                                                    }
                                                }
                                                if (viewer.viewed && !viewer.viewed_at.isNullOrBlank()) {
                                                    Text(
                                                        viewer.viewed_at!!.take(10),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                        if (data.total > 20) {
                                            Text(
                                                "+ ${data.total - 20} more recipients",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = colorScheme.onSurfaceVariant,
                                                modifier = Modifier.fillMaxWidth(),
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.5f))

                // FOOTER ACTIONS
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (item.action_url?.isNotBlank() == true) {
                        TextButton(
                            onClick = { runCatching { uriHandler.openUri(item.action_url!!) } },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("VIEW LINK", style = MaterialTheme.typography.labelLarge, color = colorScheme.primary)
                        }
                    }

                    if (!item.isRead) {
                        Button(
                            onClick = {
                                onMarkRead?.invoke(item.id)
                                onDismiss()
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("MARK AS READ", style = MaterialTheme.typography.labelLarge)
                        }
                    } else {
                        Button(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.surfaceVariant, contentColor = colorScheme.onSurfaceVariant)
                        ) {
                            Text("CLOSE", style = MaterialTheme.typography.labelLarge)
                        }
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
        color = colorScheme.surfaceVariant.copy(alpha = 0.2f),
        border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            if (isImage) {
                NetworkAvatar(
                    url = file.file_url ?: file.file_path,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentDescription = file.displayName,
                    placeholder = {
                        Box(Modifier.fillMaxSize().background(colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Image, contentDescription = null, tint = colorScheme.onSurfaceVariant)
                        }
                    }
                )
            }
            
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isImage) Icons.Default.Image else Icons.AutoMirrored.Filled.InsertDriveFile,
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = file.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    file.file_size?.let {
                        val sizeKb = (it / 1024).coerceAtLeast(1)
                        Text(
                            text = "$sizeKb KB", 
                            style = MaterialTheme.typography.labelSmall, 
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    Icons.Default.ArrowOutward,
                    contentDescription = "Open",
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
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.8.sp,
            fontWeight = FontWeight.Bold
        )
        content()
    }
}
