package com.example.ui.screens

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EaFileItem
import com.example.model.EaFileType
import com.example.model.FileExecutionStatus
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavyCardBorder
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.TradingRobotViewModel

@Composable
fun FileManagerScreen(
    viewModel: TradingRobotViewModel
) {
    val context = LocalContext.current
    val filesList by viewModel.filesList.collectAsState()
    val selectedFile by viewModel.selectedFile.collectAsState()
    val vpsConfig by viewModel.vpsConfig.collectAsState()
    val isOperating by viewModel.fileOperationInProgress.collectAsState()

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            var fileName = "custom_ea.mq4"
            var fileSize: Long = 2048
            val cursor = context.contentResolver.query(it, null, null, null, null)
            cursor?.use { c ->
                if (c.moveToFirst()) {
                    val nameIndex = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = c.getColumnIndex(OpenableColumns.SIZE)
                    if (nameIndex >= 0) fileName = c.getString(nameIndex) ?: fileName
                    if (sizeIndex >= 0) fileSize = c.getLong(sizeIndex)
                }
            }
            viewModel.addCustomFile(fileName, fileSize)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column {
            Text(
                text = "EXPERT ADVISOR REPOSITORY",
                color = NeonCyan,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
            Text(
                text = "File Manager",
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Select, compile, and execute MT4 (.ex4, .mq4) and MT5 (.ex5, .mq5) trading robots.",
                color = TextSecondary,
                fontSize = 13.sp
            )
        }

        // 1. FILE SELECTION & ACTIONS CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = NavySurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, NavyCardBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Active Robot / EA File",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // SELECT FILE BUTTON
                    Button(
                        onClick = {
                            filePickerLauncher.launch("*/*")
                        },
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("select_file_button"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                    ) {
                        Icon(
                            Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = NavyDark,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Select File",
                            color = NavyDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                if (selectedFile != null) {
                    val current = selectedFile!!
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(NavyCard)
                            .border(1.dp, NavyCardBorder, RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        Icons.Default.Description,
                                        contentDescription = null,
                                        tint = NeonCyan,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = current.fileName,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }

                                FileTypeBadge(type = current.fileType)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Size: ${current.fileSizeFormatted}",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = if (current.isRequiresCompilation) "Requires MetaEditor" else "Pre-compiled Binary",
                                    color = if (current.isRequiresCompilation) AmberWarning else EmeraldGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // FILE EXECUTION STATUS DISPLAY
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                when (current.status) {
                                    FileExecutionStatus.RUNNING -> EmeraldGreen.copy(alpha = 0.12f)
                                    FileExecutionStatus.COMPILING -> AmberWarning.copy(alpha = 0.12f)
                                    FileExecutionStatus.COMPILATION_ERROR -> CrimsonRed.copy(alpha = 0.12f)
                                    else -> NavyCard
                                }
                            )
                            .border(
                                1.dp,
                                when (current.status) {
                                    FileExecutionStatus.RUNNING -> EmeraldGreen
                                    FileExecutionStatus.COMPILING -> AmberWarning
                                    FileExecutionStatus.COMPILATION_ERROR -> CrimsonRed
                                    else -> NavyCardBorder
                                },
                                RoundedCornerShape(10.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = when (current.status) {
                                            FileExecutionStatus.RUNNING -> Icons.Default.CheckCircle
                                            FileExecutionStatus.COMPILING -> Icons.Default.Sync
                                            FileExecutionStatus.COMPILATION_ERROR -> Icons.Default.Error
                                            else -> Icons.Default.Build
                                        },
                                        contentDescription = null,
                                        tint = when (current.status) {
                                            FileExecutionStatus.RUNNING -> EmeraldGreen
                                            FileExecutionStatus.COMPILING -> AmberWarning
                                            FileExecutionStatus.COMPILATION_ERROR -> CrimsonRed
                                            else -> TextSecondary
                                        },
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Status: ${current.status.name}",
                                        color = when (current.status) {
                                            FileExecutionStatus.RUNNING -> EmeraldGreen
                                            FileExecutionStatus.COMPILING -> AmberWarning
                                            FileExecutionStatus.COMPILATION_ERROR -> CrimsonRed
                                            else -> TextPrimary
                                        },
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                if (isOperating) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        color = NeonCyan,
                                        strokeWidth = 2.dp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = current.statusDetails,
                                color = TextSecondary,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }

                    // RUN FILE & STOP FILE BUTTONS
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.runFile(current) },
                            enabled = !isOperating && current.status != FileExecutionStatus.RUNNING,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("run_file_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldGreen,
                                disabledContainerColor = NavyCard
                            )
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Run File", color = Color.Black, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.stopFile(current) },
                            enabled = !isOperating && current.status == FileExecutionStatus.RUNNING,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("stop_file_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CrimsonRed,
                                disabledContainerColor = NavyCard
                            )
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Stop File", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 2. SUPPORTED FORMATS & ARCHITECTURAL REALITY CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = NavySurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, NavyCardBorder)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Computer,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Technical & Compilation Architecture",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "MetaTrader EX4/EX5 binaries and MetaEditor compiler operate on x86 Windows architecture and cannot run directly on Android OS. This app uploads source files (.mq4/.mq5) to the connected VPS, compiles them via MetaEditor on the VPS, and starts the EA inside the MetaTrader terminal.",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }

        // 3. AVAILABLE EA REPOSITORY LIST
        Text(
            text = "Supported Robot & EA Files",
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filesList, key = { it.id }) { file ->
                val isSelected = selectedFile?.id == file.id
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) NavyCard else NavySurface)
                        .border(
                            1.dp,
                            if (isSelected) NeonCyan else NavyCardBorder,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { viewModel.selectFile(file) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        FileTypeBadge(type = file.fileType)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = file.fileName,
                                color = if (isSelected) NeonCyan else TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "${file.fileSizeFormatted} • ${if (file.isRequiresCompilation) "Needs MetaEditor" else "Ready Binary"}",
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Text(
                        text = file.status.name,
                        color = when (file.status) {
                            FileExecutionStatus.RUNNING -> EmeraldGreen
                            FileExecutionStatus.COMPILING -> AmberWarning
                            FileExecutionStatus.COMPILATION_ERROR -> CrimsonRed
                            else -> TextSecondary
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun FileTypeBadge(type: EaFileType) {
    val (label, bg, fg) = when (type) {
        EaFileType.MQ4 -> Triple("MQ4", AmberWarning.copy(alpha = 0.2f), AmberWarning)
        EaFileType.EX4 -> Triple("EX4", NeonCyan.copy(alpha = 0.2f), NeonCyan)
        EaFileType.MQ5 -> Triple("MQ5", AmberWarning.copy(alpha = 0.2f), AmberWarning)
        EaFileType.EX5 -> Triple("EX5", EmeraldGreen.copy(alpha = 0.2f), EmeraldGreen)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            color = fg,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
        )
    }
}
