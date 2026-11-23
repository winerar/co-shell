package app.coshell.core.domain.ai

/** Whether the person allowed the cloud model (#635). Off by default; kept per phone, not synced. */
interface CloudAiSettings {
    val enabled: Boolean
}

/**
 * The [LanguageModel] the app asks (#635): the cloud model when the person turned it on and it is reachable, otherwise —
 * and whenever the cloud fails (no network, daily limit, server error) — the model on the phone. Without both: `null`, rules only.
 * Nothing leaves the phone while [settings] says no.
 */
class RoutedLanguageModel(
    private val cloud: LanguageModel,
    private val phone: LanguageModel,
    private val settings: CloudAiSettings,
) : LanguageModel {
    private val cloudAllowed get() = settings.enabled && cloud.available

    override val available: Boolean get() = cloudAllowed || phone.available

    override suspend fun complete(system: String, prompt: String): String? {
        if (cloudAllowed) cloud.complete(system, prompt)?.let { return it }
        return if (phone.available) phone.complete(system, prompt) else null
    }
}
