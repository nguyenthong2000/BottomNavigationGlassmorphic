package xyz.teamgravity.bottomnavigationglassmorphic

import android.R.id.mask
import android.graphics.RuntimeShader
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.skydoves.cloudy.cloudy
import xyz.teamgravity.bottomnavigationglassmorphic.haze.BLUR_SKSL
import xyz.teamgravity.bottomnavigationglassmorphic.haze.HazeDefaults
import xyz.teamgravity.bottomnavigationglassmorphic.haze.HazeState
import xyz.teamgravity.bottomnavigationglassmorphic.haze.HazeStyle
import xyz.teamgravity.bottomnavigationglassmorphic.haze.haze
import xyz.teamgravity.bottomnavigationglassmorphic.haze.hazeChild
import xyz.teamgravity.bottomnavigationglassmorphic.ui.theme.BottomNavigationAnimatedTheme

val colors = listOf(Color.Blue, Color.Red, Color.Green, Color.Yellow, Color.Cyan)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BottomNavigationAnimatedTheme {
                Sample()
            }

        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Preview
@Composable
fun Sample1(modifier: Modifier = Modifier) {
    val colorShader = RuntimeShader(BLUR_SKSL)
    val shader = RuntimeShader(BLUR_SKSL).apply {
        setFloatUniform("blurRadius", 1f)
    }

    val shaderBrush = ShaderBrush(colorShader)

    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        colorShader.setFloatUniform("iResolution",
            size.width, size.height)
        drawCircle(brush = shaderBrush)
    }
}

@Preview
@Composable
fun Sample(modifier: Modifier = Modifier) {

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {

        val state = remember { HazeState() }
        var selectedNavigation by remember { mutableStateOf(Navigation.entries.first()) }

        Scaffold(
//                        bottomBar = {
//                            GlassmorphicBottomNavigation(
//                                state = state,
//                                selectedNavigation = selectedNavigation,
//                                onSelectNavigation = { value ->
//                                    selectedNavigation = value
//                                }
//                            )
//                        }
        ) { padding ->

            Box {
                LazyColumn(
                    contentPadding = padding,
                    modifier = Modifier
                        .haze(
                            state = state,
                        )
                        .fillMaxSize()
                ) {
                    items(50) { index ->
                        Box(
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxWidth()
                                .height(200.dp)
                                .background(
                                    color = colors.random(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .border(
                                    width = Dp.Hairline,
                                    color = Color.White.copy(alpha = 0.5F),
                                    shape = RoundedCornerShape(12.dp)
                                )
                        ) {
//                                        AsyncImage(
//                                            model = "https://yavuzceliker.github.io/sample-images/image-1.jpg",
//                                            contentDescription = null,
//                                            contentScale = ContentScale.Crop,
//                                            modifier = Modifier
//                                                .fillMaxSize()
//                                                .clip(RoundedCornerShape(12.dp))
//                                        )

//                                        Image(
//                                            painter = painterResource(R.drawable.image),
//                                            contentDescription = null,
//                                            contentScale = ContentScale.Crop,
//                                            modifier = Modifier
//                                                .matchParentSize()
//                                                .clip(
//                                                    RoundedCornerShape(12.dp)
//                                                )
//                                        )

                        }
                    }
                }

//                            GlassmorphicBottomNavigation(
//                                state = state,
//                                selectedNavigation = selectedNavigation,
//                                onSelectNavigation = { value ->
//                                    selectedNavigation = value
//                                }
//                            )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .hazeChild(
                            state,
                            style = HazeDefaults.style(backgroundColor = Color.White.copy(alpha = 1f))
                        )
                        .align(Alignment.Center)
                ) {
                    Row {
                        Icon(imageVector = Icons.Default.Home, contentDescription = null)
                        Text("HomeHomeHome", color = Color.Black, fontSize = 30.sp)
                    }
                }
            }

        }
    }
}

private enum class Navigation(
    @DrawableRes val icon: Int,
    @StringRes val label: Int,
    val color: Color
) {
    Home(
        icon = R.drawable.ic_home,
        label = R.string.home,
        color = Color(0xFFFFA574)
    ),
    Alerts(
        icon = R.drawable.ic_notification,
        label = R.string.alerts,
        color = Color(0xFFADFF64)
    ),
    Chats(
        icon = R.drawable.ic_email,
        label = R.string.chats,
        color = Color(0xFFFA6FFF)
    ),
    Settings(
        icon = R.drawable.ic_settings,
        label = R.string.settings,
        color = Color(0xFF6AB04C)
    )
}

