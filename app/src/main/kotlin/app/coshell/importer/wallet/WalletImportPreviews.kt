package app.coshell.importer.wallet

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.domain.importer.wallet.WalletCsvParser
import app.coshell.core.model.MoneyFormat
import kotlinx.datetime.TimeZone

private val PREVIEW_CSV = """
    account;category;currency;amount;ref_currency_amount;note;date;transfer;payee;labels
    Карта USD;Еда;USD;-10.00;-3800.00;;2026-11-08 11:23:53;false;;Кофе
    Наличные ARS;Продукты;ARS;-12000.00;-2850.00;Овощи;2026-11-07 15:10:45;false;;
    Карта USD;TRANSFER;USD;-20.00;-7600.00;;2026-10-18 10:05:42;true;;
""".trimIndent()

private fun previewState(step: WalletImportStep): WalletImportUiState {
    val records = WalletCsvParser.parse(PREVIEW_CSV).records
    return WalletImportUiState(step = step, draft = WalletImportDraft.start(records, emptyList(), emptyList()))
}

@Preview(showBackground = true)
@Composable
private fun StartPreview() = CoShellTheme {
    WalletImportScreen(WalletImportUiState(), WalletImportActions(), MoneyFormat.RU, TimeZone.UTC)
}

@Preview(showBackground = true)
@Composable
private fun AccountsPreview() = CoShellTheme {
    WalletImportScreen(previewState(WalletImportStep.ACCOUNTS), WalletImportActions(), MoneyFormat.RU, TimeZone.UTC)
}

@Preview(showBackground = true)
@Composable
private fun TransfersPreview() = CoShellTheme {
    WalletImportScreen(previewState(WalletImportStep.TRANSFERS), WalletImportActions(), MoneyFormat.RU, TimeZone.UTC)
}
