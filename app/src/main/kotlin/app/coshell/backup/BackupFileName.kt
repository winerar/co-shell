package app.coshell.backup

import kotlinx.datetime.LocalDate

/** Backup file name with the date, so several copies sort and do not overwrite each other. */
fun backupFileName(date: LocalDate): String = "co-shell-$date.json"

/** MIME type for "save as"; for opening we also accept any type — some file managers lose it. */
const val BACKUP_MIME_TYPE = "application/json"
