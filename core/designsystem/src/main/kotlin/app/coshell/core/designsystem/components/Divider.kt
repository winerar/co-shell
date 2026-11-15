package app.coshell.core.designsystem.components

import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Line between rows or under a block (board `CompNew`, «Разделитель и загрузка», #367 А): 1 dp `outlineVariant`;
 * [faint] — half as strong, between rows of one small table. Indent with [modifier] (`padding(start = …)`).
 */
@Composable
fun CoShellDivider(modifier: Modifier = Modifier, faint: Boolean = false) {
    val color = MaterialTheme.colorScheme.outlineVariant
    HorizontalDivider(modifier, color = if (faint) color.copy(alpha = FAINT) else color)
}

private const val FAINT = 0.5f
