package app.coshell.core.domain.update

/**
 * Called on app start. If the app was just updated to the release remembered before installing,
 * returns that release once (to show "what's new") and forgets it. A remembered release that is
 * still newer than the installed one means the install was cancelled — it is kept for next time.
 */
class WhatsNewUseCase(
    private val store: UpdateStateStore,
    private val installed: InstalledVersion,
) {
    suspend operator fun invoke(): AppRelease? {
        val pending = store.pendingRelease() ?: return null
        if (pending.versionCode > installed.versionCode) return null
        store.setPendingRelease(null)
        return pending.takeIf { it.versionCode == installed.versionCode && it.notes.isNotBlank() }
    }
}
