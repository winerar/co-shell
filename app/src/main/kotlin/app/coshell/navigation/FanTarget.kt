package app.coshell.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import app.coshell.R
import app.coshell.core.designsystem.R as DesignR

/** What the fan over «+» opens (#623, board `EwHoldA`): a receipt QR, quick notes, a template. «Голос» comes with voice entry. */
internal enum class FanTarget(@StringRes val label: Int, @DrawableRes val icon: Int) {
    RECEIPT(R.string.fan_receipt, DesignR.drawable.ic_qr_code_scanner),
    NOTES(R.string.fan_notes, DesignR.drawable.ic_edit_note),
    TEMPLATE(R.string.fan_template, DesignR.drawable.ic_format_list_bulleted),
}
