package app.coshell.notices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.core.domain.TransactionFilter
import app.coshell.core.domain.TransactionRepository
import app.coshell.core.domain.notices.NoticeKind
import app.coshell.core.domain.notices.NoticeSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * The card «Напоминать вечером о тратах?» on «Обзоре» (#745, board `Tip745Offer`): after the first entry, until it is answered once
 * («Включить» or «Не сейчас»); never when the evening reminder is already on. Hidden until the saved state is loaded.
 */
@HiltViewModel
class NoticeOfferViewModel @Inject constructor(
    private val settings: NoticeSettings,
    transactions: TransactionRepository,
) : ViewModel() {

    val visible: StateFlow<Boolean> = combine(
        settings.offerAnswered,
        settings.enabled,
        transactions.observeTransactions(TransactionFilter(limit = 1)).map { it.isNotEmpty() },
    ) { answered, kinds, hasEntries -> hasEntries && !answered && NoticeKind.EVENING !in kinds }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun enable() {
        viewModelScope.launch {
            settings.setEnabled(NoticeKind.EVENING, true)
            settings.setOfferAnswered()
        }
    }

    fun later() {
        viewModelScope.launch { settings.setOfferAnswered() }
    }
}
