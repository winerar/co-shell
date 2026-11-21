package app.coshell.core.domain.receipts

import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.datetime.LocalDate

/** Samples: `docs/samples/receipts/ameriabank.txt` and `tbank.txt` (text layers of real PDFs, names and numbers replaced). */
class BankFormatsTest {
    private val ameria =
        "+374 10 56 11 11\ninfo@ameriabank.am\nул. Вазгена Саргсяна, 2, Ереван 0010, РА\nКарточный перевод\n" +
        "Банк\tАмериабанк\nИмя Плательщика\tIVAN PETROV\nДата утверждения\t26 Октябрь 2026\nСтатус\tПодтверждено\n" +
        "Получатель\tVISA INTERNATIONAL\nСчёт списания\t1570000000000001\nСчёт зачисления\t1570000000000002\n" +
        "Сумма\t2 733.85 AMD\nДетали\tՔ: Spotify P4856C4BE6 STOCKHOLM 75"

    private val tbank =
        "14.11.2026 20:04:28\nИтого\t12 337 ₽\nПеревод\tПо номеру телефона\nСтатус\tУспешно\nСумма\t12 337 ₽\nКомиссия\t0₽\n" +
        "Отправитель\tИван Петров\nТелефон получателя\t+7 (900) 000-00-00\nПолучатель\tМария К.\nБанк получателя\tСбербанк\n" +
        "Идентификатор операции\tB63181704291760D0G101600118\nСБП\t90701\nСчет списания\t408170000000****0000\n" +
        "Квитанция № 1-126-401-521-595\nСлужба поддержки fb@tbank.ru"

    @Test
    fun `ameriabank reads the amount, the day and the merchant`() {
        val r = AmeriabankFormat.read(ameria)!!
        assertEquals(Money(273_385, CurrencyCode("AMD")), r.total)
        assertEquals(LocalDate(2026, 10, 26), r.date)
        assertEquals("Spotify", r.merchant)
        assertEquals("ameriabank:2026-10-26:1570000000000001:273385:spotifyp4856c4be6stockholm75", r.key)
    }

    @Test
    fun `ameriabank spaces instead of tabs read the same`() {
        assertEquals(AmeriabankFormat.read(ameria), AmeriabankFormat.read(ameria.replace("\t", "     ")))
    }

    @Test
    fun `ameriabank without amount, date or its name is no receipt`() {
        assertNull(AmeriabankFormat.read(ameria.replace("Сумма\t2 733.85 AMD\n", "")))
        assertNull(AmeriabankFormat.read(ameria.replace("26 Октябрь", "26 Нечто")))
        assertNull(AmeriabankFormat.read(ameria.replace("ameriabank", "otherbank").replace("Америабанк", "")))
    }

    @Test
    fun `tbank reads the amount, the day, the recipient and the operation`() {
        val r = TbankFormat.read(tbank)!!
        assertEquals(Money(1_233_700, CurrencyCode("RUB")), r.total)
        assertEquals(LocalDate(2026, 11, 14), r.date)
        assertEquals("Мария К.", r.merchant)
        assertEquals("tbank:B63181704291760D0G101600118", r.key)
    }

    @Test
    fun `tbank without the operation or the amount is no receipt`() {
        assertNull(TbankFormat.read(tbank.replace("B63181704291760D0G101600118", "")))
        assertNull(TbankFormat.read(tbank.replace("Сумма\t12 337 ₽\n", "")))
        assertNull(TbankFormat.read("Сумма 100 ₽"))
    }

    @Test
    fun `the formats do not read each other and ReceiptFormats finds both`() {
        assertNull(AmeriabankFormat.read(tbank))
        assertNull(TbankFormat.read(ameria))
        assertEquals("ameriabank", ReceiptFormats.read(ameria)!!.format)
        assertEquals("tbank", ReceiptFormats.read(tbank)!!.format)
    }
}
