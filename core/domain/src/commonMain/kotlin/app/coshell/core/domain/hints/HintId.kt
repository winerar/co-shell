package app.coshell.core.domain.hints

/** What a hint is about (`docs/agents/hints.md`): a word, a gesture or a one-time warm moment; all of them show as a card in the screen. */
enum class HintKind { WORD, GESTURE, MOMENT }

/**
 * Every hint the app can show. Declaration order is the priority order (words → gestures → moments), see [HintPlanner].
 * [key] is stored on the phone as «already shown» — never rename it; a removed hint moves to [HintId.retired].
 */
enum class HintId(val key: String, val kind: HintKind) {
    OVERVIEW_EMPTY("overview_empty", HintKind.WORD),
    ACCOUNTS("accounts", HintKind.WORD),
    ENTRY_MINIMAL("entry_minimal", HintKind.WORD),
    SCAN_RECEIPT("scan_receipt", HintKind.WORD),
    KEYPAD_MATH("keypad_math", HintKind.WORD),
    ENTRY_REFUND("entry_refund", HintKind.WORD),
    CLIPBOARD("clipboard", HintKind.WORD),
    CATEGORIES("categories", HintKind.WORD),
    BUDGETS("budgets", HintKind.WORD),
    DEBTS("debts", HintKind.WORD),
    REPORTS("reports", HintKind.WORD),
    TAGS("tags", HintKind.WORD),
    FAMILY("family", HintKind.WORD),
    CURRENCY_RECEIPT("currency_receipt", HintKind.WORD),
    NOTES("notes", HintKind.WORD),
    RECURRING("recurring", HintKind.WORD),
    HOLD_SELECT("hold_select", HintKind.GESTURE),
    SWIPE_TABS("swipe_tabs", HintKind.GESTURE),
    HOLD_PLUS("hold_plus", HintKind.GESTURE),
    TEMPLATE("template", HintKind.GESTURE),
    EDIT_TAP("edit_tap", HintKind.GESTURE),
    HOLD_AMOUNT("hold_amount", HintKind.GESTURE),
    SHAKE_UNDO("shake_undo", HintKind.GESTURE),
    FLIP_CARD("flip_card", HintKind.GESTURE),
    APP_SHORTCUTS("app_shortcuts", HintKind.GESTURE),
    FIRST_ENTRY("first_entry", HintKind.MOMENT),
    FIRST_LIMIT("first_limit", HintKind.MOMENT),
    FIRST_WEEK("first_week", HintKind.MOMENT),
    WELCOME_BACK("welcome_back", HintKind.MOMENT),
    MONTH_END("month_end", HintKind.MOMENT),
    LIMIT_80("limit_80", HintKind.MOMENT),
    BACKUP("backup", HintKind.MOMENT),
    THEME("theme", HintKind.MOMENT),
    CURRENCY_ACCOUNT("currency_account", HintKind.MOMENT),
    PIN_FORGOT("pin_forgot", HintKind.MOMENT),
    ;

    companion object {
        /** Keys of hints that no longer exist: kept so an old «shown» record never revives something else. */
        val retired: Set<String> = emptySet()

        fun fromKey(key: String): HintId? = entries.firstOrNull { it.key == key }
    }
}
