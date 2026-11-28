package app.coshell.newmark

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellStatusChip
import app.coshell.core.designsystem.components.StatusTone

/** The places wearing the «новое» pill now, the ways to them included (`NewMarkCenter`). */
@Composable
fun newMarks(viewModel: NewMarksViewModel = hiltViewModel()): Set<String> {
    val marks by viewModel.current.collectAsStateWithLifecycle()
    return marks
}

/**
 * The place behind [key] is on screen (a section of it, not a row to tap): the mark stays for this visit and goes away for good.
 * True — it was new on this visit.
 */
@Composable
fun seenMark(key: String, viewModel: NewMarksViewModel = hiltViewModel()): Boolean {
    val marks by viewModel.current.collectAsStateWithLifecycle()
    var wasNew by rememberSaveable(key) { mutableStateOf(false) }
    val isNew = key in marks
    LaunchedEffect(isNew) {
        if (isNew) {
            wasNew = true
            viewModel.opened(key)
        }
    }
    return wasNew || isNew
}

/** The «новое» pill for a row's `mark` slot when [isNew]; null otherwise. */
fun newMarkPill(isNew: Boolean): (@Composable () -> Unit)? =
    if (isNew) {
        { CoShellStatusChip(stringResource(R.string.new_mark), StatusTone.Fresh) }
    } else {
        null
    }
