package app.coshell.reports.recap

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.core.content.FileProvider
import app.coshell.R
import app.coshell.core.designsystem.amount.LocalAmountsHidden
import app.coshell.core.designsystem.components.CardTone
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.components.recordInto
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.reports.RecapCard
import app.coshell.ui.format.formatMonthOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File

/**
 * «Поделиться» on the recap stories (#554, owner poll 2026-11-23 «А», boards `Rec554Share`, `Rec554ShareImg`): one tap draws
 * the story on screen as a 4:5 picture — amounts hidden, a quiet mark below — and hands it to the system share sheet.
 */

/**
 * Draws [card] off screen at 1080 × 1350 px (360 dp at density 3, whatever the phone has) with the amounts masked and calls
 * [onCaptured] once; the charts get [SETTLE_MS] to finish their entrance first. Nothing of it is visible while it works.
 */
@Composable
internal fun RecapPictureCapture(card: RecapCard, state: MonthRecapUiState, onCaptured: (ImageBitmap) -> Unit) {
    val layer = rememberGraphicsLayer()
    val month = state.month ?: return
    CompositionLocalProvider(
        LocalDensity provides Density(PICTURE_DENSITY, fontScale = 1f),
        LocalAmountsHidden provides true,
    ) {
        // A zero-size holder: the picture node inside keeps its real size (a zero-size node is not drawn at all).
        Box(Modifier.layout { measurable, _ -> measurable.measure(Constraints()).let { layout(0, 0) { it.place(0, 0) } } }) {
            Column(
                Modifier
                    .layout { measurable, _ ->
                        val placeable = measurable.measure(Constraints.fixed(PICTURE_WIDTH, PICTURE_HEIGHT))
                        layout(PICTURE_WIDTH, PICTURE_HEIGHT) { placeable.place(0, 0) }
                    }
                    .recordInto(layer, IntSize(PICTURE_WIDTH, PICTURE_HEIGHT)),
            ) {
                CoShellCard(Modifier.fillMaxSize().padding(Spacing.l), tone = CardTone.Hero) {
                    Column(Modifier.fillMaxSize().padding(Spacing.l)) {
                        Story(card, state, onDetails = {}, forPicture = true, modifier = Modifier.weight(1f))
                        Text(
                            stringResource(R.string.recap_share_mark, stringResource(R.string.recap_title, formatMonthOf(month))),
                            Modifier.align(Alignment.CenterHorizontally),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
    LaunchedEffect(card) {
        delay(SETTLE_MS)
        withFrameNanos { }
        onCaptured(layer.toImageBitmap())
    }
}

/** Writes [bitmap] as PNG to the cache (only the last picture is kept) and offers it to the system share sheet. */
internal suspend fun shareRecapPicture(context: Context, bitmap: ImageBitmap, name: String) {
    val file = withContext(Dispatchers.IO) {
        File(context.cacheDir, "recap").apply { mkdirs() }.let { dir ->
            dir.listFiles()?.forEach { it.delete() }
            File(dir, name).apply { outputStream().use { bitmap.asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, PNG_QUALITY, it) } }
        }
    }
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.updates", file)
    val send = Intent(Intent.ACTION_SEND).setType("image/png").putExtra(Intent.EXTRA_STREAM, uri).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    context.startActivity(Intent.createChooser(send, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}

private const val PICTURE_WIDTH = 1080
private const val PICTURE_HEIGHT = 1350
private const val PICTURE_DENSITY = 3f
private const val SETTLE_MS = 1_200L
private const val PNG_QUALITY = 100
