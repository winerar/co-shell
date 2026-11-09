package app.coshell.core.designsystem.motion

// The opener's colour (issue #63): a sheet opened from the «+» starts painted in the «+»'s colour, which
// dissolves into the sheet's own look while it rises (`components/CoShellSheet`).

/** Opacity of the opener's colour over the growing element: solid at first, gone by [TINT_GONE_AT]. Pure. */
fun colorTintAlpha(progress: Float): Float {
    val t = ((progress - TINT_HOLD_UNTIL) / (TINT_GONE_AT - TINT_HOLD_UNTIL)).coerceIn(0f, 1f)
    return 1f - t * t * (3f - 2f * t) // smoothstep: no visible edge where the fade starts or ends
}

private const val TINT_HOLD_UNTIL = 0.15f
private const val TINT_GONE_AT = 0.75f
