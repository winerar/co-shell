package app.coshell.core.model

/**
 * Default order of named lists — by name, alphabetically (owner, #338: «по умолчанию везде по алфавиту»).
 * Case-insensitive, «ё» sorts with «е», surrounding spaces ignored. A manual order (#338, mode «Порядок»)
 * will replace it per list; until then `sortOrder` is kept but not used for display.
 */
fun nameSortKey(name: String): String = name.trim().lowercase().replace('ё', 'е')

/** Compares by [nameSortKey], then by the exact name, so equal keys keep a stable order. */
fun <T> nameOrder(name: (T) -> String): Comparator<T> =
    compareBy<T> { nameSortKey(name(it)) }.thenBy { name(it) }

/** Accounts: active before archived, then alphabetically. */
val ACCOUNT_ORDER: Comparator<Account> =
    compareBy<Account> { it.archived }.then(nameOrder { it.name }).thenBy { it.id }
