package app.coshell.ai

import android.app.ActivityManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/** How much memory the phone has (#587): the model needs 6 GB (tiers in `docs/research/on-device-ai.md`). */
fun interface DeviceMemory {
    /** Total memory in megabytes (the OS reports a little under the marketing number: 6 GB phones show ≈ 5,6 GB). */
    fun totalMb(): Long

    companion object {
        /** The least the model runs well on (the «средний» tier: ≥ 5600 MB reported). */
        const val MIN_MB = 5_600L
    }
}

class SystemDeviceMemory @Inject constructor(@ApplicationContext private val context: Context) : DeviceMemory {
    override fun totalMb(): Long {
        val info = ActivityManager.MemoryInfo()
        (context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).getMemoryInfo(info)
        return info.totalMem / (1024 * 1024)
    }
}
