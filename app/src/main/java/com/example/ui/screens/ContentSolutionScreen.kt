package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentSolutionScreen(
    title: String,
    contentData: String?,
    solutionData: String?,
    initialTab: Int = 0,
    isBengali: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(initialTab) }
    var isTabsVisible by remember { mutableStateOf(true) }

    val tabs = remember(isBengali) {
        if (isBengali) {
            listOf("কন্টেন্ট / নোটস", "সমাধান")
        } else {
            listOf("Content / Notes", "Solution")
        }
    }

    val contentScrollState = rememberScrollState()
    val solutionScrollState = rememberScrollState()

    // স্ক্রল ডিরেকশন ট্র্যাক করার জন্য NestedScrollConnection
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                if (delta < -10f && isTabsVisible) {
                    // নিচের দিকে স্ক্রল করলে ট্যাব লুকানো হবে
                    isTabsVisible = false
                } else if (delta > 10f && !isTabsVisible) {
                    // উপরের দিকে স্ক্রল করলে ট্যাব আবার চলে আসবে
                    isTabsVisible = true
                }
                return Offset.Zero
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = if (isBengali) "পেছনে যান" else "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .nestedScroll(nestedScrollConnection) // প্যারেন্ট কলামে নেস্টেড স্ক্রল কানেকশন যুক্ত করা হলো
        ) {
            // অ্যানিমেশনের মাধ্যমে হাইড/শো হওয়া ট্যাব কন্টেইনার
            AnimatedVisibility(
                visible = isTabsVisible,
                enter = expandVertically(animationSpec = tween(220)) + fadeIn(animationSpec = tween(200)),
                exit = shrinkVertically(animationSpec = tween(220)) + fadeOut(animationSpec = tween(180))
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    tonalElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        tabs.forEachIndexed { index, tabTitle ->
                            val isActive = selectedTab == index

                            val backgroundColor by animateColorAsState(
                                targetValue = if (isActive) MaterialTheme.colorScheme.primary else Color.Transparent,
                                animationSpec = tween(durationMillis = 200),
                                label = "tabBackgroundColor"
                            )

                            val contentColor by animateColorAsState(
                                targetValue = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                animationSpec = tween(durationMillis = 200),
                                label = "tabContentColor"
                            )

                            Surface(
                                onClick = { selectedTab = index },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp),
                                shape = RoundedCornerShape(9.dp),
                                color = backgroundColor,
                                shadowElevation = if (isActive) 2.dp else 0.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = if (index == 0) Icons.Default.Book else Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = contentColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = tabTitle,
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 13.sp
                                        ),
                                        color = contentColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // মূল কন্টেন্ট স্ক্রিন
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(180))
                    },
                    label = "tabContentTransition"
                ) { targetTab ->
                    val (displayText, activeScrollState) = if (targetTab == 0) {
                        val text = contentData?.takeIf { it.isNotBlank() }
                            ?: if (isBengali) "কোনো কন্টেন্ট বা নোটস পাওয়া যায়নি।" else "No content or notes available."
                        text to contentScrollState
                    } else {
                        val text = solutionData?.takeIf { it.isNotBlank() }
                            ?: if (isBengali) "কোনো সমাধান পাওয়া যায়নি।" else "No solution available."
                        text to solutionScrollState
                    }

                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        tonalElevation = 1.dp
                    ) {
                        MarkdownText(
                            markdown = displayText,
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(activeScrollState)
                                .padding(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier
) {
    val lines = remember(markdown) { markdown.lines() }
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (line in lines) {
            val trimmed = line.trim()
            when {
                trimmed.startsWith("# ") -> {
                    Text(
                        text = trimmed.removePrefix("# ").trim(),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            lineHeight = 28.sp
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }
                trimmed.startsWith("## ") -> {
                    Text(
                        text = trimmed.removePrefix("## ").trim(),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            lineHeight = 24.sp
                        ),
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                    )
                }
                trimmed.startsWith("### ") -> {
                    Text(
                        text = trimmed.removePrefix("### ").trim(),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            lineHeight = 22.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                trimmed.startsWith("* ") || trimmed.startsWith("- ") -> {
                    val bulletText = trimmed.removePrefix("* ").removePrefix("- ").trim()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .size(6.dp)
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(3.dp))
                        )
                        Text(
                            text = parseInlineMarkdown(bulletText),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                lineHeight = 22.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                trimmed.isBlank() -> {
                    Spacer(modifier = Modifier.height(4.dp))
                }
                else -> {
                    Text(
                        text = parseInlineMarkdown(trimmed),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            lineHeight = 24.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun parseInlineMarkdown(text: String): AnnotatedString {
    return remember(text) {
        buildAnnotatedString {
            val parts = text.split("**")
            parts.forEachIndexed { index, part ->
                if (index % 2 == 1) {
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color.Unspecified))
                    append(part)
                    pop()
                } else {
                    val italicParts = part.split("*")
                    italicParts.forEachIndexed { italicIdx, italicPart ->
                        if (italicIdx % 2 == 1) {
                            pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                            append(italicPart)
                            pop()
                        } else {
                            append(italicPart)
                        }
                    }
                }
            }
        }
    }
}
