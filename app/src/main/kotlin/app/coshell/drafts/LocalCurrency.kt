package app.coshell.drafts

import android.content.Context
import android.telephony.TelephonyManager
import app.coshell.core.model.CurrencyCode
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Currency
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The currency of the country the phone is in (#633, owner idea: «помочь распознавать валюту по геолокации»): the country of the
 * mobile network, else of the SIM — no location permission, nothing leaves the phone. `null` without a SIM or for an unknown code.
 * A quick-note line without a currency then goes to an account in this currency («кофе 3500» in Argentina → pesos).
 */
@Singleton
class LocalCurrency @Inject constructor(@ApplicationContext private val context: Context) {
    fun current(): CurrencyCode? {
        val phone = context.getSystemService(TelephonyManager::class.java) ?: return null
        val country = phone.networkCountryIso?.takeIf { it.length == 2 } ?: phone.simCountryIso?.takeIf { it.length == 2 } ?: return null
        return ofCountry(country)
    }

    companion object {
        /** «ar» → ARS, «am» → AMD; `null` when the platform does not know the country. */
        fun ofCountry(iso: String): CurrencyCode? = runCatching {
            Currency.getInstance(Locale.Builder().setRegion(iso.uppercase(Locale.ROOT)).build())?.currencyCode
        }.getOrNull()?.let(CurrencyCode::parseOrNull)
    }
}
