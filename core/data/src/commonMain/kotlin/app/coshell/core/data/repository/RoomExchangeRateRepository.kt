package app.coshell.core.data.repository

import app.coshell.core.data.db.DownloadedRateEntity
import app.coshell.core.data.db.ManualRateEntity
import app.coshell.core.data.db.RateDao
import app.coshell.core.data.db.manualRateId
import app.coshell.core.domain.rates.DownloadInfo
import app.coshell.core.domain.rates.ExchangeRateRepository
import app.coshell.core.domain.rates.RatesSnapshot
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.ExchangeRate
import app.coshell.core.model.Rate
import app.coshell.core.model.RateOrigin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * With [scope] (the app's), every screen shares one read and one parse of all rates (#260): a year of
 * history is thousands of rows, and «Обзор», «Счета», «Бюджеты» and the entry form all need them.
 */
class RoomExchangeRateRepository(
    private val rates: RateDao,
    private val stamper: WriteStamper,
    scope: CoroutineScope? = null,
) : ExchangeRateRepository {

    private val allRates: Flow<List<ExchangeRate>> =
        combine(rates.observeManual(), rates.observeDownloaded()) { manual, downloaded ->
            manual.map { it.toModel() } + downloaded.map { it.toModel() }
        }.flowOn(Dispatchers.Default).let { flow ->
            // Kept a little after the last screen leaves, so switching tabs does not re-read the table.
            scope?.let { flow.shareIn(it, SharingStarted.WhileSubscribed(SHARE_TIMEOUT_MS), replay = 1) } ?: flow
        }

    override fun observeRates(): Flow<List<ExchangeRate>> = allRates

    override fun observeManualRates(): Flow<List<ExchangeRate>> =
        rates.observeManual().map { list -> list.map { it.toModel() } }

    // The id is the same for both directions, so the upsert replaces the opposite one too.
    override suspend fun saveManualRate(rate: ExchangeRate) {
        require(rate.origin == RateOrigin.MANUAL) { "Only manual rates are saved here, got ${rate.origin}" }
        rates.upsertManual(listOf(rate.toManualEntity(stamper.stamp())))
    }

    override suspend fun deleteManualRate(date: LocalDate, a: CurrencyCode, b: CurrencyCode) {
        val stamp = stamper.stamp()
        rates.markManualDeleted(manualRateId(date.toString(), a.value, b.value), stamp.now, stamp.by)
    }

    override suspend fun saveDownloaded(snapshot: RatesSnapshot) {
        val now = stamper.stamp().now
        rates.upsertDownloaded(snapshot.rates.map { it.toDownloadedEntity(snapshot.source, now) })
    }

    override fun observeLatestDownload(): Flow<DownloadInfo?> =
        rates.observeLatestDownloaded().map { row ->
            row?.let { DownloadInfo(LocalDate.parse(it.date), it.source, Instant.fromEpochMilliseconds(it.downloadedAt)) }
        }

    override suspend fun latestDownloadedDate(): LocalDate? = rates.latestDownloadedDate()?.let(LocalDate::parse)

    override suspend fun downloadedDates(): Set<LocalDate> = rates.downloadedDates().map(LocalDate::parse).toSet()

    override suspend fun operationDates(timeZone: TimeZone): Set<LocalDate> =
        rates.operationTimes().mapTo(HashSet()) { Instant.fromEpochMilliseconds(it).toLocalDateTime(timeZone).date }

    override suspend fun usedCurrencies(): Set<CurrencyCode> =
        rates.usedCurrencies().mapNotNull(CurrencyCode::parseOrNull).toSet()

    private companion object {
        const val SHARE_TIMEOUT_MS = 5_000L
    }
}

internal fun ManualRateEntity.toModel() = ExchangeRate(
    date = LocalDate.parse(date),
    from = CurrencyCode(fromCurrency),
    to = CurrencyCode(toCurrency),
    rate = Rate.parse(rate),
    origin = RateOrigin.MANUAL,
)

internal fun ExchangeRate.toManualEntity(stamp: Stamp) = ManualRateEntity(
    id = manualRateId(date.toString(), from.value, to.value),
    date = date.toString(),
    fromCurrency = from.value,
    toCurrency = to.value,
    rate = rate.toPlainString(),
    updatedAt = stamp.now,
    updatedBy = stamp.by,
)

private fun DownloadedRateEntity.toModel() = ExchangeRate(
    date = LocalDate.parse(date),
    from = CurrencyCode(fromCurrency),
    to = CurrencyCode(toCurrency),
    rate = Rate.parse(rate),
    origin = RateOrigin.DOWNLOADED,
)

private fun ExchangeRate.toDownloadedEntity(source: String, now: Long) = DownloadedRateEntity(
    date = date.toString(),
    fromCurrency = from.value,
    toCurrency = to.value,
    rate = rate.toPlainString(),
    source = source,
    downloadedAt = now,
)
