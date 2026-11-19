package app.coshell.hints

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.motion.CoShellSuccessBurst
import app.coshell.core.designsystem.tokens.Spacing
import kotlinx.coroutines.delay

private const val FINALE_MS = 7_000L

/**
 * «Всё готово» in the place of the «Первые шаги» row (board `Hint513Finale`): once, with the halo, the sparks and the
 * `Confirm` tick of [CoShellSuccessBurst]; a tap or a few seconds later it leaves ([onDone]).
 */
@Composable
fun StepsFinale(onDone: () -> Unit, modifier: Modifier = Modifier) {
    LaunchedEffect(Unit) {
        delay(FINALE_MS)
        onDone()
    }
    CoShellSuccessBurst(modifier.fillMaxWidth()) {
        CoShellCard(Modifier.fillMaxWidth(), onClick = onDone) {
            Column(Modifier.fillMaxWidth().padding(Spacing.l), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(Spacing.xs)) {
                Text(stringResource(R.string.steps_done_title), style = MaterialTheme.typography.titleMedium)
                Text(
                    stringResource(R.string.steps_done_text),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
