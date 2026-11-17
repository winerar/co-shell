package app.coshell.family.sync

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import app.coshell.MainActivity
import app.coshell.R
import app.coshell.core.data.family.FamilyService
import app.coshell.core.data.sync.SyncResult
import app.coshell.core.domain.AccountRepository
import app.coshell.core.domain.CategoryRepository
import app.coshell.core.domain.partner.PartnerChange
import app.coshell.core.domain.partner.PartnerChanges
import app.coshell.core.domain.partner.spendsToNotify
import app.coshell.core.domain.rates.CurrencyConverter
import app.coshell.core.domain.rates.ExchangeRateRepository
import app.coshell.core.domain.rates.RateBook
import app.coshell.core.domain.settings.ReportCurrencySettings
import app.coshell.core.domain.settings.ScreenOptionsStore
import app.coshell.core.model.MoneyFormat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Clock
import kotlinx.coroutines.flow.first
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * «Трата партнёра» (#326, board `Fb326A`): after a sync that brought changes, tells about the other member's expenses from
 * the chosen size — «Аня: −52 300 ARS» over «Продукты · Mercado Pago». No server: it happens when sync runs (the app is open,
 * or the hourly background run). A change older than the moment the switch was turned on is never told.
 */
@Singleton
class PartnerSpendNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
    private val family: FamilyService,
    private val changes: PartnerChanges,
    private val options: ScreenOptionsStore,
    private val rates: ExchangeRateRepository,
    private val reportCurrencies: ReportCurrencySettings,
    private val accounts: AccountRepository,
    private val categories: CategoryRepository,
    private val moneyFormat: MoneyFormat,
    private val clock: Clock,
    private val zone: TimeZone,
) {
    private val prefs = context.getSharedPreferences("partner_spend", Context.MODE_PRIVATE)

    /** Call after every [FamilyService.sync]; quiet unless it pulled something and the switch is on. */
    suspend fun afterSync(result: SyncResult?) {
        val o = options.options.first()
        val now = clock.now().toEpochMilliseconds()
        if (!o.partnerSpend) {
            prefs.edit().putLong(SEEN, now).apply() // turned on later — starts from that moment, no flood
            return
        }
        if (result == null || result.pulled == 0) return
        val me = family.myUid() ?: return
        val since = prefs.getLong(SEEN, 0L).takeIf { it > 0L } ?: run {
            prefs.edit().putLong(SEEN, now).apply()
            return
        }
        val fresh = changes.since(since, me)
        if (fresh.isEmpty()) return
        prefs.edit().putLong(SEEN, fresh.maxOf { it.updatedAtMs }).apply()

        val main = reportCurrencies.currencies.first().firstOrNull() ?: ReportCurrencySettings.DEFAULT.first()
        val converter = CurrencyConverter(RateBook.of(rates.observeRates().first()))
        val worth = spendsToNotify(fresh, o.partnerSpendMin, main, converter) { it.date.toLocalDateTime(zone).date }
        if (worth.isEmpty() || !canNotify()) return
        val names = family.otherMemberNames().first()
        worth.takeLast(MAX_NOTICES).forEach { notice(it, names[it.authorUid].orEmpty()) }
    }

    private suspend fun notice(change: PartnerChange, name: String) {
        val t = change.transaction
        val amount = "-" + moneyFormat.format(t.amount)
        val title = if (name.isBlank()) amount else context.getString(R.string.partner_spend_title, name, amount)
        val text = listOfNotNull(t.categoryId?.let { categories.getCategory(it)?.name }, accounts.getAccount(t.accountId)?.name, t.note.ifBlank { null }?.let { "«$it»" })
            .joinToString(" · ")
        show(t.id.hashCode(), title, text)
    }

    @SuppressLint("MissingPermission") // checked by canNotify()
    private fun show(id: Int, title: String, text: String) {
        ensureChannel()
        val open = PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val notification = androidx.core.app.NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_launcher_monochrome)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(open)
            .setAutoCancel(true)
            .setCategory(androidx.core.app.NotificationCompat.CATEGORY_SOCIAL)
            .build()
        NotificationManagerCompat.from(context).notify(id, notification)
    }

    private fun canNotify(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(CHANNEL, context.getString(R.string.partner_spend_channel), NotificationManager.IMPORTANCE_DEFAULT)
            .apply { description = context.getString(R.string.partner_spend_channel_hint) }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private companion object {
        const val CHANNEL = "partner_spend"
        const val SEEN = "seen_ms"
        const val MAX_NOTICES = 5
    }
}
