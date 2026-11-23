package app.coshell.export

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.FileProvider
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.R
import app.coshell.core.designsystem.category.CategoryPalette
import app.coshell.core.designsystem.category.categoryColor
import app.coshell.core.designsystem.components.underBars
import app.coshell.core.designsystem.components.CoShellButton
import app.coshell.core.designsystem.components.CoShellFilterChip
import app.coshell.core.designsystem.components.CoShellHint
import app.coshell.core.designsystem.components.CoShellMenuRow
import app.coshell.core.designsystem.components.CoShellRowGroup
import app.coshell.core.designsystem.components.CoShellSwitchRow
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.export.CsvExport
import app.coshell.core.model.TransactionType
import app.coshell.ui.bar.aboveBottomBars
import app.coshell.ui.format.formatMonthYear
import app.coshell.ui.format.formatDayMonth
import app.coshell.ui.form.ChoiceOption
import app.coshell.ui.form.ChoiceSheet
import app.coshell.ui.form.DateRangeSheet
import app.coshell.ui.list.SectionTitle
import app.coshell.ui.top.ScreenTopBar
import java.io.File
import kotlin.time.Clock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.todayIn
import app.coshell.core.designsystem.R as DesignR

/** What the screen asks for. */
data class ExportActions(
    val onBack: () -> Unit = {},
    val onKind: (ExportKind) -> Unit = {},
    val onMonth: (LocalDate) -> Unit = {},
    val onYear: (Int) -> Unit = {},
    val onCustom: (LocalDate, LocalDate) -> Unit = { _, _ -> },
    val onAllAccounts: (Boolean) -> Unit = {},
    val onAccount: (String, Boolean) -> Unit = { _, _ -> },
    val onExport: () -> Unit = {},
)

/** «Экспорт в CSV» (board `Fb204Export`): the file goes out through the system «Поделиться». */
@Composable
fun ExportRoute(onBack: () -> Unit, viewModel: ExportViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val labels = csvLabels()
    val zone = remember { TimeZone.currentSystemDefault() }
    val every = state.accounts.map { it.id }
    ExportScreen(
        state = state,
        actions = ExportActions(
            onBack = onBack,
            onKind = viewModel::onKind,
            onMonth = viewModel::onMonth,
            onYear = viewModel::onYear,
            onCustom = viewModel::onCustom,
            onAllAccounts = viewModel::onAllAccounts,
            onAccount = { id, picked -> viewModel.onAccount(id, picked, every) },
            onExport = {
                viewModel.build(labels, dateText = { moment -> moment.toLocalDateTime(zone).let { "${it.date} ${it.hour.pad()}:${it.minute.pad()}" } }) { csv ->
                    scope.launch { share(context, csv, Clock.System.todayIn(zone)) }
                }
            },
        ),
    )
}

private fun Int.pad() = toString().padStart(2, '0')

