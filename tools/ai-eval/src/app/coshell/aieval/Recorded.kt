package app.coshell.aieval

import app.coshell.core.domain.ai.LanguageModel
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

/**
 * Model answers recorded on a phone (#742, «проверка без телефона»): the bench saves every request and answer, the file goes to
 * `docs/samples/ai/answers/`, and the JVM tests replay it through the same code — a change of a prompt check or a parser is caught in
 * `check` without a phone. A request the file does not have (the prompt changed) answers `null`, as a model that failed.
 */
class RecordingModel(private val inner: LanguageModel) : LanguageModel {
    private val answers = LinkedHashMap<String, String?>()
    override val available: Boolean get() = inner.available
    override val cheap: Boolean get() = inner.cheap

    override suspend fun complete(system: String, prompt: String): String? =
        inner.complete(system, prompt).also { answers[key(system, prompt)] = it }

    /** `{"model": …, "scores": "a 1/2, b 3/4", "answers": [{"system", "prompt", "answer"}]}`; [scores] — [AiEval.Score.short] of the run. */
    fun toJson(model: String, scores: List<String>): String {
        val items = answers.map { (k, v) ->
            val (system, prompt) = k.split(SEP, limit = 2)
            JsonObject(mapOf("system" to JsonPrimitive(system), "prompt" to JsonPrimitive(prompt), "answer" to (v?.let(::JsonPrimitive) ?: JsonNull)))
        }
        return Json { prettyPrint = true }.encodeToString(JsonObject.serializer(), JsonObject(mapOf("model" to JsonPrimitive(model), "scores" to JsonPrimitive(scores.joinToString(", ")), "answers" to JsonArray(items))))
    }

    companion object {
        internal const val SEP = "\u0000"
        internal fun key(system: String, prompt: String) = system + SEP + prompt
    }
}

/** Answers from a [RecordingModel] file, by exact request. */
class ReplayModel(json: String) : LanguageModel {
    val model: String
    private val answers: Map<String, String?>
    var missing = 0
        private set

    init {
        val root = Json.parseToJsonElement(json).jsonObject
        model = (root["model"] as? JsonPrimitive)?.contentOrNull.orEmpty()
        answers = root.getValue("answers").jsonArray.associate { e ->
            val o = e.jsonObject
            fun s(k: String) = (o[k] as? JsonPrimitive)?.contentOrNull
            RecordingModel.key(s("system").orEmpty(), s("prompt").orEmpty()) to s("answer")
        }
    }

    override val available: Boolean = true

    override suspend fun complete(system: String, prompt: String): String? {
        val key = RecordingModel.key(system, prompt)
        if (key !in answers) missing++
        return answers[key]
    }
}
