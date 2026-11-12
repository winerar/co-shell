package app.coshell.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import app.coshell.core.designsystem.motion.errorShake
import app.coshell.core.designsystem.motion.smoothResize
import app.coshell.core.designsystem.theme.AmountInputStyle
import app.coshell.core.designsystem.theme.accentInk
import app.coshell.core.designsystem.tokens.Sizes

// Text inputs of the app (design-system.md §«Компоненты», К6 = 8А, issue #200): filled `surfaceContainer`,
// 1 dp border, 12 dp corners (shapes.extraSmall), the label inside the field; full width of their slot.
// Texts come as parameters — no app strings here.

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
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val colors = coShellFieldColors()
    val shape = MaterialTheme.shapes.extraSmall
    val ink = accentInk()
    val textColor = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = DISABLED_ALPHA)
    val border = fieldBorderColor(focused = focused, isError = isError, enabled = enabled)
    CompositionLocalProvider(LocalTextSelectionColors provides colors.textSelectionColors) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = singleLine,
            minLines = minLines,
            maxLines = if (singleLine) 1 else Int.MAX_VALUE,
            keyboardOptions = keyboardOptions,
            textStyle = textStyle.merge(TextStyle(color = textColor)),
            cursorBrush = SolidColor(if (isError) MaterialTheme.colorScheme.error else ink),
            interactionSource = interaction,
            // The error/hint line comes and goes: the field grows to it smoothly (issue #64); a new error shakes it.
            modifier = modifier
                .fillMaxWidth()
                .errorShake(if (isError) error ?: true else null)
                .smoothResize(),
            decorationBox = { inner ->
                TextFieldDefaults.DecorationBox(
                    value = value,
                    innerTextField = inner,
                    enabled = enabled,
                    singleLine = singleLine,
                    visualTransformation = VisualTransformation.None,
                    interactionSource = interaction,
                    isError = isError,
                    label = { Text(label) },
                    placeholder = placeholder?.let { { Text(it) } },
                    prefix = prefix?.let { { Text(it) } },
                    suffix = suffix?.let { { Text(it) } },
                    trailingIcon = trailingIcon,
                    supportingText = supporting?.let { { Text(it) } },
                    shape = shape,
                    colors = colors,
                    // The fill and the 1 dp border hug the box only, not the error/hint line under it.
                    container = {
                        Box(
                            Modifier
                                .background(MaterialTheme.colorScheme.surfaceContainer, shape)
                                .border(Sizes.hairline, border, shape),
                        )
                    },
                )
            },
        )
    }
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

/** Label and cursor in ink when focused (neon on white is unreadable), Neon Mint selection, no M3 indicator line. */
@Composable
private fun coShellFieldColors() = accentInk().let { ink ->
    TextFieldDefaults.colors(
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        disabledContainerColor = Color.Transparent,
        errorContainerColor = Color.Transparent,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent,
        errorIndicatorColor = Color.Transparent,
        focusedLabelColor = ink,
        cursorColor = ink,
        selectionColors = TextSelectionColors(handleColor = ink, backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
    )
}

/** The 1 dp border: error wins, then ink while typing, else the soft line of the theme. */
@Composable
private fun fieldBorderColor(focused: Boolean, isError: Boolean, enabled: Boolean): Color = when {
    !enabled -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = DISABLED_ALPHA)
    isError -> MaterialTheme.colorScheme.error
    focused -> accentInk()
    else -> MaterialTheme.colorScheme.outlineVariant
}

private const val DISABLED_ALPHA = 0.38f

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
