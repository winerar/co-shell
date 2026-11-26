package app.coshell.core.domain.categoryguess

/**
 * Merchant category codes (ISO 18245, tag 52 of a payment QR, #emv) → the icon key of the starter category, the same keys as
 * [BrandDictionary]: the category of a shop nobody has paid before. Only codes whose meaning is plain; the rest — nothing.
 */
object MccCategories {
    private val byIcon: Map<String, String> = mapOf(
        "food" to "5300 5331 5411 5422 5441 5451 5462 5499",
        "restaurant" to "5811 5812 5813 5814",
        "taxi" to "4121",
        "transport" to "4111 4112 4131 4784 5172 5532 5533 5541 5542 5983 7523 7531 7538 7542",
        "health" to "5122 5912 5975 5976 8011 8021 8031 8041 8042 8043 8049 8050 8062 8071 8099",
        "clothes" to "5137 5139 5611 5621 5631 5651 5655 5661 5691 5699 7251",
        "kids" to "5641 5945 8351",
        "home" to "5039 5200 5211 5231 5251 5261 5712 5713 5714 5718 5719 5722 5732",
        "utilities" to "4812 4814 4816 4900",
        "subscriptions" to "4899 5815 5816 5817 5818 5968",
        "education" to "5942 5943 8211 8220 8241 8244 8249 8299",
        "fun" to "7832 7841 7922 7929 7932 7933 7941 7991 7992 7996 7997 7999",
        "gift" to "5947 5992",
    )

    private val index: Map<String, String> = byIcon.flatMap { (icon, codes) -> codes.split(' ').map { it to icon } }.toMap()

    /** The icon key for [mcc], `null` for a code without a plain category (banks, transfers, «misc»). */
    fun iconOf(mcc: String?): String? = mcc?.let(index::get)
}
