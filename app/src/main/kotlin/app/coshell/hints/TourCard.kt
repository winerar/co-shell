package app.coshell.hints

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.release.StoryNoticeCard

/**
 * «Знакомство с приложением · Смотреть ✕» at the top of a new «Обзор» (#598, board `Tour598Home`): only on a new install
 * (or after «Показать подсказки заново»), «Смотреть» or «✕» put it away — [HintCenter.tour].
 */
@Composable
fun TourCard(onOpen: () -> Unit, onDismiss: () -> Unit, modifier: Modifier = Modifier) =
    StoryNoticeCard(stringResource(R.string.tour_hint), stringResource(R.string.tour_title), onOpen, onDismiss, modifier)
