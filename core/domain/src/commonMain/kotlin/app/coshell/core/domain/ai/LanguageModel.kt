package app.coshell.core.domain.ai

/**
 * The on-device model behind an interface (#587): the app implements it with LiteRT-LM (or nothing when the optional
 * package is not installed). Code never depends on a concrete model; everything works without it (rules first).
 */
interface LanguageModel {
    /** `false` while the package is not installed or the phone is too weak/hot: callers skip the model silently. */
    val available: Boolean

    /**
     * One request: [system] fixes the role and format, [prompt] is the data. Returns the raw text answer, or `null` when the
     * model failed or was cancelled. Cancelling the coroutine stops the generation.
     */
    suspend fun complete(system: String, prompt: String): String?

    /**
     * `true` when a request costs the phone nothing and is fast (the cloud model, #635): then every quick-note line is read by it
     * (payment method too), not only the lines the rules could not finish. The slow model on the phone keeps `false`.
     */
    val cheap: Boolean get() = false

    /**
     * The person is about to need the model (opened «Быстрые заметки», a receipt was not recognised): start loading it now so the
     * request does not wait for the load (#583 «оболочка» п. 7). Returns at once; a model with nothing to load ignores it.
     */
    suspend fun warm() = Unit

    /** The last request was refused because the daily limit of the cloud is over (#635); the caller says so once and the phone model carries on. */
    val limited: Boolean get() = false
}
