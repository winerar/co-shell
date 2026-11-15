package app.coshell.language

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.categories.ResourceCategoryNames
import app.coshell.core.domain.RenameStarterCategoriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * The language choice (#352): which question to ask over «Обзор» ([prompt]), what is chosen now ([chosen]),
 * and applying a pick. On the first launch the starter categories already exist in the phone's language —
 * a different pick renames those nobody touched. [apply]'s `restart` recreates the screen in the new language.
 */
@HiltViewModel
class LanguageViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val renameStarter: RenameStarterCategoriesUseCase,
) : ViewModel() {
    private val _prompt = MutableStateFlow(AppLanguage.prompt(context))
    val prompt: StateFlow<LanguagePrompt> = _prompt.asStateFlow()

    /** Saved choice; `null` = follow the phone. */
    val chosen: String? get() = AppLanguage.chosen(context)

    /** The phone's language as the app would use it («Как в телефоне · сейчас — …»). */
    val phoneLanguage: String get() = AppLanguage.pick(null, AppLanguage.phoneLanguages())

    /** Saves [tag] (from the sheet over «Обзор» when [fromPrompt]); calls [restart] if the language changes. */
    fun apply(tag: String?, fromPrompt: Boolean, restart: () -> Unit) {
        val before = AppLanguage.current()
        val after = AppLanguage.pick(tag, AppLanguage.phoneLanguages())
        val firstLaunch = fromPrompt && _prompt.value == LanguagePrompt.FIRST
        if (fromPrompt) AppLanguage.answer(context, tag) else AppLanguage.choose(context, tag)
        _prompt.value = LanguagePrompt.NONE
        if (after == before) return
        viewModelScope.launch {
            if (firstLaunch) renameStarter(ResourceCategoryNames(context) { before }, ResourceCategoryNames(context) { after })
            restart()
        }
    }
}
