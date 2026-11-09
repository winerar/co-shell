package app.coshell.core.domain.importer.wallet

/** Copy of `docs/samples/wallet-export-sample.csv` (common tests cannot read files). */
internal object WalletSamples {

    const val HEADER = "account;category;currency;amount;ref_currency_amount;type;payment_type;" +
        "payment_type_local;note;date;gps_latitude;gps_longitude;gps_accuracy_in_meters;" +
        "warranty_in_month;transfer;payee;labels;envelope_id;custom_category"

    val SAMPLE = """
        $HEADER
        Карта USD;Образование;USD;-10.00;-3800.00;Gastos;CASH;Efectivo;;2026-11-08 11:23:53;;;;0;false;;Курсы;6006;false
        Наличные ARS;Продукты;ARS;-12000.00;-2850.00;Gastos;CASH;Efectivo;;2026-11-08 09:22:10;;;;0;false;;Пекарня;1003;false
        Наличные ARS;Продукты;ARS;-8000.00;-1900.00;Gastos;CASH;Efectivo;Овощи;2026-11-07 15:10:45;;;;0;false;;;1000;false
        Карта RUB;Дети;RUB;-2000.00;-9500.00;Gastos;CASH;Efectivo;Коляска: аванс;2026-11-07 09:21:33;;;;0;false;;;2003;false
        Карта RUB;Проценты с вкладов, дивиденды;RUB;3000.00;14200.00;Ingresos;CASH;Efectivo;;2026-11-02 09:23:00;;;;0;false;;;10001;false
        Счёт AMD;Зарплата;AMD;500000.00;500000.00;Ingresos;CASH;Efectivo;;2026-11-01 10:49:53;;;;0;false;;;10000;false
        Счёт AMD;TRANSFER;AMD;-380000.00;-380000.00;Gastos;CASH;Efectivo;;2026-10-31 12:55:37;;;;0;true;;;20001;false
        Карта USD;TRANSFER;USD;1000.00;380000.00;Ingresos;CASH;Efectivo;;2026-10-31 12:55:37;;;;0;true;;;20001;false
        Карта USD;TRANSFER;USD;-500.00;-190000.00;Gastos;CASH;Efectivo;;2026-10-13 21:03:38;;;;0;true;;;20001;false
        Наличные ARS;TRANSFER;ARS;780000.00;190000.00;Ingresos;CASH;Efectivo;;2026-10-13 21:03:38;;;;0;true;;;20001;false
        Карта USD;TRANSFER;USD;-20.00;-7600.00;Gastos;CASH;Efectivo;;2026-10-18 10:05:42;;;;0;true;;;20001;false
        Наличные ARS;Рестораны;ARS;-25000.00;-5950.00;Gastos;CASH;Efectivo;;2026-10-04 22:01:54;;;;0;false;;Кофе;1001;false
    """.trimIndent() + "\n"

    /** A data row with every column of [HEADER]; override cells by column name. */
    fun row(vararg cells: Pair<String, String>): String {
        val values = mutableMapOf(
            "account" to "Карта USD", "category" to "Еда", "currency" to "USD", "amount" to "-1.00",
            "ref_currency_amount" to "-380.00", "type" to "Gastos", "payment_type" to "CASH",
            "payment_type_local" to "Efectivo", "date" to "2026-11-01 10:00:00",
            "warranty_in_month" to "0", "transfer" to "false", "custom_category" to "false",
        )
        values.putAll(cells)
        return HEADER.split(';').joinToString(";") { values[it].orEmpty() }
    }
}
