package app.coshell.privacy

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellIconButton
import app.coshell.core.designsystem.R as DesignR

/**
 * «Глаз» in the header of «Обзор» (#145, board `Priv145Shown`, poll «А»): hides or shows every sum in the app at once.
 * Nothing in previews and tests (no [LocalPrivacyCenter]).
 */
@Composable
fun EyeButton() {
    val center = LocalPrivacyCenter.current ?: return
    val hidden by center.hidden.collectAsStateWithLifecycle()
    CoShellIconButton(onClick = center::toggleHidden) {
        Icon(
            painterResource(if (hidden) DesignR.drawable.ic_visibility_off else DesignR.drawable.ic_visibility),
            contentDescription = stringResource(if (hidden) R.string.eye_show else R.string.eye_hide),
        )
    }
}
