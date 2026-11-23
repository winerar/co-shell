package app.coshell.core.designsystem.tokens

import androidx.compose.ui.unit.dp

/** Fixed sizes of repeating elements (medium list density, design-questions §4). Screens use these, not `N.dp`. */
object Sizes {
    /** List row: at least the touch-friendly 56 dp; with a [badge] and Spacing.m above and below it is 64. */
    val rowMinHeight = 56.dp
    /** Dense menu row («Ещё», #155): a [badgeSmall] with Spacing.s above and below fits in 52. */
    val rowDenseMinHeight = 52.dp

    /** Rows of the data lists when «Плотность списков» is compact. */
    val rowCompactMinHeight = 44.dp
    /** Category badge, colour swatch — the 40 dp circle of a 64 dp list row. */
    val badge = 40.dp
    /** Badge in an editor's header, beside the name: the category as it will look. */
    val badgeLarge = 56.dp
    /** Subcategory badge inside an open folder. */
    val badgeSmall = 32.dp
    /** Colour dot of a one-line strip and the «Иконка и цвет» sheet (#290): nine fit across a form card. */
    val swatch = 28.dp
    /** Ring around the selected swatch or icon. */
    val selectionRing = 2.dp
    /** Space under the last list item so a floating button doesn't cover it: FAB 56 + margins 16 + 16. */
    val fabClearance = 88.dp
    /** The round add button of a list screen ([app.coshell.core.designsystem.components.CoShellAddFab], #408) and its «+». */
    val fab = 56.dp
    val fabIcon = 26.dp
    /** Big status ring in the middle of a single-purpose screen (update, later import/sync). */
    val heroRing = 168.dp

    /** [CoShellLoading]: in a row / alone on a screen (board `CompNew`). */
    val loadingRow = 24.dp
    val loadingScreen = 44.dp
    /** Icon inside [heroRing]. */
    val heroIcon = 56.dp
    /** Stroke of [heroRing]. */
    val heroStroke = 6.dp
    /** Bento tile on «Обзор» (2 in a row): tall enough for a label, a large amount and a line under it. */
    val tileMinHeight = 128.dp
    /** The icon tile of an empty screen and the glyph in it ([CoShellEmptyState]). */
    val emptyTile = 72.dp
    val emptyIcon = 32.dp
    /** Small icon in a tile's header. */
    val tileIcon = 20.dp
    /** The round arrow between the two version tiles of the update screen (poll 2026-11-18). */
    val arrowChip = 32.dp
    /** Camera distance of a turning card ([app.coshell.core.designsystem.components.CoShellFlipCard], #519), times density: real depth without a fish-eye. */
    val flipDepth = 12.dp
    /** Height of the mini bars in a tile (expenses per day). */
    val miniBarsHeight = 32.dp
    /** Lowest visible bar: a day with little spending still shows; a day with none is a dot of this height. */
    val miniBarMin = 2.dp
    /** Key of the amount keypad in the entry sheet: big enough to hit with a thumb without looking. */
    val keypadKey = 56.dp
    /** A dot of the PIN (CoShellPinDots, #145) and the width of its empty ring. */
    val pinDot = 16.dp
    val pinDotStroke = 2.dp
    /** Account tile in the account picker grid (badge, name, balance). */
    val accountTile = 96.dp
    /** Thin outline of a card or a floating pill. */
    val hairline = 1.dp
    /** Floating notice pill over the bottom bar (CoShellNoticePill): a touch-friendly 48 dp. */
    val pillHeight = 48.dp
    /** Search pill above a list (CoShellSearchField): 48 dp, like the boards. */
    val searchHeight = 48.dp
    /** Magnifier and «✕» inside the search pill. */
    val searchIcon = 20.dp
    /** Floating bottom bar capsule (poll 2026-11-16 #441 «Б»: 56 high, [panelInset] from the screen edges). */
    val bottomBarHeight = 56.dp
    /** Active tab's pill inside the bar. */
    val bottomBarPill = 40.dp
    /** Top bar capsule (same style as the bottom bar): 56 high, room for 48 dp buttons. */
    val topBarHeight = 56.dp
    /** Side margin of both floating capsules: narrow, the screen's background shows around them. */
    val panelInset = 32.dp
    /** The «+» circle in the middle of the bar — a button, so [button] high like all of them. */
    val bottomBarAdd = 48.dp
    /** The round buttons of the fan over «+» (CoShellActionFan, #623, board `EwHoldA`) and their distance from the «+». */
    val fanButton = 56.dp
    val fanRadius = 120.dp
    /** Room of a fan item: the button and its caption under it. */
    val fanItemWidth = 88.dp
    /** Buttons inside an action bar (CoShellActionBar): 48 dp in the 64 dp capsule. */
    val actionBarButton = 48.dp
    /** A currency code tile in a rates row («USD» on «Курсах валют»). */
    val currencyChipWidth = 44.dp
    val currencyChipHeight = 36.dp
    /** The card-coloured ring that separates overlapping avatars (members on «Семье»). */
    val avatarRing = 3.dp
    /**
     * Every button — filled, outlined, text, the icon button (a circle) and the bar's «+» (owner's poll «Единый стиль»
     * 2026-11-15: «все кнопки одной высоты, стандартный размер 48, в том числе круги»).
     */
    val button = 48.dp