/** Writes [csv] to the cache and offers it to the system share sheet (e-mail, messenger, a drive). */
private suspend fun share(context: Context, csv: String, today: LocalDate) {
    val file = withContext(Dispatchers.IO) {
        File(context.cacheDir, "exports").apply { mkdirs() }.let { dir ->
            dir.listFiles()?.forEach { it.delete() } // only the last file is kept
            File(dir, "co-shell-$today.csv").apply { writeText(csv, Charsets.UTF_8) }
        }
    }
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.updates", file)
    val send = Intent(Intent.ACTION_SEND).setType("text/csv").putExtra(Intent.EXTRA_STREAM, uri).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    context.startActivity(Intent.createChooser(send, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}

@Composable
private fun csvLabels(): CsvExport.Labels {
    val columns = mapOf(
        CsvExport.Column.DATE to R.string.export_col_date, CsvExport.Column.TYPE to R.string.export_col_type,
        CsvExport.Column.ACCOUNT to R.string.export_col_account, CsvExport.Column.AMOUNT to R.string.export_col_amount,
        CsvExport.Column.CURRENCY to R.string.export_col_currency, CsvExport.Column.CATEGORY to R.string.export_col_category,
        CsvExport.Column.PAYEE to R.string.export_col_payee, CsvExport.Column.NOTE to R.string.export_col_note,
        CsvExport.Column.TAGS to R.string.export_col_tags, CsvExport.Column.RECEIPT_AMOUNT to R.string.export_col_receipt_amount,
        CsvExport.Column.RECEIPT_CURRENCY to R.string.export_col_receipt_currency, CsvExport.Column.TO_ACCOUNT to R.string.export_col_to_account,
        CsvExport.Column.TO_AMOUNT to R.string.export_col_to_amount, CsvExport.Column.TO_CURRENCY to R.string.export_col_to_currency,
        CsvExport.Column.AUTHOR to R.string.export_col_author,
    ).mapValues { stringResource(it.value) }
    val types = mapOf(
        TransactionType.EXPENSE to R.string.export_type_expense, TransactionType.INCOME to R.string.export_type_income,
        TransactionType.TRANSFER to R.string.export_type_transfer,
    ).mapValues { stringResource(it.value) }
    return CsvExport.Labels(columns, types)
}

@Composable
fun ExportScreen(state: ExportUiState, actions: ExportActions) {
    var picking by rememberSaveable { mutableStateOf(false) }
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    Scaffold(
        topBar = { ScreenTopBar(stringResource(R.string.export_title), onBack = actions.onBack) },
        bottomBar = {
            Box(Modifier.fillMaxWidth().aboveBottomBars().padding(Spacing.l)) {
                CoShellButton(onClick = actions.onExport, enabled = state.count > 0, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.export_action))
                }
            }
        },
    ) { padding ->
        // Its own button bar below: the list ends above it and goes under the top capsule only.
        LazyColumn(Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding()), contentPadding = PaddingValues(top = padding.calculateTopPadding())) {
            item {
                Row(Modifier.padding(horizontal = Spacing.l, vertical = Spacing.s), horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                    ExportKind.entries.forEach { kind ->
                        CoShellFilterChip(selected = state.kind == kind, onClick = { actions.onKind(kind); if (kind == ExportKind.CUSTOM) picking = true }, label = { Text(stringResource(kind.labelRes)) })
                    }
                }
            }
            item {
                CoShellRowGroup(Modifier.padding(horizontal = Spacing.l).padding(top = Spacing.s, bottom = Spacing.l)) {
                    row {
                        CoShellMenuRow(
                            DesignR.drawable.ic_calendar_today, categoryColor(CategoryPalette.colors[8]),
                            periodTitle(state), { picking = true }, subtitle = stringResource(R.string.export_period),
                        )
                    }
                }
            }
            item { SectionTitle(stringResource(R.string.export_accounts)) }
            item {
                CoShellRowGroup(Modifier.padding(horizontal = Spacing.l).padding(bottom = Spacing.l)) {
                    row {
                        CoShellSwitchRow(
                            title = stringResource(R.string.export_all_accounts),
                            subtitle = stringResource(R.string.export_all_accounts_hint, state.accounts.size),
                            checked = state.allAccounts, onCheckedChange = actions.onAllAccounts,
                        )
                    }
                    if (!state.allAccounts) state.accounts.forEach { account ->
                        row {
                            CoShellSwitchRow(
                                title = account.name, subtitle = account.currency,
                                checked = account.picked, onCheckedChange = { actions.onAccount(account.id, it) },
                            )
                        }
                    }
                }
            }
            item {
                Text(
                    stringResource(R.string.export_count, state.count),
                    Modifier.fillMaxWidth().padding(horizontal = Spacing.l),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
            item { CoShellHint(stringResource(R.string.export_hint), Modifier.padding(horizontal = Spacing.l, vertical = Spacing.m)) }
        }
    }
    if (picking) PeriodSheet(state, today, actions) { picking = false }
}

/** The month or the year to pick, or the days of an own period. */
@Composable
private fun PeriodSheet(state: ExportUiState, today: LocalDate, actions: ExportActions, onDismiss: () -> Unit) {
    when (state.kind) {
        ExportKind.CUSTOM -> DateRangeSheet(state.first, state.last, onDismiss) { a, b -> actions.onCustom(a, b); onDismiss() }
        ExportKind.MONTH -> {
            val months = remember(today) { (0 until MONTHS).map { YearMonth(today.year, today.month).minus(it, DateTimeUnit.MONTH) } }
            ChoiceSheet(
                title = stringResource(R.string.export_period),
                options = months.map { ChoiceOption(it.firstDay, formatMonthYear(it)) },
                selected = LocalDate(state.first.year, state.first.month, 1),
                onPick = { actions.onMonth(it); onDismiss() },
                onDismiss = onDismiss,
            )
        }
        ExportKind.YEAR -> ChoiceSheet(
            title = stringResource(R.string.export_period),
            options = (0 until YEARS).map { today.year - it }.map { ChoiceOption(it, it.toString()) },
            selected = state.first.year,
            onPick = { actions.onYear(it); onDismiss() },
            onDismiss = onDismiss,
        )
    }
}

@Composable
private fun periodTitle(state: ExportUiState): String = when (state.kind) {
    ExportKind.MONTH -> formatMonthYear(YearMonth(state.first.year, state.first.month))
    ExportKind.YEAR -> state.first.year.toString()
    ExportKind.CUSTOM -> "${formatDayMonth(state.first)} – ${formatDayMonth(state.last)}"
}

private val ExportKind.labelRes: Int
    get() = when (this) {
        ExportKind.MONTH -> R.string.export_kind_month
        ExportKind.YEAR -> R.string.export_kind_year
        ExportKind.CUSTOM -> R.string.export_kind_custom
    }

private const val MONTHS = 24
private const val YEARS = 6
