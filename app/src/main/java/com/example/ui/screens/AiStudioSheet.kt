package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Refresh
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.media.WallpaperTarget
import com.example.data.model.UiStyleMode
import com.example.data.repository.AiAspectRatio
import com.example.data.repository.AiImageModelTier
import com.example.data.repository.AiImageResolution
import com.example.ui.theme.LocalDarkThemeFlag
import com.example.ui.theme.LocalUiStyleMode
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
    val focusManager = LocalFocusManager.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val capabilities by viewModel.capabilities.collectAsStateWithLifecycle()
    val prompt by viewModel.prompt.collectAsStateWithLifecycle()
    val modelTier by viewModel.modelTier.collectAsStateWithLifecycle()
    val resolution by viewModel.resolution.collectAsStateWithLifecycle()
    val aspectRatio by viewModel.aspectRatio.collectAsStateWithLifecycle()
    val referenceBitmap by viewModel.referenceBitmap.collectAsStateWithLifecycle()
    val isLoadingReference by viewModel.isLoadingReference.collectAsStateWithLifecycle()
    val imageUiState by viewModel.imageUiState.collectAsStateWithLifecycle()
    val isPolishing by viewModel.isPolishingPrompt.collectAsStateWithLifecycle()
    val isSavingOrApplying by viewModel.isSavingOrApplying.collectAsStateWithLifecycle()

    val analysisBitmap by viewModel.analysisBitmap.collectAsStateWithLifecycle()
    val analysisUiState by viewModel.analysisUiState.collectAsStateWithLifecycle()

    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isChatSending by viewModel.isChatSending.collectAsStateWithLifecycle()
    val chatError by viewModel.chatError.collectAsStateWithLifecycle()
    val lastFailedPrompt by viewModel.lastFailedPrompt.collectAsStateWithLifecycle()
    val actionStatus by viewModel.actionStatus.collectAsStateWithLifecycle()
    val actionIsError by viewModel.actionIsError.collectAsStateWithLifecycle()

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

    val isGlass = LocalUiStyleMode.current == UiStyleMode.FROSTED_GLASS_OS
    val isDark = LocalDarkThemeFlag.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = if (isGlass) {
            MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = if (isDark) 0.90f else 0.94f)
        } else {
            androidx.compose.material3.BottomSheetDefaults.ContainerColor
        },
        modifier = Modifier.testTag("ai_studio_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(bottom = 16.dp)
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
                    color = if (actionIsError) MaterialTheme.colorScheme.errorContainer
                    else MaterialTheme.colorScheme.secondaryContainer,
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
                            color = if (actionIsError) MaterialTheme.colorScheme.onErrorContainer
                            else MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = viewModel::clearStatus) {
                            Text("OK")
                        }
                    }
                }
            }

            val visibleTabs = remember(capabilities) {
                buildList {
                    if (capabilities.imageGenAvailable) add(AiStudioTab.CREATE_EDIT)
                    if (capabilities.textAndVisionAvailable) {
                        add(AiStudioTab.ANALYZE)
                        add(AiStudioTab.CURATOR)
                    }
                }
            }

            LaunchedEffect(visibleTabs, activeTab) {
                if (visibleTabs.isEmpty()) {
                    onDismiss()
                } else if (activeTab !in visibleTabs) {
                    viewModel.selectTab(visibleTabs.first())
                }
            }

            if (visibleTabs.isEmpty()) return@Column
            val currentTab = if (activeTab in visibleTabs) activeTab else visibleTabs.first()

            TabRow(selectedTabIndex = visibleTabs.indexOf(currentTab).coerceAtLeast(0)) {
                visibleTabs.forEach { tab ->
                    Tab(
                        selected = currentTab == tab,
                        onClick = { viewModel.selectTab(tab) },
                        text = { Text(tab.label, maxLines = 1) },
                        modifier = Modifier.testTag("ai_tab_${tab.name}")
                    )
                }
            }

            when (currentTab) {
                AiStudioTab.CREATE_EDIT -> {
                    val createScrollState = rememberScrollState()

                    // Scroll automatically so the active spinner, generated artwork, or error is always visible
                    LaunchedEffect(imageUiState) {
                        if (imageUiState !is AiImageUiState.Idle) {
                            createScrollState.animateScrollTo(createScrollState.maxValue)
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 580.dp)
                            .verticalScroll(createScrollState)
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
                                    onClick = {
                                        focusManager.clearFocus()
                                        viewModel.polishCurrentPrompt()
                                    },
                                    enabled = prompt.isNotBlank() && !isPolishing
                                ) {
                                    if (isPolishing) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text("Polish")
                                    }
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
                                enabled = !isLoadingReference && imageUiState !is AiImageUiState.Loading,
                                modifier = Modifier.testTag("ai_pick_reference_button")
                            ) {
                                Icon(Icons.Rounded.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (referenceBitmap == null) "Add Reference Photo" else "Change Reference")
                            }

                            if (isLoadingReference) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Text(
                                        text = "Loading photo...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
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
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.generateOrEdit()
                            },
                            enabled = imageUiState !is AiImageUiState.Loading && !isLoadingReference && prompt.isNotBlank(),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("ai_generate_button")
                        ) {
                            if (imageUiState is AiImageUiState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (referenceBitmap != null) "Remixing Artwork..." else "Creating Wallpaper...")
                            } else {
                                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (referenceBitmap != null) "Remix with Gemini" else "Create Wallpaper")
                            }
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
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = st.message,
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        OutlinedButton(
                                            onClick = viewModel::generateOrEdit,
                                            shape = RoundedCornerShape(50)
                                        ) {
                                            Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Try Again")
                                        }
                                    }
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
                                        FilledTonalButton(
                                            onClick = viewModel::saveGeneratedToPictures,
                                            enabled = !isSavingOrApplying
                                        ) {
                                            if (isSavingOrApplying) {
                                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                            } else {
                                                Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Save")
                                        }
                                        Button(
                                            onClick = { showApplyTargetDialog = true },
                                            enabled = !isSavingOrApplying
                                        ) {
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
                    val analyzeScrollState = rememberScrollState()
                    LaunchedEffect(analysisUiState) {
                        if (analysisUiState !is AiAnalysisUiState.Idle) {
                            analyzeScrollState.animateScrollTo(analyzeScrollState.maxValue)
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 580.dp)
                            .verticalScroll(analyzeScrollState)
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                pickAnalysisLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            enabled = analysisUiState !is AiAnalysisUiState.Loading,
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
                                            "Analyzing mood, color palette & depth headroom...",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                            is AiAnalysisUiState.Error -> {
                                Surface(
                                    color = MaterialTheme.colorScheme.errorContainer,
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = st.message,
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        if (analysisBitmap != null) {
                                            OutlinedButton(
                                                onClick = viewModel::analyzeCurrentBitmap,
                                                shape = RoundedCornerShape(50)
                                            ) {
                                                Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Retry Analysis")
                                            }
                                        }
                                    }
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
                                                        Color(hex.toColorInt())
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

                                        if (res.suggestedRemixPrompt.isNotBlank() && capabilities.imageGenAvailable) {
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
                    val listState = rememberLazyListState()
                    val quickPrompts = listOf(
                        "Best pastel palettes for OLED phones?",
                        "Tips for Depth Studio lock-screen framing",
                        "Current minimalist wallpaper design trends"
                    )

                    val lastTextLength = chatMessages.lastOrNull()?.text?.length ?: 0
                    LaunchedEffect(chatMessages.size, lastTextLength) {
                        if (chatMessages.isNotEmpty()) {
                            listState.animateScrollToItem(chatMessages.lastIndex)
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 380.dp, max = 560.dp)
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(quickPrompts) { suggestion ->
                                AssistChip(
                                    onClick = { viewModel.sendChatMessage(suggestion) },
                                    enabled = !isChatSending,
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
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = chatError.orEmpty(),
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (lastFailedPrompt != null) {
                                        TextButton(onClick = viewModel::retryLastChatMessage) {
                                            Text("Retry")
                                        }
                                    }
                                }
                            }
                        }

                        LazyColumn(
                            state = listState,
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
                                                text = msg.text.ifBlank { if (msg.isStreaming) "Curating response..." else "" },
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
                                                                    val intent = Intent(Intent.ACTION_VIEW, src.uri.toUri())
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
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                keyboardActions = KeyboardActions(
                                    onSend = {
                                        if (chatInput.isNotBlank() && !isChatSending) {
                                            viewModel.sendChatMessage(chatInput)
                                            chatInput = ""
                                            focusManager.clearFocus()
                                        }
                                    }
                                ),
                                modifier = Modifier.weight(1f).testTag("ai_chat_input")
                            )
                            IconButton(
                                onClick = {
                                    viewModel.sendChatMessage(chatInput)
                                    chatInput = ""
                                    focusManager.clearFocus()
                                },
                                enabled = chatInput.isNotBlank() && !isChatSending,
                                modifier = Modifier.size(48.dp).testTag("ai_chat_send")
                            ) {
                                if (isChatSending) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = "Send curator message")
                                }
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
