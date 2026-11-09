package app.coshell.update.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.motion.breathing
import app.coshell.core.designsystem.theme.RateTextStyle
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.size
import app.coshell.core.designsystem.components.ProgressHero
import app.coshell.core.designsystem.theme.AmountTitleStyle
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.update.UpdatePhase
import app.coshell.core.designsystem.R as DesignR

/** The ring in the middle of the update screen: an icon per step, the percentage while downloading. */
@Composable
internal fun UpdateHero(phase: UpdatePhase, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    ProgressHero(
        ring = phase.heroRing,
        state = phase,
        modifier = modifier.breathing(phase is UpdatePhase.Available),
        accent = phase is UpdatePhase.Available || phase is UpdatePhase.Downloading,
        contentKey = { it.heroStep },
    ) { shown ->
        val step = shown.heroStep
        if (shown is UpdatePhase.Downloading) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${(shown.progress * 100).toInt()}%", style = AmountTitleStyle, color = colors.onPrimaryContainer)
                val total = shown.release.sizeBytes
                Text(
                    stringResource(R.string.update_downloaded_mb, megabytes((total * shown.progress).toLong()), megabytes(total)),
                    style = RateTextStyle,
                    color = colors.onSurfaceVariant,
                )
            }
        } else {
            val tint = when (step) {
                UpdateHeroStep.Available -> colors.onPrimaryContainer
                UpdateHeroStep.Failed -> colors.error
                UpdateHeroStep.UpToDate -> colors.primary
                else -> colors.onSurfaceVariant
            }
            Icon(painterResource(step.icon), contentDescription = null, tint = tint, modifier = Modifier.size(Sizes.heroIcon))
        }
    }
}

private val UpdateHeroStep.icon: Int
    get() = when (this) {
        UpdateHeroStep.Checking -> DesignR.drawable.ic_refresh
        UpdateHeroStep.UpToDate -> DesignR.drawable.ic_check
        UpdateHeroStep.NotConfigured -> DesignR.drawable.ic_build
        UpdateHeroStep.Available, UpdateHeroStep.Downloading, UpdateHeroStep.Installing -> DesignR.drawable.ic_deployed_code_update
        UpdateHeroStep.Failed -> DesignR.drawable.ic_close
    }
