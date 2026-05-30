package com.jitpomi.seyfr.ui.screens

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jitpomi.seyfr.AppUiState
import com.jitpomi.seyfr.TransferStatus
import com.jitpomi.seyfr.ui.components.FileRings
import com.jitpomi.seyfr.ui.components.FolderRings
import com.jitpomi.seyfr.ui.components.PrimaryButton
import com.jitpomi.seyfr.ui.components.QRCodeView
import com.jitpomi.seyfr.ui.components.SecondaryButton

@Composable
fun SendScreen(
    uiState: AppUiState,
    onSend: (String) -> Unit,
    onClearSend: () -> Unit,
    onCopyTicket: (String) -> Unit,
    onShareTicket: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isFolderMode by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val haptic = LocalHapticFeedback.current

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val path = getRealPathFromURI(context, it)
            path?.let(onSend)
        }
    }

    val folderPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            // Persist permission and resolve to real path
            context.contentResolver.takePersistableUriPermission(
                it,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            val path = getFolderPathFromURI(context, it)
            path?.let(onSend)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        if (uiState.sendStatus is TransferStatus.Idle && uiState.selectedFileName == null) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.clickable {
                        if (isFolderMode) {
                            folderPicker.launch(null)
                        } else {
                            filePicker.launch("*/*")
                        }
                    }
                ) {
                    AnimatedContent(
                        targetState = isFolderMode,
                        transitionSpec = {
                            (fadeIn() + scaleIn(initialScale = 0.92f)).togetherWith(fadeOut() + scaleOut(targetScale = 0.92f))
                        },
                        label = "ring_transition"
                    ) { folderMode ->
                        if (folderMode) {
                            FolderRings(isAnimating = uiState.sendStatus is TransferStatus.Sending)
                        } else {
                            FileRings(isAnimating = uiState.sendStatus is TransferStatus.Sending)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "File mode",
                        fontSize = 13.sp,
                        fontWeight = if (!isFolderMode) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (!isFolderMode) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Switch(
                        checked = isFolderMode,
                        onCheckedChange = { 
                            isFolderMode = it
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onSurface,
                            checkedTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            uncheckedTrackColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                    Text(
                        text = "Folder mode",
                        fontSize = 13.sp,
                        fontWeight = if (isFolderMode) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isFolderMode) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }



        AnimatedVisibility(
            visible = uiState.ticket.isNotEmpty(),
            enter = slideInVertically(animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            LaunchedEffect(uiState.ticket) {
                if (uiState.ticket.isNotEmpty()) {
                    delay(400)
                    val target = (scrollState.value + 400).coerceAtMost(scrollState.maxValue)
                    scrollState.animateScrollTo(target)
                }
            }
            Card(
                modifier = Modifier.padding(horizontal = 20.dp),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Transfer Ticket",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        IconButton(onClick = onClearSend) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    QRCodeView(ticket = uiState.ticket)

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Text(
                            text = uiState.ticket,
                            modifier = Modifier.padding(6.dp),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 11.sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            SecondaryButton(
                                onClick = { 
                                    onCopyTicket(uiState.ticket) 
                                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                }
                            ) {
                                Icon(imageVector = Icons.Outlined.ContentCopy, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Copy", fontSize = 15.sp)
                            }
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            PrimaryButton(
                                onClick = { onShareTicket(uiState.ticket) }
                            ) {
                                Icon(imageVector = Icons.Outlined.Share, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Share", fontSize = 15.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FileCard(
    fileName: String,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = fileName,
                modifier = Modifier.weight(1f),
                fontSize = 14.sp
            )
            if (isLoading) {
                Text(
                    text = "Sending...",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "✓ Completed",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private fun getRealPathFromURI(context: Context, uri: android.net.Uri): String? {
    return try {
        val fileName = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (index >= 0) cursor.getString(index) else null
            } else null
        } ?: uri.lastPathSegment ?: "unknown"

        // Copy content URI to app-private file so Rust can access it
        val destFile = java.io.File(context.cacheDir, fileName)
        context.contentResolver.openInputStream(uri)?.use { input ->
            destFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        destFile.absolutePath
    } catch (e: Exception) {
        android.util.Log.e("SendScreen", "Failed to copy file from URI", e)
        null
    }
}

private fun getFolderPathFromURI(context: Context, treeUri: android.net.Uri): String? {
    return try {
        val destDir = java.io.File(context.cacheDir, "picked_folder_${System.currentTimeMillis()}")
        destDir.mkdirs()

        val docUri = android.provider.DocumentsContract.buildDocumentUriUsingTree(
            treeUri,
            android.provider.DocumentsContract.getTreeDocumentId(treeUri)
        )
        copyDocumentTree(context, docUri, destDir)
        destDir.absolutePath
    } catch (e: Exception) {
        android.util.Log.e("SendScreen", "Failed to copy folder from URI", e)
        null
    }
}

private fun copyDocumentTree(context: Context, docUri: android.net.Uri, destDir: java.io.File) {
    if (android.provider.DocumentsContract.isDocumentUri(context, docUri)) {
        val mimeType = context.contentResolver.getType(docUri)
        if (mimeType == null || mimeType == android.provider.DocumentsContract.Document.MIME_TYPE_DIR) {
            // It's a directory - recurse
            val childrenUri = android.provider.DocumentsContract.buildChildDocumentsUriUsingTree(
                docUri,
                android.provider.DocumentsContract.getDocumentId(docUri)
            )
            context.contentResolver.query(childrenUri, null, null, null, null)?.use { cursor ->
                while (cursor.moveToNext()) {
                    val childDocId = cursor.getString(
                        cursor.getColumnIndexOrThrow(android.provider.DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                    )
                    val childUri = android.provider.DocumentsContract.buildDocumentUriUsingTree(docUri, childDocId)
                    copyDocumentTree(context, childUri, destDir)
                }
            }
        } else {
            // It's a file - copy it
            val name = context.contentResolver.query(docUri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    cursor.getString(cursor.getColumnIndexOrThrow(android.provider.DocumentsContract.Document.COLUMN_DISPLAY_NAME))
                } else null
            } ?: docUri.lastPathSegment ?: "file"

            val destFile = java.io.File(destDir, name)
            context.contentResolver.openInputStream(docUri)?.use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
    }
}
