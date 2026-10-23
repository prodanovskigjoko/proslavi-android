package mk.proslavi.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Rose = Color(0xFF8E4B5A)
private val RoseLight = Color(0xFFF2D7DD)
private val Gold = Color(0xFF8A6D10)
private val GoldLight = Color(0xFFF6E3A6)

private val Light = lightColorScheme(
    primary = Rose,
    onPrimary = Color.White,
    primaryContainer = RoseLight,
    onPrimaryContainer = Color(0xFF3A0B17),
    secondary = Gold,
    onSecondary = Color.White,
    secondaryContainer = GoldLight,
    onSecondaryContainer = Color(0xFF2A2000),
    background = Color(0xFFFFF8F6),
    surface = Color(0xFFFFF8F6),
    surfaceVariant = Color(0xFFF3E5E7),
)

private val Dark = darkColorScheme(
    primary = Color(0xFFFFB1C1),
    onPrimary = Color(0xFF55202E),
    primaryContainer = Color(0xFF713544),
    onPrimaryContainer = RoseLight,
    secondary = Color(0xFFE5C55C),
    onSecondary = Color(0xFF3C2F00),
    secondaryContainer = Color(0xFF574500),
    onSecondaryContainer = GoldLight,
)

@Composable
fun ProslaviTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) Dark else Light, content = content)
}
