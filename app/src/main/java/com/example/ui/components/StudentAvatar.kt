package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun StudentAvatar(
    profileImageUri: String,
    displayName: String,
    size: Dp,
    modifier: Modifier = Modifier,
    googlePhotoUrl: String? = null,
    onClick: (() -> Unit)? = null
) {
    val clickableModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .then(clickableModifier),
        contentAlignment = Alignment.Center
    ) {
        val resolvedUri = profileImageUri.ifBlank { googlePhotoUrl ?: "" }
        if (resolvedUri.startsWith("avatar:")) {
            val code = resolvedUri.substringAfter("avatar:")
            val (emoji, brush) = when (code) {
                "1" -> "🎓" to Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)))
                "2" -> "🔭" to Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF06B6D4)))
                "3" -> "💻" to Brush.linearGradient(listOf(Color(0xFF1E293B), Color(0xFF0F766E)))
                "4" -> "📚" to Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF047857)))
                "5" -> "🎨" to Brush.linearGradient(listOf(Color(0xFFEC4899), Color(0xFFF43F5E)))
                "6" -> "🚀" to Brush.linearGradient(listOf(Color(0xFFF97316), Color(0xFFDC2626)))
                "7" -> "🦁" to Brush.linearGradient(listOf(Color(0xFFF97316), Color(0xFFD97706)))
                "8" -> "🧠" to Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFFD946EF)))
                else -> "🎓" to Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)))
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(brush),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = emoji,
                    fontSize = (size.value * 0.45f).sp,
                    textAlign = TextAlign.Center
                )
            }
        } else if (resolvedUri.isNotBlank()) {
            AsyncImage(
                model = resolvedUri,
                contentDescription = "Profile Picture",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            // Default Initials Avatar
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF4F46E5), Color(0xFF06B6D4))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = displayName.take(1).uppercase(),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = (size.value * 0.38f).sp
                    ),
                    color = Color.White
                )
            }
        }
    }
}
