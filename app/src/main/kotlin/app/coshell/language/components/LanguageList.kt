package app.coshell.language.components

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.core.designsystem.components.CoShellRadioMark
import app.coshell.core.designsystem.components.CoShellRowGroup
import app.coshell.language.LanguageChoice

/**
 * The list of the language sheet: each language by its own name with, below, its name in the interface language
 * (none for the language in effect); «Как в телефоне» shows what the phone gives now. A tap picks ([onPick]).
 */
@Composable
internal fun LanguageList(
    choices: List<LanguageChoice>,
    selected: LanguageChoice?,
    phoneLanguage: String,
    uiLanguage: String,
    onPick: (LanguageChoice) -> Unit,
) {
    CoShellRowGroup {
        choices.forEach { choice ->
            row {
                val (title, subtitle) = when {
                    choice.keep -> stringResource(R.string.language_keep_russian) to stringResource(R.string.language_keep_hint)
                    choice.tag == null ->
                        stringResource(R.string.language_follow_phone) to stringResource(R.string.language_phone_now, stringResource(endonymRes(phoneLanguage)))
                    else -> stringResource(endonymRes(choice.tag)) to stringResource(nameRes(choice.tag)).takeIf { choice.tag != uiLanguage }
                }
                CoShellListRow(
                    title = title,
                    subtitle = subtitle,
                    trailing = { CoShellRadioMark(selected = choice == selected) },
                    onClick = { onPick(choice) },
                )
            }
        }
    }
}

/** The language's own name: «Русский», «English», «Español». */
@StringRes
internal fun endonymRes(tag: String): Int = when (tag) {
    "en" -> R.string.language_endonym_en
    "es" -> R.string.language_endonym_es
    else -> R.string.language_endonym_ru
}

/** The language's name in the interface language: «английский» in Russian, «Spanish» in English. */
@StringRes
private fun nameRes(tag: String): Int = when (tag) {
    "en" -> R.string.language_name_en
    "es" -> R.string.language_name_es
    else -> R.string.language_name_ru
}
