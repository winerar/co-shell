package app.coshell.transactions.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.transactions.DeleteTransactionWithUndo
import app.coshell.core.domain.ai.NoLanguageModel
import app.coshell.core.domain.ai.PayeeCategoryAi
import app.coshell.core.domain.ai.ReceiptAi
import app.coshell.receipt.inbox.UnreadReceipts
import app.coshell.core.domain.categoryguess.CategoryGuess
import app.coshell.core.domain.drafts.DraftRepository
import app.coshell.core.domain.drafts.InMemoryDraftRepository
import app.coshell.core.domain.settings.InMemoryScreenOptions
import app.coshell.core.domain.settings.ScreenOptionsStore
import app.coshell.core.domain.AccountRepository
import app.coshell.core.domain.CategoryRepository
import app.coshell.core.domain.SaveTransactionUseCase
import app.coshell.core.domain.TransactionFilter
import app.coshell.core.domain.TransactionRepository
import app.coshell.core.designsystem.components.keypad.AmountKeys
import app.coshell.core.domain.quickentry.AmountInText
import app.coshell.core.domain.quickentry.TipPrefs
import app.coshell.core.domain.quickentry.Tips
import app.coshell.core.domain.recurring.autoPaymentId
import app.coshell.core.domain.rates.CurrencyConverter
import app.coshell.core.domain.rates.ExchangeRateRepository
import app.coshell.core.domain.rates.RateBook
import app.coshell.core.domain.credit.CreditCard
import app.coshell.core.domain.quickentry.EntryDraftStore
import app.coshell.core.domain.quickentry.EntryDrafts
import app.coshell.core.domain.quickentry.InMemoryEntryDraftStore
import app.coshell.core.domain.receipts.Receipt
import app.coshell.core.domain.receipts.ReceiptAccounts
import app.coshell.core.domain.receipts.ReceiptFormats
import app.coshell.core.model.Account
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.Currencies
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.IdGenerator
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import app.coshell.navigation.TransactionEditDestination
import app.coshell.core.designsystem.components.keypad.KeypadKey
import app.coshell.transactions.edit.TransactionEditUiState.EntryStep
import app.coshell.transactions.edit.TransactionEditUiState.KeypadTarget
import app.coshell.transactions.edit.tip.EntryTip
import app.coshell.transactions.edit.tip.NEW_TIP_CATEGORY
import app.coshell.transactions.edit.tip.TipContext
import app.coshell.transactions.edit.tip.TipUi
import app.coshell.transactions.edit.tip.refreshed
import app.coshell.ui.offMain
import app.coshell.undo.SavedEntries
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * New or existing transaction. Validation lives in [validateForm], category choices in
 * [withCategoryOptions]; this class only loads data, keeps the state and saves.
 */
