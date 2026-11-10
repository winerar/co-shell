package app.coshell.wipe

/**
 * The copy taken before «Стереть все данные»: a JSON file the user can find and restore from
 * («Ещё → Резервная копия»). Returns the file name, or `null` if it could not be written — then
 * nothing is wiped. Android implementation — [app.coshell.wipe.data.DownloadsWipeBackup].
 */
fun interface WipeBackup {
    suspend fun save(): String?
}
