package app.coshell.importer

/**
 * A full JSON copy of the database taken right before an import writes anything (ROADMAP 5c).
 * Returns `false` if the copy could not be written — the import then does not start.
 * Android implementation — [app.coshell.importer.data.FilesPreImportBackup].
 */
fun interface PreImportBackup {
    suspend fun save(): Boolean
}
