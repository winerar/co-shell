package app.coshell.categories.edit

import androidx.annotation.StringRes
import app.coshell.R
import app.coshell.core.domain.SaveCategoryUseCase

/** Text shown under the form when [SaveCategoryUseCase] refuses to save. */
@get:StringRes
internal val SaveCategoryUseCase.Reason.messageRes: Int
    get() = when (this) {
        SaveCategoryUseCase.Reason.PARENT_NOT_FOUND -> R.string.category_error_parent_not_found
        SaveCategoryUseCase.Reason.PARENT_NOT_TOP_LEVEL -> R.string.category_error_parent_nested
        SaveCategoryUseCase.Reason.PARENT_TYPE_MISMATCH -> R.string.category_error_parent_type
        SaveCategoryUseCase.Reason.HAS_SUBCATEGORIES -> R.string.category_error_has_children
        SaveCategoryUseCase.Reason.TYPE_LOCKED -> R.string.category_type_locked
    }
