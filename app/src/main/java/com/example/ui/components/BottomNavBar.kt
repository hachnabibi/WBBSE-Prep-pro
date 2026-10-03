package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ripple
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.AppScreen

sealed class NavItem(
    val screen: AppScreen,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    object Home : NavItem(AppScreen.HOME, "হোম", Icons.Default.Home)
    object Journey : NavItem(AppScreen.JOURNEY, "রোডম্যাপ", Icons.Default.Map)
    object Mock : NavItem(AppScreen.MOCK, "মক", Icons.Default.Quiz)
    object Video : NavItem(AppScreen.VIDEO_LESSON, "ভিডিও", Icons.Default.PlayCircle)
    object Doubt : NavItem(AppScreen.DOUBT_SOLVER, "ডাউট", Icons.Default.AutoAwesome)
    object Profile : NavItem(AppScreen.PROFILE, "প্রোফাইল", Icons.Default.Person)
}

/**
 * Compact Bottom Navigation Bar:
 * - Reduced sleek height (52.dp content + navigation bar system insets)
 * - Adjustable, evenly distributed item widths (Modifier.weight(1f))
 * - Dedicated circular background for active/selected item (CircleShape)
 */
@Composable
fun BottomNavBar(
    currentScreen: AppScreen,
    isBengali: Boolean,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        NavItem.Home,
        NavItem.Journey,
        NavItem.Mock,
        NavItem.Video,
        NavItem.Doubt,
        NavItem.Profile
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("bottom_nav_bar"),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                thickness = 0.75.dp
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp) // Slightly increased for better label visibility
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val isSelected = currentScreen == item.screen
                    // Home tab special case: Home in Home mode, otherwise Back icon? 
                    // No, requirements say keep bottom nav tabs synced.
                    
                    val labelText = item.title 

                    val iconTint by animateColorAsState(
                        targetValue = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                        label = "icon_tint"
                    )

                    val textColor by animateColorAsState(
                        targetValue = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                        label = "text_tint"
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = false, radius = 32.dp),
                                onClick = { onNavigate(item.screen) }
                            )
                            .testTag("nav_item_${item.screen.name.lowercase()}"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = labelText,
                            tint = iconTint,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = labelText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                fontSize = 10.sp
                            ),
                            color = textColor,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
