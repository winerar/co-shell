package app.coshell.core.model

/**
 * Default order of named lists — by name, alphabetically (owner, #338: «по умолчанию везде по алфавиту»).
 * Case-insensitive, «ё» sorts with «е», surrounding spaces ignored. Once the family drags a list into its own
 * order (#338, «Порядок»), that list goes by `sortOrder` instead ([MANUAL_ACCOUNT_ORDER], [MANUAL_CATEGORY_ORDER]).
 */
fun nameSortKey(name: String): String = name.trim().lowercase().replace('ё', 'е')

/** Compares by [nameSortKey], then by the exact name, so equal keys keep a stable order. */
fun <T> nameOrder(name: (T) -> String): Comparator<T> =
    compareBy<T> { nameSortKey(name(it)) }.thenBy { name(it) }

/** Accounts: active before archived, then alphabetically. */
val ACCOUNT_ORDER: Comparator<Account> =
    compareBy<Account> { it.archived }.then(nameOrder { it.name }).thenBy { it.id }

/** Accounts after a manual order (#338): active before archived, then by `sortOrder`, ties alphabetically. */
val MANUAL_ACCOUNT_ORDER: Comparator<Account> =
    compareBy<Account> { it.archived }.thenBy { it.sortOrder }.then(nameOrder { it.name }).thenBy { it.id }

/** The account order of a list: [MANUAL_ACCOUNT_ORDER] once [manual], else [ACCOUNT_ORDER]. */
fun accountOrder(manual: Boolean): Comparator<Account> = if (manual) MANUAL_ACCOUNT_ORDER else ACCOUNT_ORDER
