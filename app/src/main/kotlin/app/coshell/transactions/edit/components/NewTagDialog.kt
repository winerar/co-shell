package app.coshell.transactions.edit.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellDialog
import app.coshell.core.designsystem.components.CoShellTextField
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.model.Tag
import app.coshell.ui.form.ColorSection

/**
 * «Новый тег — название и цвет» (#293): the name (from the search text) and a palette colour. No colour chosen —
 * the tag gets one from its id, the same on both phones. The tag is chosen for the operation at once.
 */
@Composable
internal fun NewTagDialog(initialName: String, onCreate: (name: String, color: Long?) -> Unit, onDismiss: () -> Unit) {
    var name by rememberSaveable { mutableStateOf(initialName.take(Tag.MAX_NAME)) }
    var color by rememberSaveable { mutableStateOf<Long?>(null) }
    CoShellDialog(
        title = stringResource(R.string.tags_new_title),
        confirmLabel = stringResource(R.string.tags_create),
        onConfirm = { onCreate(name, color) },
        onDismiss = onDismiss,
        dismissLabel = stringResource(R.string.action_cancel),
        confirmEnabled = name.isNotBlank(),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
            CoShellTextField(
                value = name,
                onValueChange = { name = it.take(Tag.MAX_NAME) },
                label = stringResource(R.string.tags_name),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None),
            )
            ColorSection(selected = color, onSelect = { color = it })
        }
    }
}