@Composable
private fun RowScope.NavigationItem(
    navigation: Navigation,
    selected: Boolean,
    onSelect: () -> Unit
) {
    val alpha by animateFloatAsState(
        targetValue = if (selected) 1F else 0.35F,
        label = "alpha"
    )
    val scale by animateFloatAsState(
        targetValue = if (selected) 1F else 0.98F,
        visibilityThreshold = 0.000001F,
        animationSpec = spring(
            stiffness = Spring.StiffnessLow,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "scale"
    )

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(scale)
            .alpha(alpha)
            .fillMaxHeight()
            .weight(1F)
            .pointerInput(Unit) {
                detectTapGestures {
                    onSelect()
                }
            }
    ) {
        Icon(
            painter = painterResource(id = navigation.icon),
            contentDescription = stringResource(id = navigation.label)
        )
        Text(
            text = stringResource(id = navigation.label)
        )
    }
}

@Composable
private fun GlassmorphicBottomNavigation(
    state: HazeState,
    selectedNavigation: Navigation,
    onSelectNavigation: (navigation: Navigation) -> Unit
) {
    val animatedSelectedNavigationIndex by animateFloatAsState(
        targetValue = selectedNavigation.ordinal.toFloat(),
        animationSpec = spring(
            stiffness = Spring.StiffnessLow,
            dampingRatio = Spring.DampingRatioLowBouncy
        ),
        label = "animatedSelectedNavigationIndex"
    )
    val animatedColor by animateColorAsState(
        targetValue = selectedNavigation.color,
        animationSpec = spring(
            stiffness = Spring.StiffnessLow
        ),
        label = "animatedColor"
    )

    Box(
        modifier = Modifier
            .padding(
                vertical = 24.dp,
                horizontal = 64.dp
            )
            .fillMaxWidth()
            .height(64.dp)
            .hazeChild(
                state = state,
            )
//                .border(
//                    width = Dp.Hairline,
//                    brush = Brush.verticalGradient(
//                        colors = listOf(
//                            Color.White.copy(alpha = 0.8F),
//                            Color.White.copy(alpha = 0.2F)
//                        )
//                    ),
//                    shape = CircleShape
//                )
    ) {


//            CompositionLocalProvider(
//                LocalTextStyle provides LocalTextStyle.current.copy(
//                    fontSize = 12.sp,
//                    fontWeight = FontWeight.Medium
//                ),
//                LocalContentColor provides Color.White
//            ) {
//                Row(
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    Navigation.entries.forEach { navigation ->
//                        NavigationItem(
//                            navigation = navigation,
//                            selected = navigation == selectedNavigation,
//                            onSelect = {
//                                onSelectNavigation(navigation)
//                            }
//                        )
//                    }
//                }
//            }
//            Canvas(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .clip(CircleShape)
//                    .blur(
//                        radius = 50.dp,
//                        edgeTreatment = BlurredEdgeTreatment.Unbounded
//                    )
//            ) {
//                val navigationWidth = size.width / Navigation.entries.size
//                drawCircle(
//                    color = animatedColor.copy(alpha = 0.3F),
//                    radius = size.height / 2,
//                    center = Offset(
//                        x = (navigationWidth * animatedSelectedNavigationIndex) + navigationWidth / 2,
//                        y = size.height / 2
//                    )
//                )
//            }
//            Canvas(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .clip(CircleShape)
//            ) {
//                val path = Path()
//                path.addRoundRect(
//                    RoundRect(
//                        rect = size.toRect(),
//                        cornerRadius = CornerRadius(size.height)
//                    )
//                )
//                val measure = PathMeasure()
//                measure.setPath(
//                    path = path,
//                    forceClosed = false
//                )
//                val navigationWidth = size.width / Navigation.entries.size
//
//                drawPath(
//                    path = path,
//                    brush = Brush.horizontalGradient(
//                        colors = listOf(
//                            animatedColor.copy(alpha = 0F),
//                            animatedColor.copy(alpha = 1F),
//                            animatedColor.copy(alpha = 1F),
//                            animatedColor.copy(alpha = 0F)
//                        ),
//                        startX = navigationWidth * animatedSelectedNavigationIndex,
//                        endX = navigationWidth * (animatedSelectedNavigationIndex + 1)
//                    ),
//                    style = Stroke(
//                        width = 6F,
//                        pathEffect = PathEffect.dashPathEffect(
//                            intervals = floatArrayOf(measure.length / 2F, measure.length)
//                        )
//                    )
//                )
//            }
    }

}