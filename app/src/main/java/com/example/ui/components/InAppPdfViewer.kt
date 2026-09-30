package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.widget.Toast
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.util.InAppPdfManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Fullscreen In-App PDF Viewer utilizing Android's native PdfRenderer.
 * Completely eliminates any need for external PDF reader apps or web browsers.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InAppPdfViewer(
    file: File,
    title: String,
    subtitle: String,
    isBengali: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var pfd by remember { mutableStateOf<ParcelFileDescriptor?>(null) }
    var pdfRenderer by remember { mutableStateOf<PdfRenderer?>(null) }
    var pageCount by remember { mutableIntStateOf(0) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var isSavingToDevice by remember { mutableStateOf(false) }

    // Zoom & Pan state
    var zoomScale by remember { mutableFloatStateOf(1f) }
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }

    // Reading mode: Normal, Night (inverted), Warm Sepia
    var isNightReadingMode by remember { mutableStateOf(false) }

    BackHandler {
        onBack()
    }

    // Initialize PdfRenderer
    LaunchedEffect(file) {
        withContext(Dispatchers.IO) {
            try {
                if (!file.exists() || file.length() < 100) {
                    loadError = "পিডিএফ ফাইলটি পাওয়া যায়নি বা ক্ষতিগ্রস্ত।"
                    return@withContext
                }
                val descriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                val renderer = PdfRenderer(descriptor)
                pfd = descriptor
                pdfRenderer = renderer
                pageCount = renderer.pageCount
                loadError = null
            } catch (e: Exception) {
                e.printStackTrace()
                loadError = "পিডিএফ লোড করতে সমস্যা হয়েছে: ${e.localizedMessage}"
            }
        }
    }

    // Cleanup resources
    DisposableEffect(file) {
        onDispose {
            try {
                pdfRenderer?.close()
                pfd?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val visiblePage = remember {
        derivedStateOf {
            if (pageCount == 0) 1 else (listState.firstVisibleItemIndex + 1).coerceIn(1, pageCount)
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                shadowElevation = 3.dp,
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back button & Page Controls
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("in_app_pdf_back_btn")
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    val target = (listState.firstVisibleItemIndex - 1).coerceAtLeast(0)
                                    listState.animateScrollToItem(target)
                                }
                            },
                            enabled = listState.firstVisibleItemIndex > 0,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.ChevronLeft,
                                contentDescription = "Previous Page",
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PrimaryIndigo.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, PrimaryIndigo.copy(alpha = 0.25f))
                        ) {
                            Text(
                                text = if (pageCount > 0) "${visiblePage.value} / $pageCount" else "...",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = PrimaryIndigo,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    val target = (listState.firstVisibleItemIndex + 1).coerceAtMost(pageCount - 1)
                                    listState.animateScrollToItem(target)
                                }
                            },
                            enabled = listState.firstVisibleItemIndex < pageCount - 1,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = "Next Page",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Divider
                    Box(
                        modifier = Modifier
                            .height(20.dp)
                            .width(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                    )

                    // Zoom Controls
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        IconButton(
                            onClick = {
                                zoomScale = (zoomScale - 0.25f).coerceAtLeast(0.75f)
                                if (zoomScale <= 1f) {
                                    panOffsetX = 0f
                                    panOffsetY = 0f
                                }
                            },
                            enabled = zoomScale > 0.75f,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.ZoomOut,
                                contentDescription = "Zoom Out",
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (zoomScale != 1f) ExamGold.copy(alpha = 0.2f) else Color.Transparent,
                            modifier = Modifier.clickable {
                                zoomScale = 1f
                                panOffsetX = 0f
                                panOffsetY = 0f
                            }
                        ) {
                            Text(
                                text = "${(zoomScale * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (zoomScale != 1f) Color(0xFFB45309) else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                zoomScale = (zoomScale + 0.25f).coerceAtMost(3.0f)
                            },
                            enabled = zoomScale < 3.0f,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.ZoomIn,
                                contentDescription = "Zoom In",
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }

                    // Divider
                    Box(
                        modifier = Modifier
                            .height(20.dp)
                            .width(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                    )

                    // Night Mode & Save to Device
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        IconButton(
                            onClick = { isNightReadingMode = !isNightReadingMode },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isNightReadingMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Night Mode",
                                tint = if (isNightReadingMode) ExamGold else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    file
                                )
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/pdf"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share PDF"))
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = "Share PDF",
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                if (isSavingToDevice) return@IconButton
                                isSavingToDevice = true
                                coroutineScope.launch {
                                    val cleanTitle = title.replace(Regex("[^a-zA-Z0-9\\u0980-\\u09FF_-]"), "_")
                                    val success = InAppPdfManager.savePdfToPublicDownloads(context, file, cleanTitle)
                                    isSavingToDevice = false
                                    Toast.makeText(
                                        context,
                                        if (success) {
                                            "✓ ডাউনলোড ফোল্ডারে সেভ হয়েছে (Downloads/MadhyamikPYQ)"
                                        } else {
                                            "ফাইল সেভ করা সম্ভব হয়নি"
                                        },
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("in_app_pdf_save_btn")
                        ) {
                            if (isSavingToDevice) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(
                                    Icons.Default.Download,
                                    contentDescription = "Save to Device",
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                tonalElevation = 2.dp,
                shadowElevation = 4.dp,
                color = if (isNightReadingMode) Color(0xFF1E1E1E) else MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                AdMobBanner(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    isBengali = isBengali
                )
            }
        },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(if (isNightReadingMode) Color(0xFF121212) else Color(0xFFE5E7EB))
        ) {
            when {
                loadError != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = loadError ?: "",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onBack) {
                            Text("ফিরে যান")
                        }
                    }
                }

                pdfRenderer == null || pageCount == 0 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = PrimaryIndigo,
                            strokeWidth = 4.dp,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "পিডিএফ ফাইলটি প্রসেস করা হচ্ছে...",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "বড় ফাইলের ক্ষেত্রে একটু সময় লাগতে পারে।",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                else -> {
                    val pageIndices = remember(pageCount) { (0 until pageCount).toList() }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    zoomScale = (zoomScale * zoom).coerceIn(0.75f, 3.5f)
                                    if (zoomScale > 1f) {
                                        panOffsetX += pan.x
                                        panOffsetY += pan.y
                                    } else {
                                        panOffsetX = 0f
                                        panOffsetY = 0f
                                    }
                                }
                            }
                    ) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    scaleX = zoomScale
                                    scaleY = zoomScale
                                    translationX = panOffsetX
                                    translationY = panOffsetY
                                },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            itemsIndexed(pageIndices, key = { _, index -> "page_$index" }) { index, _ ->
                                PdfPageCard(
                                    renderer = pdfRenderer,
                                    pageIndex = index,
                                    pageNumber = index + 1,
                                    totalPages = pageCount,
                                    isNightMode = isNightReadingMode,
                                    isBengali = isBengali
                                )
                            }

                            item {
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Renders an individual PDF page as a crisp, high-resolution Bitmap.
 */
@Composable
private fun PdfPageCard(
    renderer: PdfRenderer?,
    pageIndex: Int,
    pageNumber: Int,
    totalPages: Int,
    isNightMode: Boolean,
    isBengali: Boolean,
    modifier: Modifier = Modifier
) {
    var pageBitmap by remember(pageIndex, renderer) { mutableStateOf<Bitmap?>(null) }
    var isRendering by remember(pageIndex, renderer) { mutableStateOf(true) }

    LaunchedEffect(pageIndex, renderer) {
        if (renderer == null) return@LaunchedEffect
        withContext(Dispatchers.IO) {
            try {
                // PdfRenderer is NOT thread-safe for concurrent openPage calls; synchronize on the renderer instance
                synchronized(renderer) {
                    val page = renderer.openPage(pageIndex)
                    // Render at 2.0x points for sharp text reading
                    val targetWidth = (page.width * 2f).toInt()
                    val targetHeight = (page.height * 2f).toInt()
                    val bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bitmap)
                    canvas.drawColor(android.graphics.Color.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()
                    pageBitmap = bitmap
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isRendering = false
            }
        }
    }

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(8.dp))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (pageBitmap != null) {
                val colorFilter = if (isNightMode) {
                    // High-contrast inverted matrix for comfortable reading at night
                    val invertMatrix = ColorMatrix(
                        floatArrayOf(
                            -1f, 0f, 0f, 0f, 255f,
                            0f, -1f, 0f, 0f, 255f,
                            0f, 0f, -1f, 0f, 255f,
                            0f, 0f, 0f, 1f, 0f
                        )
                    )
                    ColorFilter.colorMatrix(invertMatrix)
                } else null

                Image(
                    bitmap = pageBitmap!!.asImageBitmap(),
                    contentDescription = "পৃষ্ঠা $pageNumber",
                    contentScale = ContentScale.FillWidth,
                    colorFilter = colorFilter,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    if (isRendering) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                strokeWidth = 3.dp,
                                color = PrimaryIndigo
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "পৃষ্ঠা $pageNumber তৈরি হচ্ছে...",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }
                    } else {
                        Text(
                            text = "পৃষ্ঠা লোড ব্যর্থ",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Page footer badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isNightMode) Color(0xFF1E1E1E) else Color(0xFFF3F4F6))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "WBBSE Madhyamik Original PYQ",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isNightMode) Color.LightGray else Color.DarkGray
                )
                Text(
                    text = "$pageNumber / $totalPages",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isNightMode) ExamGold else PrimaryIndigo
                )
            }
        }
    }
}
