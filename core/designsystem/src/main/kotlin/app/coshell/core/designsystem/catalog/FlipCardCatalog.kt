package app.coshell.core.designsystem.catalog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.coshell.core.designsystem.components.CoShellFlipCard
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.designsystem.tokens.Spacing

/** Showcase of [CoShellFlipCard] (#519): the face and the back side, both resting (the turn itself is motion, not a still). */
@Composable
fun FlipCardCatalog(flipped: Boolean = false) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Column(Modifier.padding(Spacing.l)) {
            CoShellFlipCard(
                flipped = flipped,
                onFlip = {},
                flipLabel = "Flip",
                front = {
                    Text("Total balance", style = MaterialTheme.typography.labelLarge)
                    Text("12,480.00 USD", style = MaterialTheme.typography.headlineMedium)
                },
                back = {
                    Text("November", style = MaterialTheme.typography.labelLarge)
                    Text("+860.00 USD", style = MaterialTheme.typography.headlineMedium)
                    Text("Income 2,100 · expenses 1,240", style = MaterialTheme.typography.bodySmall)
                },
            )
        }
    }
}

@Preview(name = "Flip card · face · light")
@Composable
private fun FlipFacePreview() = CoShellTheme(darkTheme = false) { FlipCardCatalog() }

@Preview(name = "Flip card · back · dark")
@Composable
private fun FlipBackPreview() = CoShellTheme(darkTheme = true) { FlipCardCatalog(flipped = true) }
