package app.coshell.rates.list.components

import app.coshell.core.designsystem.theme.accentInk
import app.coshell.core.designsystem.tokens.Spacing
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import app.coshell.R

/** Where rates come from; open.er-api's terms ask for the "Rates By Exchange Rate API" link. */
@Composable
internal fun RatesSources() {
    val linkStyle = TextLinkStyles(SpanStyle(color = accentInk(), textDecoration = TextDecoration.Underline))
    val text = buildAnnotatedString {
        append(stringResource(R.string.rates_sources))
        append(' ')
        withLink(LinkAnnotation.Url(ATTRIBUTION_URL, linkStyle)) { append(stringResource(R.string.rates_attribution)) }
    }
    Text(
        text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(Spacing.l),
    )
}

private const val ATTRIBUTION_URL = "https://www.exchangerate-api.com"
