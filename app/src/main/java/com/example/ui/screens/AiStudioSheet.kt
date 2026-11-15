package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.media.WallpaperTarget
import com.example.data.repository.AiAspectRatio
import com.example.data.repository.AiImageModelTier
import com.example.data.repository.AiImageResolution
import com.example.ui.viewmodel.AiAnalysisUiState
import com.example.ui.viewmodel.AiImageUiState
import com.example.ui.viewmodel.AiStudioTab
import com.example.ui.viewmodel.AiStudioViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AiStudioSheet(
    viewModel: AiStudioViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val prompt by viewModel.prompt.collectAsStateWithLifecycle()
    val modelTier by viewModel.modelTier.collectAsStateWithLifecycle()
    val resolution by viewModel.resolution.collectAsStateWithLifecycle()
    val aspectRatio by viewModel.aspectRatio.collectAsStateWithLifecycle()
    val referenceBitmap by viewModel.referenceBitmap.collectAsStateWithLifecycle()
    val imageUiState by viewModel.imageUiState.collectAsStateWithLifecycle()
    val isPolishing by viewModel.isPolishingPrompt.collectAsStateWithLifecycle()

    val analysisBitmap by viewModel.analysisBitmap.collectAsStateWithLifecycle()
    val analysisUiState by viewModel.analysisUiState.collectAsStateWithLifecycle()

    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isChatSending by viewModel.isChatSending.collectAsStateWithLifecycle()
    val chatError by viewModel.chatError.collectAsStateWithLifecycle()
    val actionStatus by viewModel.actionStatus.collectAsStateWithLifecycle()

    val pickReferenceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) viewModel.setReferenceFromUri(context, uri)
    }

    val pickAnalysisLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) viewModel.setAnalysisImageFromUri(context, uri)
    }

    var showApplyTargetDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.testTag("ai_studio_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "BACKDROP AI Studio",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = "Close AI Studio")
                }
            }

            if (actionStatus != null) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = actionStatus.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = viewModel::clearStatus) {
                            Text("OK")
                        }
                    }
                }
            }

            TabRow(selectedTabIndex = activeTab.ordinal) {
                AiStudioTab.entries.forEach { tab ->
                    Tab(
                        selected = activeTab == tab,
                        onClick = { viewModel.selectTab(tab) },
                        text = { Text(tab.label, maxLines = 1) },
                        modifier = Modifier.testTag("ai_tab_${tab.name}")
                    )
                }
            }

            when (activeTab) {
                AiStudioTab.CREATE_EDIT -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 560.dp)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = prompt,
                            onValueChange = viewModel::updatePrompt,
                            label = { Text("Describe your wallpaper or edit") },
                            placeholder = { Text("Soft pastel silk dunes under misty twilight sky...") },
                            minLines = 2,
                            maxLines = 4,
                            trailingIcon = {
                                TextButton(
                                    onClick = viewModel::polishCurrentPrompt,
                                    enabled = prompt.isNotBlank() && !isPolishing
                                ) {
                                    Text(if (isPolishing) "..." else "Polish")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("ai_prompt_input")
                        )

                        // Optional Reference Photo for Image-to-Image Editing
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    pickReferenceLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier.testTag("ai_pick_reference_button")
                            ) {
                                Icon(Icons.Rounded.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (referenceBitmap == null) "Add Reference Photo" else "Change Reference")
                            }

                            referenceBitmap?.let { bmp ->
                                Box(modifier = Modifier.size(48.dp)) {
                                    Image(
                                        bitmap = bmp.asImageBitmap(),
                                        contentDescription = "Reference image preview",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .matchParentSize()
                                            .clip(RoundedCornerShape(10.dp))
                                    )
                                    Surface(
                                        color = Color.Black.copy(alpha = 0.65f),
                                        shape = CircleShape,
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(20.dp)
                                            .clickable { viewModel.clearReferenceBitmap() }
                                    ) {
                                        Icon(
                                            Icons.Rounded.Close,
                                            contentDescription = "Clear reference image",
                                            tint = Color.White,
                                            modifier = Modifier.padding(2.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Model Tier
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            AiImageModelTier.entries.forEachIndexed { idx, tier ->
                                SegmentedButton(
                                    selected = modelTier == tier,
                                    onClick = { viewModel.selectModelTier(tier) },
                                    shape = SegmentedButtonDefaults.itemShape(idx, AiImageModelTier.entries.size)
                                ) {
                                    Text(tier.label)
                                }
                            }
                        }

                        // Output Resolution (1K, 2K, 4K)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Size", style = MaterialTheme.typography.labelLarge)
                            AiImageResolution.entries.forEach { res ->
                                FilterChip(
                                    selected = resolution == res,
                                    onClick = { viewModel.selectResolution(res) },
                                    label = { Text(res.label) }
                                )
                            }
                        }

                        // Aspect Ratios (1:1 .. 21:9)
                        Text("Aspect Ratio", style = MaterialTheme.typography.labelLarge)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(AiAspectRatio.entries) { ratio ->
                                FilterChip(
                                    selected = aspectRatio == ratio,
                                    onClick = { viewModel.selectAspectRatio(ratio) },
                                    label = { Text(ratio.label) }
                                )
                            }
                        }

                        Button(
                            onClick = viewModel::generateOrEdit,
                            enabled = imageUiState !is AiImageUiState.Loading && prompt.isNotBlank(),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("ai_generate_button")
                        ) {
                            Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (referenceBitmap != null) "Remix with Gemini" else "Create Wallpaper")
                        }

                        when (val st = imageUiState) {
                            AiImageUiState.Idle -> Unit
                            AiImageUiState.Loading -> {
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        CircularProgressIndicator()
                                        Text(
                                            "Synthesizing ${resolution.label} (${aspectRatio.label}) artwork...",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            "High-resolution generation can take 15–45 seconds.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            is AiImageUiState.Error -> {
                                Surface(
                                    color = MaterialTheme.colorScheme.errorContainer,
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = st.message,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(14.dp)
                                    )
                                }
                            }
                            is AiImageUiState.Success -> {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Image(
                                        bitmap = st.bitmap.asImageBitmap(),
                                        contentDescription = "Generated AI wallpaper",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(0.75f)
                                            .clip(RoundedCornerShape(24.dp))
                                    )
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        FilledTonalButton(onClick = viewModel::saveGeneratedToPictures) {
                                            Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Save")
                                        }
                                        Button(onClick = { showApplyTargetDialog = true }) {
                                            Icon(Icons.Rounded.Wallpaper, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Apply")
                                        }
                                        OutlinedButton(onClick = viewModel::useGeneratedAsReference) {
                                            Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Edit Further")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                AiStudioTab.ANALYZE -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 560.dp)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                pickAnalysisLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.fillMaxWidth().testTag("ai_analyze_pick_button")
                        ) {
                            Icon(Icons.Rounded.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Pick Photo for Mood & Depth Analysis")
                        }

                        analysisBitmap?.let { bmp ->
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Photo under analysis",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(170.dp)
                                    .clip(RoundedCornerShape(20.dp))
                            )
                        }

                        when (val st = analysisUiState) {
                            AiAnalysisUiState.Idle -> {
                                Text(
                                    "Pick a photo from your library or tap Analyze on any catalog wallpaper to extract color palettes, mood notes, and lock-screen depth suitability.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            AiAnalysisUiState.Loading -> {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                            is AiAnalysisUiState.Error -> {
                                Surface(
                                    color = MaterialTheme.colorScheme.errorContainer,
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = st.message,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.padding(14.dp)
                                    )
                                }
                            }
                            is AiAnalysisUiState.Success -> {
                                val res = st.result
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                                    shape = RoundedCornerShape(22.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(res.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                        Text(res.moodSummary, style = MaterialTheme.typography.bodyMedium)

                                        Text("Depth Readiness: ${res.depthReadinessScore}/100", style = MaterialTheme.typography.labelLarge)
                                        LinearProgressIndicator(
                                            progress = { res.depthReadinessScore / 100f },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Text(
                                            res.depthGuidance,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        if (res.dominantHexColors.isNotEmpty()) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                res.dominantHexColors.forEach { hex ->
                                                    val parsedColor = runCatching {
                                                        Color(android.graphics.Color.parseColor(hex))
                                                    }.getOrDefault(MaterialTheme.colorScheme.primary)
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(32.dp)
                                                                .clip(CircleShape)
                                                                .background(parsedColor)
                                                        )
                                                        Text(hex, style = MaterialTheme.typography.labelSmall)
                                                    }
                                                }
                                            }
                                        }

                                        if (res.suggestedRemixPrompt.isNotBlank()) {
                                            FilledTonalButton(
                                                onClick = { viewModel.applyAnalyzedPromptToStudio(res.suggestedRemixPrompt) },
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text("Use Prompt in Create Studio")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                AiStudioTab.CURATOR -> {
                    var chatInput by remember { mutableStateOf("") }
                    val quickPrompts = listOf(
                        "Best pastel palettes for OLED phones?",
                        "Tips for Depth Studio lock-screen framing",
                        "Current minimalist wallpaper design trends"
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 380.dp, max = 540.dp)
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(quickPrompts) { suggestion ->
                                AssistChip(
                                    onClick = { viewModel.sendChatMessage(suggestion) },
                                    label = { Text(suggestion) }
                                )
                            }
                        }

                        if (chatError != null) {
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = chatError.orEmpty(),
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }

                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            items(chatMessages, key = { it.id }) { msg ->
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = if (msg.isUser) Alignment.End else Alignment.Start
                                ) {
                                    Surface(
                                        color = if (msg.isUser) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceContainerHigh,
                                        shape = RoundedCornerShape(20.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = msg.text.ifBlank { if (msg.isStreaming) "Thinking..." else "" },
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = if (msg.isUser) MaterialTheme.colorScheme.onPrimaryContainer
                                                else MaterialTheme.colorScheme.onSurface
                                            )
                                            if (msg.sources.isNotEmpty()) {
                                                FlowRow(
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    msg.sources.take(3).forEach { src ->
                                                        AssistChip(
                                                            onClick = {
                                                                runCatching {
                                                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(src.uri))
                                                                    context.startActivity(intent)
                                                                }
                                                            },
                                                            leadingIcon = {
                                                                Icon(Icons.Rounded.Link, contentDescription = null, modifier = Modifier.size(14.dp))
                                                            },
                                                            label = { Text(src.title, maxLines = 1) }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = chatInput,
                                onValueChange = { chatInput = it },
                                placeholder = { Text("Ask BACKDROP Curator...") },
                                singleLine = true,
                                modifier = Modifier.weight(1f).testTag("ai_chat_input")
                            )
                            IconButton(
                                onClick = {
                                    viewModel.sendChatMessage(chatInput)
                                    chatInput = ""
                                },
                                enabled = chatInput.isNotBlank() && !isChatSending,
                                modifier = Modifier.size(48.dp).testTag("ai_chat_send")
                            ) {
                                Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = "Send curator message")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showApplyTargetDialog) {
        AlertDialog(
            onDismissRequest = { showApplyTargetDialog = false },
            title = { Text("Apply AI Wallpaper") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            showApplyTargetDialog = false
                            viewModel.applyGeneratedWallpaper(WallpaperTarget.HOME)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Home Screen") }
                    FilledTonalButton(
                        onClick = {
                            showApplyTargetDialog = false
                            viewModel.applyGeneratedWallpaper(WallpaperTarget.LOCK)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Lock Screen") }
                    OutlinedButton(
                        onClick = {
                            showApplyTargetDialog = false
                            viewModel.applyGeneratedWallpaper(WallpaperTarget.BOTH)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Home & Lock Screens") }
                }
            },
            confirmButton = {
                TextButton(onClick = { showApplyTargetDialog = false }) { Text("Cancel") }
            }
        )
    }
}
