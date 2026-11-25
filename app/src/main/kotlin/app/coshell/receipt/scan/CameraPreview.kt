package app.coshell.receipt.scan

import android.util.Size
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.util.concurrent.Executors

/**
 * The back camera as a preview, with every frame given to [QrAnalyzer] (#526). [onCamera] gets the bound camera
 * (for the torch; `null` when it is gone). Nothing is recorded or saved.
 */
@Composable
internal fun CameraPreview(onText: (String) -> Unit, onCamera: (Camera?) -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnText by rememberUpdatedState(onText)
    val currentOnCamera by rememberUpdatedState(onCamera)
    val previewView = androidx.compose.runtime.remember {
        PreviewView(context).apply { scaleType = PreviewView.ScaleType.FILL_CENTER }
    }
    DisposableEffect(lifecycleOwner) {
        val executor = Executors.newSingleThreadExecutor()
        // Frames are read on [executor]; the text goes on to the screen (and navigation) on the main thread.
        val handoff = MainThreadText(ContextCompat.getMainExecutor(context)) { currentOnText(it) }
        val analyzer = QrAnalyzer(handoff)
        val future = ProcessCameraProvider.getInstance(context)
        var provider: ProcessCameraProvider? = null
        future.addListener(
            {
                val bound = future.get()
                provider = bound
                val preview = Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }
                val analysis = ImageAnalysis.Builder()
                    .setResolutionSelector(analysisResolution)
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build().also { it.setAnalyzer(executor, analyzer) }
                runCatching {
                    bound.unbindAll()
                    currentOnCamera(bound.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis))
                }.onFailure { currentOnCamera(null) }
            },
            ContextCompat.getMainExecutor(context),
        )
        onDispose {
            handoff.close()
            provider?.unbindAll()
            currentOnCamera(null)
            executor.shutdown()
            analyzer.close()
        }
    }
    AndroidView(factory = { previewView }, modifier = modifier)
}

/**
 * Frames for the analyzer at ~1280×960 (#712): CameraX's default 640×480 gives a dense receipt QR (a long AFIP link)
 * 2–3 pixels a module unless the phone is held close and straight; twice the side lets it be read from further away
 * and at an angle. Closest higher first, then lower — any phone gets some size.
 */
private val analysisResolution = ResolutionSelector.Builder()
    .setResolutionStrategy(ResolutionStrategy(Size(1280, 960), ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER))
    .build()
