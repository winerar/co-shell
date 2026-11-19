package app.coshell.receipt.scan

import androidx.camera.core.Camera
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import app.coshell.R
import app.coshell.core.designsystem.R as DesignR
import app.coshell.core.designsystem.components.CoShellAlertDialog
import app.coshell.core.designsystem.components.CoShellButton
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.components.CoShellIconButton
import app.coshell.core.designsystem.components.CoShellNoticePill
import app.coshell.core.designsystem.components.CoShellScanFrame
import app.coshell.core.designsystem.components.coShellIconButtonColors
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.ui.top.ScreenTopBar

/**
 * The receipt QR scanner (#526, boards «QrScan», «QrBad», «QrNoCam»): the camera under a frame, a line of help, the
 * flashlight and «Ввести вручную» at the bottom. Without the camera permission — a card with «Разрешить» instead.
 */
@Composable
internal fun ReceiptScanScreen(
    granted: Boolean,
    badVisible: Boolean,
    onText: (String) -> Unit,
    onAllow: () -> Unit,
    onBadDismiss: () -> Unit,
    onManual: () -> Unit,
    onClose: () -> Unit,
) {
    var camera by remember { mutableStateOf<Camera?>(null) }
    var torch by remember { mutableStateOf(false) }
    LaunchedEffect(camera, torch) { camera?.cameraControl?.enableTorch(torch) }

    Box(Modifier.fillMaxSize()) {
        if (granted) CameraPreview(onText = onText, onCamera = { camera = it }, modifier = Modifier.fillMaxSize())
        Column(Modifier.fillMaxSize()) {
            ScreenTopBar(title = stringResource(R.string.scan_title), onBack = onClose)
        }
        if (granted) {
            CoShellScanFrame(Modifier.align(Alignment.Center))
            Column(
                Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(Spacing.l),
                verticalArrangement = Arrangement.spacedBy(Spacing.m),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CoShellNoticePill(
                    visible = badVisible,
                    icon = DesignR.drawable.ic_qr_code_scanner,
                    text = stringResource(R.string.scan_bad),
                    onClick = onBadDismiss,
                    dismissLabel = stringResource(R.string.action_close),
                    onDismiss = onBadDismiss,
                )
                CoShellCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.fillMaxWidth().padding(Spacing.m), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.scan_hint), style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                        Text(
                            stringResource(R.string.scan_hint_offline),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.s), verticalAlignment = Alignment.CenterVertically) {
                    if (camera?.cameraInfo?.hasFlashUnit() == true) {
                        CoShellIconButton(
                            onClick = { torch = !torch },
                            colors = coShellIconButtonColors(container = MaterialTheme.colorScheme.surfaceContainerHigh),
                        ) {
                            Icon(
                                painterResource(if (torch) DesignR.drawable.ic_flashlight_on else DesignR.drawable.ic_flashlight_off),
                                contentDescription = stringResource(R.string.scan_torch),
                            )
                        }
                    }
                    CoShellButton(onClick = onManual, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.scan_manual)) }
                }
            }
        } else {
            CoShellAlertDialog(
                title = stringResource(R.string.scan_cam_title),
                text = stringResource(R.string.scan_cam_text),
                confirmLabel = stringResource(R.string.scan_cam_allow),
                onConfirm = onAllow,
                onDismiss = onClose,
                dismissLabel = stringResource(R.string.scan_manual),
                onDismissClick = onManual,
                icon = DesignR.drawable.ic_qr_code_scanner,
            )
        }
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun ReceiptScanNoCameraPreview() {
    CoShellTheme { ReceiptScanScreen(granted = false, badVisible = false, onText = {}, onAllow = {}, onBadDismiss = {}, onManual = {}, onClose = {}) }
}
