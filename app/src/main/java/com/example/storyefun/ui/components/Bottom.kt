package com.example.storyefun.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.example.storyefun.ui.theme.LocalAppColors
import com.example.storyefun.viewModel.ThemeViewModel
import kotlinx.coroutines.delay

data class Item(
    val icon: ImageVector,
    val color: Color,
    val route: String,
    val offset: Offset = Offset.Zero,
    val size: IntSize = IntSize.Zero
)

@Composable
fun BottomBar(navController: NavController, currentRoute: String, themeViewModel: ThemeViewModel) {
    val configuration = LocalConfiguration.current
    val theme = LocalAppColors.current // Access theme colors

    val items = remember {
        mutableStateListOf(
            Item(
                icon = Icons.Rounded.Home,
                color = theme.buttonOrange, // Use theme.buttonOrange
                route = "home"
            ),
            Item(
                icon = Icons.Rounded.AccountBox,
                color = theme.buttonOrange,
                route = "category"
            ),

            Item(
                icon = Icons.Rounded.FavoriteBorder,
                color = theme.buttonOrange,
                route = "favourite"
            ),
            Item(
                icon = Icons.Rounded.Difference,
                color = theme.buttonOrange,
                route = "quest"
            ),
            Item(
                icon = Icons.Rounded.Settings,
                color = theme.buttonOrange,
                route = "setting"
            )
        )
    }
    val indicatorWidth = (configuration.screenWidthDp / items.count()) / 2
    var selectedIndex = remember(currentRoute) {
        mutableStateOf(
            items.indexOfFirst { it.route == currentRoute }.coerceAtLeast(1)
        )
    }


    val indicatorOffset by animateIntOffsetAsState(
        targetValue = IntOffset(
            items[selectedIndex.value].offset.x.toInt() + (items[selectedIndex.value].size.width / 4) - (items.count() * 2) - 2,
            15
        ),
        animationSpec = tween(400)
    )
    val infiniteTransition = rememberInfiniteTransition()
    val indicatorColor by animateColorAsState(
        targetValue = items[selectedIndex.value].color,
        animationSpec = tween(500)
    )
    val indicatorFlashingColor by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        )
    )

    val switching = remember {
        mutableStateOf(false)
    }
    LaunchedEffect(switching.value) {
        if (switching.value) {
            delay(250)
            switching.value = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            //.shadow(8.dp, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(0.dp))
            .background(theme.backgroundColor) // Use theme.backgroundColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                if (item.route == currentRoute) {
                    selectedIndex.value = index
                }
                val isSelected = selectedIndex.value == index
                val iconTintColor = if (isSelected) theme.buttonOrange else theme.textSecondary // Use theme colors

                Box(
                    modifier = Modifier
                        .onGloballyPositioned {
                            val offset = it.positionInParent()
                            items[index] = items[index].copy(
                                offset = offset,
                                size = it.size
                            )
                        }
                        .weight((1.0 / items.count()).toFloat())
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = {
                                switching.value = true
                                selectedIndex.value = index
                                navController.navigate(item.route)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = iconTintColor
                    )
                }
            }
        }

        Column(
            modifier = Modifier.offset {
                indicatorOffset
            },
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .shadow(
                        2.dp,
                        CircleShape,
                        ambientColor = indicatorColor,
                        spotColor = indicatorColor
                    )
                    .height(3.dp)
                    .width(indicatorWidth.dp)
                    .clip(CircleShape)
                    .background(indicatorColor)
            )
            AnimatedVisibility(
                visible = !switching.value,
                enter = expandVertically() + fadeIn(),
                exit = shrinkHorizontally() + fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .width(50.dp)
                        .height(50.dp)
                        .drawBehind {
                            val path = Path()
                            path.moveTo(100f, 0f)
                            path.lineTo(48f, 0f)
                            path.lineTo(-4f, 135f)
                            path.lineTo(135f, 135f)
                            path.close()
                            drawPath(
                                path = path,
                                brush = Brush.verticalGradient(
                                    listOf(
                                        indicatorColor.copy(alpha = indicatorFlashingColor - 0.2f),
                                        indicatorColor.copy(alpha = indicatorFlashingColor - 0.4f),
                                        Color.Transparent
                                    )
                                )
                            )
                        }
                )
            }
        }
    }
}