    /** Viewfinder of the receipt QR scanner (#526): square side, corner stroke, corner radius, length of the straight arm. */
    val scanFrame = 280.dp
    val scanFrameStroke = 5.dp
    val scanFrameCorner = 20.dp
    val scanFrameArm = 36.dp
    /** Icon next to a button's label. */
    val buttonIcon = 20.dp
    val chipIcon = 18.dp
    /** Account badge in the entry-sheet ribbon (#505); its 48 dp cell is [button]. */
    val ribbonBadge = 44.dp
    /** Mini account badge before the account name in an operation row (#505). */
    val miniBadge = 16.dp
    /** Account badge inside a choice chip. */
    val chipBadge = 24.dp
    /** Horizontal drag after which letting go flips to the neighbouring tab (issue #112). */
    val tabSwipe = 80.dp
    /** Segment of CoShellSegmented: 36 dp, with the track's [segmentInset] the control is a 44 dp cell (board Style1). */
    val segmentHeight = 36.dp
    /** Gap between the segments' track and the sliding pill. */
    val segmentInset = 4.dp
    /** Label — a capsule that is not pressed: tag, change, status, «новое» (board Style2). */
    val labelHeight = 22.dp
    /** Chip in a row of choices (ChoicePill, FilterChip, TagChip; board Style1). */
    val chipHeight = 36.dp
    /** Cell of a choice grid, a day of the month (LabelGrid, DayGrid; board Style1). */
    val cellHeight = 44.dp
    /** Amount typed on the keypad ([app.coshell.core.designsystem.components.CoShellAmountSlot]): with a label / the main one. */
    val amountSlot = 64.dp
    val amountSlotMain = 72.dp
    /** Dot of a vertical timeline (feedback card, #157); the line between dots is [selectionRing] wide. */
    val timelineDot = 12.dp
    /** Every bar — budget bullet, bar row, progress (board Style7: one bar 8 dp with a mark). */
    val bullet = 8.dp
    /** The dot under a marked day of [cellHeight]. */
    val dayMark = 5.dp

    /** Height of the date range picker in its sheet (#345): about three months on screen. */
    val rangePicker = 440.dp

    /** A control row (arrows + month + pill) is split in two below this width × the font scale (#379). */
    val narrowRow = 340.dp

    /** A day of [app.coshell.core.designsystem.components.CoShellHeatGrid] (#553): 48 dp, a thumb-size target. */
    val heatCell = 48.dp
    val heatSwatch = 18.dp
    val heatTodayRing = 1.5.dp
    val heatSelectedRing = 2.dp
}
