package app.coshell.ui.ai

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellStatusChip
import app.coshell.core.designsystem.components.StatusTone

/**
 * «бета» (#643, poll 2026-11-23 «А», board `NbBetaA`): a status capsule in the second accent next to everything that is not stable
 * yet — every AI function and any other feature until its stability criteria are met (owner: criteria are set when work starts).
 */
@Composable
fun BetaMark(modifier: Modifier = Modifier) = CoShellStatusChip(stringResource(R.string.beta), StatusTone.Manual, modifier)
