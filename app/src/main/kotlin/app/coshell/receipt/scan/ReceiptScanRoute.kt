package app.coshell.receipt.scan

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import app.coshell.core.domain.receipts.ReceiptFormats
import kotlinx.coroutines.delay

/** How long «Это не QR чека» stays; the same wrong code is not shown again while it is in the frame. */
private const val BAD_MESSAGE_MS = 3_000L

/**
 * Receipt QR scanner (#526): asks for the camera on the first open, reads codes, hands the text of a receipt QR to
 * [onReceipt] once. [onManual] — «Ввести вручную» (also when the camera is refused).
 */
@Composable
fun ReceiptScanRoute(onReceipt: (String) -> Unit, onManual: () -> Unit, onClose: () -> Unit) {
    val context = LocalContext.current
    var granted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    var asked by remember { mutableStateOf(false) }
    val request = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted = it }
    LaunchedEffect(Unit) {
        if (!granted && !asked) {
            asked = true
            request.launch(Manifest.permission.CAMERA)
        }
    }

    var bad by remember { mutableStateOf(false) }
    var badText by remember { mutableStateOf<String?>(null) }
    var done by remember { mutableStateOf(false) }
    LaunchedEffect(badText) {
        if (badText != null) {
            bad = true
            delay(BAD_MESSAGE_MS)
            bad = false
            badText = null
        }
    }

    ReceiptScanScreen(
        granted = granted,
        badVisible = bad,
        onText = { text ->
            if (done) return@ReceiptScanScreen
            if (ReceiptFormats.readExact(text) != null) {
                done = true
                onReceipt(text)
            } else if (badText != text) {
                badText = text
            }
        },
        onAllow = { request.launch(Manifest.permission.CAMERA) },
        onBadDismiss = { bad = false },
        onManual = onManual,
        onClose = onClose,
    )
}
