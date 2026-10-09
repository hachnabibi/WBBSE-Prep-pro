package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.data.network.ArchiveSearchResult
import com.example.data.network.InternetArchiveApi
import com.example.ui.components.AdMobBanner
import com.example.ui.components.InAppPdfViewer
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.SecondaryEmerald
import com.example.util.InAppPdfManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InternetArchiveSearchScreen(
    initialQuery: String,
    isBengali: Boolean,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    var searchInput by remember { mutableStateOf(initialQuery) }
    var currentQuery by remember { mutableStateOf(initialQuery) }
    var results by remember { mutableStateOf<List<ArchiveSearchResult>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Pagination states
    var currentPage by remember { mutableIntStateOf(1) }
    var isPaginationLoading by remember { mutableStateOf(false) }
    var canLoadMore by remember { mutableStateOf(true) }
    val listState = rememberLazyListState()

    val downloadedIdentifiers = remember { mutableStateListOf<String>() }
    var downloadingIdentifier by remember { mutableStateOf<String?>(null) }
    var downloadProgress by remember { mutableFloatStateOf(0f) }
    var activeViewingDoc by remember { mutableStateOf<ViewingPdfDoc?>(null) }

    // Sync downloaded archive books on start
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val dir = InAppPdfManager.getArchiveDir(context)
            val files = dir.listFiles() ?: emptyArray()
            val existingIds = files
                .filter { it.extension.equals("pdf", ignoreCase = true) && InAppPdfManager.isValidPdf(it) }
                .map { it.nameWithoutExtension }
            withContext(Dispatchers.Main) {
                downloadedIdentifiers.clear()
                downloadedIdentifiers.addAll(existingIds)
            }
        }
    }

    // Intercept back button if currently viewing a PDF inside the app
    if (activeViewingDoc != null) {
        InAppPdfViewer(
            file = activeViewingDoc!!.file,
            title = activeViewingDoc!!.title,
            subtitle = activeViewingDoc!!.subtitle,
            isBengali = isBengali,
            onBack = { activeViewingDoc = null },
            modifier = modifier
        )
        return
    }

    // Fetch initial page 1 search results whenever currentQuery changes
    LaunchedEffect(currentQuery) {
        if (currentQuery.isNotBlank()) {
            isLoading = true
            errorMessage = null
            currentPage = 1
            canLoadMore = true
            results = emptyList()
            try {
                val firstPageResults = InternetArchiveApi.searchTexts(currentQuery, page = 1)
                results = firstPageResults
                if (firstPageResults.isEmpty()) {
                    errorMessage = "কোনো বই পাওয়া যায়নি।"
                    canLoadMore = false
                } else if (firstPageResults.size < 20) {
                    canLoadMore = false
                }
            } catch (e: Exception) {
                errorMessage = "সার্চ করতে সমস্যা হয়েছে। ইন্টারনেট কানেকশন চেক করুন।"
            } finally {
                isLoading = false
            }
        }
    }

    // Auto-load next page when user scrolls near the end of the list
    val shouldLoadMore by remember {
        derivedStateOf {
            val totalItems = listState.layoutInfo.totalItemsCount
            val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisibleItemIndex >= totalItems - 4
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && !isLoading && !isPaginationLoading && canLoadMore && currentQuery.isNotBlank()) {
            isPaginationLoading = true
            val nextPage = currentPage + 1
            try {
                val newPageResults = InternetArchiveApi.searchTexts(currentQuery, page = nextPage)
                if (newPageResults.isNotEmpty()) {
                    val existingIds = results.map { it.identifier }.toSet()
                    val distinctNewItems = newPageResults.filter { it.identifier !in existingIds }
                    results = results + distinctNewItems
                    currentPage = nextPage
                    if (newPageResults.size < 20) {
                        canLoadMore = false
                    }
                } else {
                    canLoadMore = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isPaginationLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("archive_back_btn")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "ফিরে যান"
                        )
                    }
                },
                title = {
                    // Sleek, compact search box in header bar
                    OutlinedTextField(
                        value = searchInput,
                        onValueChange = { searchInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("archive_header_search_input"),
                        placeholder = {
                            Text(
                                text = "বই খুঁজুন...",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                modifier = Modifier.size(18.dp),
                                tint = PrimaryIndigo
                            )
                        },
                        trailingIcon = {
                            if (searchInput.isNotEmpty()) {
                                IconButton(
                                    onClick = { searchInput = "" },
                                    modifier = Modifier.size(26.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(22.dp),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                if (searchInput.isNotBlank()) {
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                    currentQuery = searchInput.trim()
                                }
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryIndigo,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                        )
                    )
                },
                actions = {
                    Spacer(modifier = Modifier.width(4.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { padding ->
        BackHandler {
            onBack()
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Body Content: Loading, Error or Paginated Results
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = PrimaryIndigo,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "ইন্টারনেট আর্কাইভে খোঁজা হচ্ছে...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp)
                ) {
                    results.forEachIndexed { index, result ->
                        val safeId = result.identifier.replace(Regex("[^a-zA-Z0-9._-]"), "_")
                        val isDownloaded = downloadedIdentifiers.contains(safeId)
                        val isDownloading = downloadingIdentifier == result.identifier

                        item(key = result.identifier) {
                            ArchiveResultCard(
                                result = result,
                                isBengali = isBengali,
                                isDownloaded = isDownloaded,
                                isDownloading = isDownloading,
                                downloadProgress = if (isDownloading) downloadProgress else 0f,
                                onDownloadClick = {
                                    val localFile = InAppPdfManager.getLocalArchiveBookFile(context, result.identifier)
                                    if (localFile != null) {
                                        activeViewingDoc = ViewingPdfDoc(
                                            file = localFile,
                                            title = result.title,
                                            subtitle = "${result.creator} • Internet Archive"
                                        )
                                    } else {
                                        coroutineScope.launch {
                                            downloadingIdentifier = result.identifier
                                            downloadProgress = 0f
                                            val downloadedFile = InAppPdfManager.downloadArchiveBook(context, result.identifier) { p ->
                                                downloadProgress = p
                                            }
                                            downloadingIdentifier = null

                                            if (downloadedFile != null) {
                                                val idKey = result.identifier.replace(Regex("[^a-zA-Z0-9._-]"), "_")
                                                if (!downloadedIdentifiers.contains(idKey)) {
                                                    downloadedIdentifiers.add(idKey)
                                                }
                                                Toast.makeText(
                                                    context,
                                                    "✓ ডাউনলোড সম্পন্ন! খোলা হচ্ছে...",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                activeViewingDoc = ViewingPdfDoc(
                                                    file = downloadedFile,
                                                    title = result.title,
                                                    subtitle = "${result.creator} • Internet Archive"
                                                )
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    "পিডিএফ সরাসরি প্রস্তুত করা যায়নি।",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    }
                                }
                            )
                        }

                        // Add AdMob Banner after every 5 cards
                        if ((index + 1) % 5 == 0 && index < results.size - 1) {
                            item(key = "archive_ad_slot_${result.identifier}_$index") {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(84.dp) // Matched to average card height
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AdMobBanner(
                                            modifier = Modifier.fillMaxWidth(),
                                            isBengali = isBengali
                                        )
                                    }
                                }
                            }
                        } else if ((index + 1) % 7 == 0 && index < results.size - 1) {
                            // Add Custom App Promo Card after every 7 cards (where there is no AdMob ad)
                            val promoTypes = com.example.ui.components.PromoType.entries.toList()
                            val promoToDisplay = promoTypes[((index + 1) / 7) % promoTypes.size]
                            
                            item(key = "archive_promo_slot_${result.identifier}_$index") {
                                com.example.ui.components.CustomAppPromoCard(
                                    promoType = promoToDisplay,
                                    isBengali = isBengali,
                                    onClick = {
                                        when (promoToDisplay) {
                                            com.example.ui.components.PromoType.PYQ_DOWNLOAD -> onNavigate("PYQ_PDF_DOWNLOAD")
                                            com.example.ui.components.PromoType.LIVE_MOCK_TEST -> onNavigate("LIVE_MOCK_TEST")
                                            com.example.ui.components.PromoType.LIVE_QUIZ -> onNavigate("LIVE_QUIZ")
                                            com.example.ui.components.PromoType.SCHOLARSHIP -> onNavigate("LEADERBOARD")
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // Auto-load pagination loading indicator
                    if (isPaginationLoading) {
                        item(key = "archive_pagination_loader") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.5.dp,
                                    color = PrimaryIndigo
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ArchiveResultCard(
    result: ArchiveSearchResult,
    isBengali: Boolean,
    isDownloaded: Boolean,
    isDownloading: Boolean,
    downloadProgress: Float,
    onDownloadClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onDownloadClick() }
            .testTag("archive_book_${result.identifier}"),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            1.dp,
            if (isDownloaded) SecondaryEmerald.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Book Cover Image Thumbnail (with fallback)
            SubcomposeAsyncImage(
                model = result.coverImageUrl,
                contentDescription = result.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(width = 46.dp, height = 64.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(PrimaryIndigo.copy(alpha = 0.12f)),
                loading = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 1.8.dp,
                            color = PrimaryIndigo
                        )
                    }
                },
                error = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(PrimaryIndigo.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            tint = PrimaryIndigo,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            )

            // Book Details (Spacious layout, icons before author and download count)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = result.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        lineHeight = 16.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Author row with person icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = result.creator,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Year & Download count with download icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (result.year.isNotBlank() && result.year != "N/A") {
                        Text(
                            text = result.year,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = null,
                            tint = PrimaryIndigo,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = result.formattedDownloads,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Compact Action Button (No external browser icon)
            if (isDownloading) {
                if (downloadProgress > 0f) {
                    CircularProgressIndicator(
                        progress = { downloadProgress },
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.2.dp,
                        color = PrimaryIndigo
                    )
                } else {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.2.dp,
                        color = PrimaryIndigo
                    )
                }
            } else if (isDownloaded) {
                FilledTonalButton(
                    onClick = onDownloadClick,
                    shape = RoundedCornerShape(7.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = SecondaryEmerald.copy(alpha = 0.18f),
                        contentColor = SecondaryEmerald
                    ),
                    contentPadding = PaddingValues(horizontal = 7.dp, vertical = 1.dp),
                    modifier = Modifier
                        .height(30.dp)
                        .testTag("open_archive_${result.identifier}")
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "পড়ুন",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp
                        )
                    )
                }
            } else {
                Button(
                    onClick = onDownloadClick,
                    shape = RoundedCornerShape(7.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                    contentPadding = PaddingValues(horizontal = 7.dp, vertical = 1.dp),
                    modifier = Modifier
                        .height(30.dp)
                        .testTag("download_archive_${result.identifier}")
                ) {
                    Icon(
                        Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "ডাউনলোড",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp
                        ),
                        color = Color.White
                    )
                }
            }
        }
    }
}