@HiltViewModel
class TransactionEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val accounts: AccountRepository,
    private val categories: CategoryRepository,
    private val transactions: TransactionRepository,
    private val saveTransaction: SaveTransactionUseCase,
    private val ids: IdGenerator,
    private val moneyFormat: MoneyFormat,
    private val clock: Clock,
    private val timeZone: TimeZone,
    private val rates: ExchangeRateRepository,
    private val deleteWithUndo: DeleteTransactionWithUndo,
    private val entryTemplates: EntryTemplates,
    private val entryTags: EntryTags,
    private val entryTip: EntryTip,
    private val screenOptions: ScreenOptionsStore = InMemoryScreenOptions(),
    private val drafts: DraftRepository = InMemoryDraftRepository(),
    private val categoryAi: PayeeCategoryAi = PayeeCategoryAi(NoLanguageModel),
    private val entryDrafts: EntryDraftStore = InMemoryEntryDraftStore(),
    private val savedEntries: SavedEntries = SavedEntries { _, _ -> },
    private val receiptAi: ReceiptAi = ReceiptAi(NoLanguageModel),
    private val unreadReceipts: UnreadReceipts = UnreadReceipts.NONE,
) : ViewModel() {

    /** The edited operation; `null` for a new one. «Дублировать» in the header copies it. */
    val transactionId: String? = savedStateHandle[TransactionEditDestination.ARG_TRANSACTION_ID]
    private val initialType: TransactionType = savedStateHandle.get<String>(TransactionEditDestination.ARG_TYPE)
        ?.let { name -> TransactionType.entries.firstOrNull { it.name == name } }
        ?: TransactionType.EXPENSE

    private val copyFromId: String? = savedStateHandle[TransactionEditDestination.ARG_COPY_FROM]

    /** Template from the route («Все шаблоны» → tap); applied once after the first load. */
    private var pendingTemplateId: String? = savedStateHandle[TransactionEditDestination.ARG_TEMPLATE_ID]
    /** The template the form was filled from; its usage is counted when the operation is saved. */
    private var templateId: String? = null
    /** «Провести» from «Скоро» (#143): the payment's day; applied with the route's template, cleared with it. */
    private var paymentDate: LocalDate? = savedStateHandle.get<String>(TransactionEditDestination.ARG_PAYMENT_DATE)
        ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

    /** «Провести» a credit card payment from «Скоро» (#686): a new transfer into this card for the minimum payment. */
    private val creditCardId: String? = savedStateHandle[TransactionEditDestination.ARG_CREDIT_CARD]

    /** A draft of a quick note from the route (#586): fills a new form once; saved — the draft is gone. */
    private val draftId: String? = savedStateHandle[TransactionEditDestination.ARG_DRAFT_ID]

    /** Text with a receipt QR link from the route (#526); applied once to a new form, like the template. */
    private var pendingReceipt: String? = savedStateHandle[TransactionEditDestination.ARG_RECEIPT_QR]
    /** The receipt the form was filled from; the amount follows the chosen account until the person types their own. */
    private var scannedQr: Receipt? = null
    private var scanAmountTouched = false

    private var existing: Transaction? = null
    /** Labels of the edited or copied operation: references the form does not change keep their form. */
    private var sourceLabels: List<String> = emptyList()
    private var accountsById: Map<String, Account> = emptyMap()
    private var allCategories: List<Category> = emptyList()
    private var recentCategoryIds: List<String> = emptyList()
    /** The newest operations of the family (#584 guesses the category from them); a manual choice stops the guessing. */
    private var history: List<Transaction> = emptyList()
    private var categoryTouched = false
    /** Known rates for the "debited" suggestion; `null` until loaded. */
    private var converter: CurrencyConverter? = null

    private val _state = MutableStateFlow(
        TransactionEditUiState(
            isLoading = true,
            isNew = transactionId == null,
            receiptUnread = savedStateHandle[TransactionEditDestination.ARG_RECEIPT_UNREAD] ?: false,
        ).let { it.copy(receiptAi = if (it.receiptUnread) receiptAiMode() else ReceiptAiMode.NONE) },
    )
    val state: StateFlow<TransactionEditUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch { load() }
        // What is typed is kept as it changes (a call, the app killed): the next «+» offers it (#323). Saved or discarded — cleared.
        viewModelScope.launch {
            _state.map { draftOf(it, accountsById, clock.now()) }
                .distinctUntilChanged { a, b -> a?.copy(savedAt = b?.savedAt ?: a.savedAt) == b }
                .collect { draft -> if (draft != null) entryDrafts.save(draft) }
        }
        viewModelScope.launch {
            // Parsing and indexing every known rate is heavy: off the main thread (#258).
            rates.observeRates().map { list -> CurrencyConverter(RateBook.of(list)) }.offMain().collect { built ->
                converter = built
                if (!_state.value.isLoading) updateForm { it }
            }
        }
        viewModelScope.launch {
            entryTags.observe().offMain().collect { choices -> _state.update { it.copy(tags = choices) } }
        }
    }

    // region Loading

    private suspend fun load() {
        val transaction = transactionId?.let { transactions.getTransaction(it) }
        existing = transaction
        val template = copyFromId?.let { transactions.getTransaction(it) }
        sourceLabels = (transaction ?: template)?.labels.orEmpty()
        val used = setOfNotNull(transaction?.accountId, transaction?.toAccountId, template?.accountId, template?.toAccountId)
        // Archived accounts and categories are hidden unless the edited transaction uses them.
        val withBalances = accounts.observeAccountsWithBalance(includeArchived = true).first()
            .filter { !it.account.archived || it.account.id in used }
        val accountList = withBalances.map { it.account }
        val balances = withBalances.associate { it.account.id to moneyFormat.format(it.balance) }
        val negative = withBalances.filter { it.balance.isNegative }.map { it.account.id }.toSet()
        accountsById = accountList.associateBy { it.id }
        allCategories = categories.observeCategories(includeArchived = true).first()
            .filter { !it.archived || it.id == transaction?.categoryId }

        val today = clock.now().toLocalDateTime(timeZone).date
        // The newest operations are enough for the last account, the category order and receipt currencies (#149).
        val recent = transactions.observeTransactions(TransactionFilter(limit = RECENT_OPERATIONS)).first()
        recentCategoryIds = recent.mapNotNull { it.categoryId }.distinct()
        history = recent
        val base = TransactionEditUiState(
            accounts = accountList.map { it.toOption(balances[it.id].orEmpty(), it.id in negative) },
            recentAccountIds = recent.map { it.accountId }.distinct().filter { it in accountsById }.take(RECENT_ACCOUNTS),
            today = today,
            receiptCurrencies = receiptCurrencySuggestions(recent, accountList),
            tags = _state.value.tags,
            receiptUnread = _state.value.receiptUnread,
            receiptAi = _state.value.receiptAi,
        )
        val loaded = when {
            transaction != null -> entryFormOf(transaction, base, allCategories, moneyFormat, timeZone)
            // «Дублировать»: the same operation as a new one today, straight on the amount.
            template != null -> entryFormOf(template, base, allCategories, moneyFormat, timeZone).copy(isNew = true, isCopy = true, date = base.today)
            else -> newEntryForm(base, initialType, accountList, recent, screenOptions.options.first().entryAccountId)
        }
        _state.value = loaded.withCategoryOptions(allCategories, recentCategoryIds)
        pendingTemplateId?.let { applyTemplate(it) }
        pendingTemplateId = null
        if (transaction == null && template == null) pendingReceipt?.let { applyReceipt(it) }
        pendingReceipt = null
        creditCardId?.takeIf { transaction == null && template == null }?.let { id ->
            val card = withBalances.firstOrNull { it.account.id == id } ?: return@let
            val minimum = card.account.credit?.let { CreditCard.status(it, card.balance, today).minPayment } ?: return@let
            _state.update { it.withCreditPayment(card.account, minimum, accountList.filter { a -> !a.archived }, moneyFormat) }
        }
        if (transaction == null && template == null) draftId?.let { applyDraft(it) }
        // A plain new sheet offers what the last closed one had (#323).
        if (isPlainNew(transaction, template)) {
            EntryDrafts.toOffer(entryDrafts.load(), clock.now())?.let { offer ->
                _state.update { it.copy(unsaved = unsavedUi(offer, clock.now(), allCategories, moneyFormat)) }
            }
        }
    }

    /** A new form from a draft of a quick note (#586); the mapping lives in `NoteDraftEntry.kt`. */
    private suspend fun applyDraft(id: String) {
        val draft = drafts.getDraft(id) ?: return
        _state.update { it.withNoteDraft(draft, accountsById, allCategories, recentCategoryIds, converter, moneyFormat, timeZone) }
    }

    // endregion

    /** A new operation from nothing: not an edit, a copy, a template, a receipt, a draft of notes or a credit card payment. */
    private fun isPlainNew(transaction: Transaction?, template: Transaction?): Boolean =
        transactionId == null && transaction == null && copyFromId == null && template == null && templateId == null && scannedQr == null && draftId == null && creditCardId == null &&
            !_state.value.receiptUnread

    /** «Продолжить» on the plate «Не сохранено» (#323): the draft goes back into the form. */
    fun onUnsavedContinue() {
        val draft = entryDrafts.load() ?: return _state.update { it.copy(unsaved = null) }
        _state.update { it.withDraft(draft, accountsById, allCategories, moneyFormat).withDebitSuggestion(converter, moneyFormat).withCategoryOptions(allCategories, recentCategoryIds) }
    }

    /** «✕» on the plate: the draft is forgotten. */
    fun onUnsavedDiscard() {
        entryDrafts.clear()
        _state.update { it.copy(unsaved = null) }
    }

    // region Receipt QR (#526)

    private suspend fun applyReceipt(text: String) {
        val qr = ReceiptFormats.read(text) ?: return
        scannedQr = qr
        scanAmountTouched = false
        val duplicate = transactions.findByReceiptKey(qr.key)?.let { found ->
            ScanDuplicate(
                date = found.date.toLocalDateTime(timeZone).date,
                amount = moneyFormat.format(found.amount),
                category = allCategories.firstOrNull { it.id == found.categoryId }?.name,
            )
        }
        _state.update { form ->
            if (qr.transfer != null) {
                form.withReceiptTransfer(qr, moneyFormat, duplicate).withCategoryOptions(allCategories, recentCategoryIds)
            } else {
                form.copy(scan = ScanUi(qr.merchant, qr.key, duplicate)).withReceipt(qr, moneyFormat)
                    .withDebitSuggestion(converter, moneyFormat)
                    .withGuessedCategory()
                    .withCategoryOptions(allCategories, recentCategoryIds)
            }
        }
        if (qr.transfer == null) askAiCategory()
        linkReceiptAccounts(qr)
    }

    /**
     * A bank receipt prints the last digits of the card or account (#577): a single account with them is chosen (both sides of a
     * transfer, #646); digits no account has yet — «Какой это счёт?», the answer is remembered in the account.
     */
    private fun linkReceiptAccounts(qr: Receipt) {
        val live = accountsById.values.filter { !it.archived }
        val debitDigits = qr.debitDigits
        val toDigits = qr.transfer?.toDigits
        val from = ReceiptAccounts.find(live, debitDigits)
        val to = ReceiptAccounts.find(live, toDigits)
        from?.let { setReceiptAccount(it.id, target = false) }
        to?.let { setReceiptAccount(it.id, target = true) }
        val ask = when {
            debitDigits != null && from == null -> ReceiptAccountAsk(debitDigits)
            toDigits != null && to == null -> ReceiptAccountAsk(toDigits, target = true)
            else -> null
        }
        if (ask != null && live.isNotEmpty()) _state.update { it.copy(receiptAccountAsk = ask) }
    }

    private fun setReceiptAccount(id: String, target: Boolean) = _state.update { form ->
        if (target) return@update form.copy(toAccountId = id.takeIf { it != form.accountId }, toAccountError = null)
        val chosen = form.copy(accountId = id, accountError = false, toAccountId = form.toAccountId?.takeIf { it != id })
        val qr = scannedQr
        if (qr != null && qr.transfer == null) chosen.withReceipt(qr, moneyFormat).withDebitSuggestion(converter, moneyFormat) else chosen
    }

    /** «Какой это счёт?» answered: the digits stay in the account, the account is chosen; a transfer may still ask for its other side. */
    fun onReceiptAccountPick(id: String) {
        val ask = _state.value.receiptAccountAsk ?: return
        _state.update { it.copy(receiptAccountAsk = null) }
        val account = accountsById[id] ?: return
        val linked = account.copy(cardDigits = ask.digits)
        accountsById = accountsById + (id to linked)
        setReceiptAccount(id, ask.target)
        viewModelScope.launch {
            accounts.saveAccount(linked)
            scannedQr?.let { qr -> if (!ask.target) linkReceiptAccounts(qr.copy(debitDigits = null)) }
        }
    }

    /** «Пропустить» — closing «Какой это счёт?»: the account stays as it is, nothing is remembered. */
    fun onReceiptAccountSkip() = _state.update { it.copy(receiptAccountAsk = null) }

    /**
     * «Вставить из буфера» (#321): [text] is what the clipboard held, read on the tap. A receipt of a known format fills the sheet
     * as a share does; otherwise the amount, its currency and the rest of the line (the note) are taken from the text.
     */
    fun onPasteText(text: String?) {
        viewModelScope.launch {
            val body = text?.trim().orEmpty()
            if (body.isNotEmpty() && ReceiptFormats.read(body) != null) {
                applyReceipt(body)
                _state.update { it.copy(clipboardNone = false) }
                return@launch
            }
            val accountCurrency = _state.value.accountId?.let { accountsById[it]?.currency }
            val fallback = accountCurrency ?: accountsById.values.firstOrNull()?.currency ?: CurrencyCode("USD")
            val found = body.takeIf { it.isNotEmpty() }
                ?.let { AmountInText.find(it, fallback, known = accountsById.values.map { a -> a.currency }.toSet()) }
            _state.update { form ->
                if (found == null) form.copy(clipboardNone = true)
                else form.withClipboardAmount(found, accountCurrency, moneyFormat)
                    .withDebitSuggestion(converter, moneyFormat)
                    .withGuessedCategory()
                    .withCategoryOptions(allCategories, recentCategoryIds)
            }
        }
    }

    fun onClipboardNoneDismiss() = _state.update { it.copy(clipboardNone = false) }

    /** «Ввести вручную» or «Отправить образец» in «Не удалось прочитать чек» (#561): the window closes. */
    fun onReceiptUnreadDismiss() = _state.update { it.copy(receiptUnread = false) }

    private var receiptReadJob: Job? = null

    private fun receiptAiMode(): ReceiptAiMode = when {
        unreadReceipts.text().isNullOrBlank() -> ReceiptAiMode.NONE
        !receiptAi.available -> ReceiptAiMode.OFF
        receiptAi.cloud -> ReceiptAiMode.CLOUD
        else -> ReceiptAiMode.PHONE
    }

    /**
     * «Прочитать с ИИ» (#593): the model reads the receipt no format knew; the total must be printed in it ([ReceiptAi]). Read — the sheet
     * is filled as from any receipt, with «Прочитано ИИ · проверьте сумму и дату»; nothing found — the window comes back without the button.
     */
    fun onReceiptAiRead() {
        val text = unreadReceipts.text()?.takeIf { it.isNotBlank() } ?: return
        _state.update { it.copy(receiptUnread = false, receiptReading = true) }
        receiptReadJob?.cancel()
        receiptReadJob = viewModelScope.launch {
            val live = accountsById.values.filter { !it.archived }
            val fallback = _state.value.accountId?.let { accountsById[it]?.currency } ?: live.firstOrNull()?.currency ?: CurrencyCode("USD")
            val receipt = receiptAi.read(text, fallback, live.map { it.currency }.toSet(), clock.now().toLocalDateTime(timeZone).date)
            if (receipt == null) {
                _state.update { it.copy(receiptReading = false, receiptUnread = true, receiptAiFailed = true) }
                return@launch
            }
            scannedQr = receipt
            scanAmountTouched = false
            _state.update { form ->
                form.copy(receiptReading = false).withReceipt(receipt, moneyFormat)
                    .withDebitSuggestion(converter, moneyFormat)
                    .withGuessedCategory()
                    .withCategoryOptions(allCategories, recentCategoryIds)
            }
            askAiCategory()
        }
    }

    /** «Отменить» in «Читаю чек…»: the reading stops, the blank sheet stays. */
    fun onReceiptAiCancel() {
        receiptReadJob?.cancel()
        _state.update { it.copy(receiptReading = false) }
    }

    /** «Всё равно записать»: the same receipt once more, on purpose. */
    fun onScanDuplicateAccept() = _state.update { form -> form.copy(scan = form.scan?.copy(duplicate = null)) }

    /** «Отмена» in the «Этот чек уже записан» window: nothing to add, the sheet closes. */
    fun onScanDuplicateCancel() = _state.update { it.copy(isDone = true) }

    // endregion

    // region Templates

    /** A chip of the strip: the same sheet, filled from the template. */
    fun onTemplatePick(id: String) {
        if (_state.value.isLoading) return
        viewModelScope.launch { applyTemplate(id) }
    }

    /** «✕» on the template pill: back to an empty new operation. */
    fun onTemplateClear() {
        templateId = null
        paymentDate = null
        _state.update { it.copy(payment = null) }
        viewModelScope.launch { load() }
    }

    /** «Пропустить этот раз» (#404): the payment is done without an operation; the sheet closes. */
    fun onPaymentSkip() {
        val id = templateId ?: return
        val day = paymentDate ?: return
        if (_state.value.isDone) return
        viewModelScope.launch {
            entryTemplates.paid(id, day)
            _state.update { it.copy(isDone = true) }
        }
    }

    private suspend fun applyTemplate(id: String) {
        val (draft, name) = entryTemplates.draft(id, clock.now(), accountsById, allCategories) ?: return
        // Another template picked in the strip is an ordinary operation, not the payment.
        if (id != templateId && templateId != null) paymentDate = null
        templateId = id
        val day = paymentDate
        val payment = day?.let { paymentUi(name, it, entryTemplates.history(id, it, timeZone), moneyFormat) }
        _state.update { form ->
            form.filledFrom(draft, name, moneyFormat).let { if (day != null) it.copy(date = day, payment = payment) else it }
                .withDebitSuggestion(converter, moneyFormat)
                .withCategoryOptions(allCategories, recentCategoryIds)
        }
    }

    /** «Сохранить как шаблон» for the saved operation (not for unsaved edits in the form). */
    fun saveAsTemplate(name: String, rememberAmount: Boolean) {
        val transaction = existing ?: return
        if (name.isBlank()) return
        viewModelScope.launch {
            val saved = entryTemplates.saveAs(transaction, name, rememberAmount)
            _state.update { it.copy(savedTemplateName = name.trim().takeIf { saved }, templateSaveFailed = !saved) }
        }
    }

    // endregion

    // region Form input

    fun onTypeChange(type: TransactionType) = _state.update {
        it.copy(type = type, refund = false, categoryError = false, toAccountError = null, categoryGuessed = false, categoryGuessSource = null, categoryByAi = false)
            .withGuessedCategory().withCategoryOptions(allCategories, recentCategoryIds)
    }

    /**
     * #584: a new operation without a manual category gets the one its seller or note had before; the guess follows
     * the form (note, type) until the person picks a category themselves.
     */
    private fun TransactionEditUiState.withGuessedCategory(): TransactionEditUiState {
        if (!isNew || isCopy || categoryTouched || (categoryId != null && !categoryGuessed)) return this
        val guess = CategoryGuess.suggest(history, type, allCategories, scan?.seller, scan?.key?.takeIf { it.isNotBlank() }, note)
        if (guess == null && categoryByAi) return this
        return copy(categoryId = guess?.categoryId, categoryGuessed = guess != null, categoryGuessSource = guess?.source, categoryByAi = false)
    }

    /** «↩ Возврат» chip of an expense (#208): the same category, the sign flips. */
    fun onRefundToggle(enabled: Boolean) = _state.update { it.copy(refund = enabled && it.type == TransactionType.EXPENSE) }

    fun onAmountChange(value: String) = _state.update {
        scanAmountTouched = true
        it.copy(amount = value, amountError = null, amountSuggested = false, suggestedRateDate = null)
    }

    fun onToAmountChange(value: String) = _state.update { it.copy(toAmount = value, toAmountError = null) }

    /** On the account step a tap also moves on: to the target account of a transfer, else to the amount. */
    fun onAccountSelect(id: String) = updateForm { form ->
        // A scanned amount in pesos is the purchase on a card in another currency: the layout follows the account
        // until the person types their own amount or leaves the expense.
        val qr = scannedQr?.takeIf { !scanAmountTouched && form.type == TransactionType.EXPENSE }
        val chosen = form.copy(accountId = id).let { if (qr != null) it.withReceipt(qr, moneyFormat) else it }
        chosen.copy(
            toAccountId = chosen.toAccountId?.takeIf { to -> to != id },
            accountError = false,
            amountError = null,
            toAccountError = null,
            receiptCurrencyError = null,
            step = chosen.step.afterAccount(chosen.isTransfer),
            keypadTarget = if (qr != null) chosen.keypadTarget else KeypadTarget.AMOUNT,
        )
    }

    fun onToAccountSelect(id: String) = _state.update {
        val step = if (it.step == EntryStep.TO_ACCOUNT) EntryStep.DETAILS else it.step
        it.copy(toAccountId = id, toAccountError = null, step = step)
    }

    fun onStepChange(step: EntryStep) = _state.update { it.copy(step = step) }

    fun onKeypadTarget(target: KeypadTarget) = _state.update { it.copy(keypadTarget = target) }

    /** A keypad key types into [TransactionEditUiState.keypadTarget]; «Готово» saves. */
    fun onKey(key: KeypadKey) {
        if (key == KeypadKey.Done) return save()
        val (target, text) = _state.value.typed(key)
        when (target) {
            KeypadTarget.AMOUNT -> onAmountChange(text)
            KeypadTarget.TO_AMOUNT -> onToAmountChange(text)
            KeypadTarget.RECEIPT -> onReceiptAmountChange(text)
        }
    }

    /** Selecting a folder selects the folder itself and opens its subcategories. */
    fun onCategorySelect(id: String) = _state.update {
        categoryTouched = true
        it.copy(categoryId = id, categoryError = false, categoryGuessed = false, categoryGuessSource = null, categoryByAi = false).withCategoryOptions(allCategories, recentCategoryIds)
    }

    fun onDateSelect(date: LocalDate) = updateForm { it.copy(date = date) }

    /** From Material's DatePicker, which works in UTC midnight millis. */
    fun onDatePicked(millisUtc: Long) =
        onDateSelect(Instant.fromEpochMilliseconds(millisUtc).toLocalDateTime(TimeZone.UTC).date)

    fun onNoteChange(value: String) {
        _state.update { it.copy(note = value).withGuessedCategory().withCategoryOptions(allCategories, recentCategoryIds) }
        askAiCategory()
    }

    private var aiJob: Job? = null

    /**
     * #594: a new expense whose seller or note nothing in the history or the brand dictionary knows asks the model for a category (when the
     * model is there); the sheet stays usable meanwhile and a manual pick wins. Caption «предложено ИИ» until the person picks.
     */
    private fun askAiCategory() {
        val s = _state.value
        val text = (s.scan?.seller ?: s.note).trim()
        val open = s.isNew && !s.isCopy && !categoryTouched && s.type == TransactionType.EXPENSE && (s.categoryId == null || s.categoryByAi)
        if (!categoryAi.available || !open || text.length < AI_MIN_TEXT) {
            aiJob?.cancel()
            return
        }
        aiJob?.cancel()
        aiJob = viewModelScope.launch {
            delay(AI_DELAY_MS)
            val options = allCategories.filter { it.type == CategoryType.EXPENSE && !it.archived }
            val name = categoryAi.suggest(text, options.map { it.name }) ?: return@launch
            val category = options.firstOrNull { it.name == name } ?: return@launch
            _state.update { cur ->
                val same = (cur.scan?.seller ?: cur.note).trim() == text
                if (categoryTouched || !same || (cur.categoryId != null && !cur.categoryByAi) || cur.categoryGuessed && !cur.categoryByAi) cur
                else cur.copy(categoryId = category.id, categoryGuessed = true, categoryGuessSource = null, categoryByAi = true, categoryError = false)
                    .withCategoryOptions(allCategories, recentCategoryIds)
            }
        }
    }

    /** A pill in the row or a row of the «Теги» sheet: on ↔ off. */
    fun onTagToggle(id: String) = _state.update {
        it.copy(tagIds = if (id in it.tagIds) it.tagIds - id else it.tagIds + id)
    }

    /** «Новый тег»: created (or found by name) and chosen at once. */
    fun onTagCreate(name: String, color: Long?) {
        viewModelScope.launch {
            val id = entryTags.create(name, color) ?: return@launch
            _state.update { if (id in it.tagIds) it else it.copy(tagIds = it.tagIds + id) }
        }
    }

    /** Turning the receipt on preselects the last receipt currency that differs from the account's. */
    fun onReceiptToggle(enabled: Boolean) = updateForm {
        val currency = it.receiptCurrency.ifEmpty { it.receiptCurrencyChoices.firstOrNull().orEmpty() }
        it.copy(
            receiptEnabled = enabled,
            receiptCurrency = currency,
            receiptCurrencyError = null,
            receiptAmountError = null,
            // The receipt amount is what one reads off the paper first; «списано» follows by the rate.
            keypadTarget = if (enabled) KeypadTarget.RECEIPT else KeypadTarget.AMOUNT,
        )
    }

    fun onReceiptCurrencyChange(value: String) = updateForm {
        it.copy(receiptCurrency = value.trim().uppercase(), receiptCurrencyError = null, receiptAmountError = null)
    }

    fun onReceiptAmountChange(value: String) = updateForm {
        scanAmountTouched = true
        it.copy(receiptAmount = value, receiptAmountError = null) }

    /** Changes that affect the receipt conversion also refresh the suggested debited amount. */
    private fun updateForm(change: (TransactionEditUiState) -> TransactionEditUiState) =
        _state.update { change(it).withDebitSuggestion(converter, moneyFormat) }

    // endregion

    // region Tips (#322)

    /** The purchase the open «Чаевые» sheet adds a tip to: validated and built, not saved yet. */
    private var tipMain: Transaction? = null

    private fun tipContext(main: Transaction) = TipContext(main, _state.value.date, accountsById, converter, moneyFormat)

    /** «＋ Чаевые»: the purchase must be valid first; then the sheet opens with the remembered account and category. */
    fun onTipOpen() {
        val current = _state.value
        if (!current.tipAvailable || current.isLoading || current.isDone) return
        val main = buildMain(current) ?: return
        tipMain = main
        viewModelScope.launch { _state.update { it.copy(tip = entryTip.open(tipContext(main), allCategories)) } }
    }

    /** Any change of the sheet: its texts are recomputed from the purchase and the rates. */
    fun onTipChange(change: (TipUi) -> TipUi) {
        val main = tipMain ?: return
        _state.update { form -> form.tip?.let { form.copy(tip = change(it).refreshed(tipContext(main))) } ?: form }
    }

    fun onTipClose() {
        tipMain = null
        _state.update { it.copy(tip = null) }
    }

    /** «Своя сумма» typed on the sheet's keypad; «Готово» saves both operations. */
    fun onTipKey(key: KeypadKey, note: String) {
        val main = tipMain ?: return
        if (key == KeypadKey.Done) return onTipSave(note)
        val digits = Currencies.info(Tips.base(main).currency).fractionDigits
        onTipChange { it.copy(percent = null, own = AmountKeys.apply(it.own, key, digits)) }
    }

    /** A line of the category choice: a category id, [TipPrefs.SAME_AS_PURCHASE] or [NEW_TIP_CATEGORY] (created as [newName]). */
    fun onTipCategory(choice: String, newName: String) {
        viewModelScope.launch {
            val id = if (choice == NEW_TIP_CATEGORY) entryTip.categoryNamed(newName, allCategories) ?: return@launch else choice
            if (choice == NEW_TIP_CATEGORY) {
                allCategories = categories.observeCategories(includeArchived = true).first().filter { !it.archived }
                _state.update { it.withCategoryOptions(allCategories, recentCategoryIds) }
            }
            onTipChange { it.copy(categoryId = id, asking = false) }
        }
    }

    /** «Сохранить две операции»: the purchase and the tip, [note] — «Чаевые» from resources. */
    fun onTipSave(note: String) {
        val main = tipMain ?: return
        val ui = _state.value.tip?.takeIf { it.canSave } ?: return
        if (_state.value.isDone) return
        val operation = (entryTip.transaction(ui, tipContext(main), note) as? Tips.Result.Ok)?.transaction
            ?: return onTipChange { it.copy(failed = true) }
        launchSave(main, operation to ui)
    }

    // endregion

    // region Save and delete

    fun save() {
        val current = _state.value
        if (current.isLoading || current.isDone) return
        val transaction = buildMain(current) ?: return
        launchSave(transaction)
    }

    /** The valid form as an operation; `null` after the field errors are shown. */
    private fun buildMain(current: TransactionEditUiState): Transaction? {
        val form = when (val result = validateForm(current, accountsById)) {
            is FormResult.Invalid -> {
                _state.update { it.withErrors(result.errors) }
                return null
            }
            is FormResult.Valid -> result.form
        }
        val date = dateWithTime(existing?.date ?: clock.now(), current.date, timeZone)
        // A payment gets the id of its template and day, as «Проводить само» does: both phones posting it before
        // they sync write one record, and «В прошлый раз» finds it (#404).
        val paymentId = templateId?.let { id -> paymentDate?.let { autoPaymentId(id, it) } }
        val built = buildTransaction(existing, form, date, paymentId?.let { fixed -> IdGenerator { fixed } } ?: ids, sourceLabels)
        // A new expense made from a scanned receipt remembers which one, to warn when it is scanned again (#526).
        // A receipt the model read has no number (#593): nothing to remember.
        val scanKey = current.scan?.key?.takeIf { it.isNotBlank() }
        return if (existing == null && scanKey != null && (built.type == TransactionType.EXPENSE || built.type == TransactionType.TRANSFER)) built.copy(receiptKey = scanKey) else built
    }

    /** Saves [transaction] and, with [tip] (#322), the tip operation after it; remembers the tip's account and category. */
    private fun launchSave(transaction: Transaction, tip: Pair<Transaction, TipUi>? = null) {
        viewModelScope.launch {
            when (val result = saveTransaction(transaction)) {
                SaveTransactionUseCase.Result.Saved -> {
                    tip?.let { (operation, ui) ->
                        saveTransaction(operation)
                        entryTip.remember(ui)
                    }
                    // A new operation (not an edit) gets «Сохранено · Отменить» and the shake window (#325).
                    if (existing == null) savedEntries.saved(listOfNotNull(transaction, tip?.first), moneyFormat.format(transaction.amount))
                    templateId?.let { id ->
                        entryTemplates.used(id)
                        paymentDate?.let { entryTemplates.paid(id, it) }
                    }
                    draftId?.let { drafts.deleteDrafts(listOf(it)) }
                    entryDrafts.clear()
                    _state.update { it.copy(isDone = true) }
                }
                is SaveTransactionUseCase.Result.Invalid -> _state.update { it.copy(tip = null).withError(result.reason) }
            }
        }
    }

    fun delete() {
        val transaction = existing ?: return
        viewModelScope.launch {
            deleteWithUndo(transaction.id)
            _state.update { it.copy(isDone = true) }
        }
    }

    // endregion

    private companion object {
        const val RECENT_ACCOUNTS = 4
        const val RECENT_OPERATIONS = 500

        /** #594: the model is asked for a seller or note of at least this many letters, a moment after the last keystroke. */
        const val AI_MIN_TEXT = 3
        const val AI_DELAY_MS = 700L
    }
}
