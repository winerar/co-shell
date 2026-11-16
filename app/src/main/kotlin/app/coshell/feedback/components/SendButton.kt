package app.coshell.feedback.components

import androidx.compose.foundation.layout.Spacer
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.designsystem.tokens.Sizes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellButton
import app.coshell.core.designsystem.R as DesignR

/** Full-width "Send" button. */
@Composable
internal fun SendButton(enabled: Boolean, onClick: () -> Unit) {
    CoShellButton(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
        Icon(painterResource(DesignR.drawable.ic_send), contentDescription = null, Modifier.size(Sizes.buttonIcon))
        Spacer(Modifier.width(Spacing.s))
        Text(stringResource(R.string.feedback_send))
    }
}
