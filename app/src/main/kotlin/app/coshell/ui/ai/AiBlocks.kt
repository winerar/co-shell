package app.coshell.ui.ai

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.coshell.core.designsystem.R as DesignR
import app.coshell.core.designsystem.category.IconBadge
import app.coshell.core.designsystem.components.CardTone
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.core.designsystem.components.MenuIcon
import app.coshell.core.designsystem.theme.LocalHeroPalette
import app.coshell.core.designsystem.tokens.Spacing

/**
 * The one main block of the AI screens («ИИ на телефоне», «Облачный разбор»; boards `AiOff`, `CloudOff`): a sparkle, the state, a sentence
 * and [extra] under them (a chip, a bar).
 */
@Composable
fun AiStatusCard(title: String, text: String, modifier: Modifier = Modifier, extra: @Composable ColumnScope.() -> Unit = {}) {
    CoShellCard(modifier.fillMaxWidth(), tone = CardTone.Hero) {
        Column(Modifier.padding(Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
            IconBadge(DesignR.drawable.ic_spark, LocalHeroPalette.current.amount)
            Text(title, style = MaterialTheme.typography.headlineSmall, color = LocalHeroPalette.current.content)
            Text(text, style = MaterialTheme.typography.bodyMedium, color = LocalHeroPalette.current.hint)
            extra()
        }
    }
}

/** A fact or a «where it works» row of the AI screens: a round icon, a title and a grey line. */
@Composable
fun AiFactRow(icon: Int, title: String, text: String) =
    CoShellListRow(title = title, subtitle = text, leading = { MenuIcon(icon) })
