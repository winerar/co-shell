package app.coshell.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.coshell.core.designsystem.theme.LocalAmountPalette
import app.coshell.core.designsystem.theme.LocalStatusPalette

/** Meaning of a [CoShellStatusChip]: new or «сегодня», in work or «пропущен», done, closed. */
enum class StatusTone { New, Work, Done, No }

/**
 * A status label (board `CompNew` «Точка и статус», #367 А; shape — board Style2): a 22 dp capsule tinted 18 % with
 * the tone, the tone also in the dot, the [text] in ink (#385: colour only repeats what the words say).
 */
@Composable
fun CoShellStatusChip(text: String, tone: StatusTone, modifier: Modifier = Modifier) {
    val color = statusToneColor(tone)
    Row(
        modifier.labelCapsule(color),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        CoShellDot(color, size = 6.dp)
        Text(
            text,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun statusToneColor(tone: StatusTone): Color = when (tone) {
    StatusTone.New -> LocalAmountPalette.current.income
    StatusTone.Work -> LocalStatusPalette.current.warning
    StatusTone.Done -> LocalStatusPalette.current.success
    StatusTone.No -> MaterialTheme.colorScheme.onSurfaceVariant
}
