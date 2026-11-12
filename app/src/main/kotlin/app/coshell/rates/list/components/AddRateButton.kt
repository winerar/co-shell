package app.coshell.rates.list.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.dashedBorder
import app.coshell.core.designsystem.motion.Tactile
import app.coshell.core.designsystem.motion.tactileClickable
import app.coshell.core.designsystem.theme.accentInk
import app.coshell.core.designsystem.tokens.PillShape
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.designsystem.R as DesignR

/** «＋ Добавить курс»: a dashed pill in accent ink under the manual rates (board `Rates`). */
@Composable
internal fun AddRateButton(onClick: () -> Unit) {
    val ink = accentInk()
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.l, vertical = Spacing.s)
            .height(Sizes.wideButton)
            .tactileClickable(Tactile.Control, PillShape, onClick = onClick)
            .dashedBorder(MaterialTheme.colorScheme.outline, Sizes.wideButton),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.s, Alignment.CenterHorizontally),
    ) {
        Icon(painterResource(DesignR.drawable.ic_add), contentDescription = null, tint = ink, modifier = Modifier.size(Sizes.buttonIcon))
        Text(stringResource(R.string.rates_manual_add), style = MaterialTheme.typography.titleSmall, color = ink)
    }
}
