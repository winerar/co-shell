package app.coshell.core.designsystem.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import app.coshell.core.designsystem.motion.errorShake
import app.coshell.core.designsystem.motion.smoothResize
import app.coshell.core.designsystem.theme.AmountInputStyle
import app.coshell.core.designsystem.theme.accentInk

// Text inputs of the app (design-system.md §«Компоненты»): outlined, 12 dp corners (shapes.extraSmall),
// full width of their slot. Texts come as parameters — no app strings here.

/**
 * Text input. The line under the field shows [error] (field turns red) or else [hint].
 * [isError] — red without a message of its own (e.g. a length counter as [hint]).
 */
@Composable
fun CoShellTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    hint: String? = null,
    isError: Boolean = error != null,
    placeholder: String? = null,
    prefix: String? = null,
    suffix: String? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardOptions: KeyboardOptions = TextKeyboard,
    textStyle: TextStyle = LocalTextStyle.current,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    val supporting = supportingLine(error, hint)
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        prefix = prefix?.let { { Text(it) } },
        suffix = suffix?.let { { Text(it) } },
        trailingIcon = trailingIcon,
        isError = isError,
        supportingText = supporting?.let { { Text(it) } },
        enabled = enabled,
        singleLine = singleLine,
        minLines = minLines,
        keyboardOptions = keyboardOptions,
        textStyle = textStyle,
        colors = coShellFieldColors(),
        // The error/hint line comes and goes: the field grows to it smoothly (issue #64); a new error shakes it.
        modifier = modifier.fillMaxWidth().errorShake(if (isError) error ?: true else null).smoothResize(),
    )
}

/**
 * Money input: decimal keyboard, the amount font ([AmountInputStyle]), "0" placeholder and the
 * currency after the number in grey ([currency] — code or symbol).
 * [onToggleSign] — shows a "±" button (decimal keyboards often lack a minus; issue #128),
 * [toggleSignLabel] — its spoken description.
 */
@Composable
fun CoShellAmountField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    currency: String? = null,
    error: String? = null,
    hint: String? = null,
    imeAction: ImeAction = ImeAction.Done,
    onToggleSign: (() -> Unit)? = null,
    toggleSignLabel: String? = null,
) = CoShellTextField(
    value = value,
    onValueChange = onValueChange,
    label = label,
    modifier = modifier,
    error = error,
    hint = hint,
    placeholder = "0",
    suffix = currency,
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = imeAction),
    textStyle = AmountInputStyle,
    trailingIcon = onToggleSign?.let { toggle ->
        {
            TextButton(onClick = toggle, modifier = Modifier.semantics { toggleSignLabel?.let { contentDescription = it } }) {
                Text("±", style = MaterialTheme.typography.titleLarge)
            }
        }
    },
)

/** Focused field: ink border, label and cursor (neon on white is unreadable), Neon Mint selection. */
@Composable
private fun coShellFieldColors() = accentInk().let { ink ->
    OutlinedTextFieldDefaults.colors(
        focusedBorderColor = ink,
        focusedLabelColor = ink,
        cursorColor = ink,
        selectionColors = TextSelectionColors(handleColor = ink, backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
    )
}

/** Plain text: sentence capitals, "Next" moves to the following field. */
val TextKeyboard = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next)

/** Currency codes and other latin codes: capitals, ASCII keyboard. */
val CodeKeyboard = KeyboardOptions(
    capitalization = KeyboardCapitalization.Characters,
    keyboardType = KeyboardType.Ascii,
    imeAction = ImeAction.Next,
)

/** The line under a field: an error wins over the hint; nothing → no line. */
internal fun supportingLine(error: String?, hint: String?): String? = error ?: hint
