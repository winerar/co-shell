package app.coshell.aibench

/** A model to try: the file is downloaded from Hugging Face (public, Apache 2.0, no login) into the app's folder. */
data class Candidate(val id: String, val label: String, val repo: String, val file: String, val sizeMb: Int, val thinkingOff: Boolean = true, val gated: Boolean = false) {
    val url get() = "https://huggingface.co/$repo/resolve/main/$file"
}

// Qwen 3 / 3.5 removed (#742): their .litertlm loads ~30 s on the phone — too long for the app; Gemma 4 E2B is the app's model.
val CANDIDATES = listOf(
    // Gated on Hugging Face (Gemma terms): needs a free account, accepting the terms on the model page and a read token.
    Candidate("gemma3n-e2b", "Gemma 3n E2B (нужен токен)", "google/gemma-3n-E2B-it-litert-lm", "gemma-3n-E2B-it-int4.litertlm", 3660, gated = true),
    Candidate("gemma4-e2b", "Gemma 4 E2B", "litert-community/gemma-4-E2B-it-litert-lm", "gemma-4-E2B-it.litertlm", 2470),
    Candidate("gemma4-e4b", "Gemma 4 E4B", "litert-community/gemma-4-E4B-it-litert-lm", "gemma-4-E4B-it.litertlm", 3490),
)

/** What the phone's memory suggests (the same tiers as in docs/research/on-device-ai.md). */
fun tierFor(totalRamMb: Long): String = when {
    totalRamMb >= 7600 -> "сильный (≥ 8 ГБ): модель класса 4B"
    totalRamMb >= 5600 -> "средний (6–8 ГБ): модель класса 1,7–2B"
    else -> "слабый (< 6 ГБ): только код без ИИ"
}
