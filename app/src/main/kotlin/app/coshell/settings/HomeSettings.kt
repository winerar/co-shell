package app.coshell.settings

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import app.coshell.ui.form.ChoiceSheet
import app.coshell.ui.form.ChoiceOption
import app.coshell.core.domain.settings.HomeTile
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.R
import app.coshell.core.designsystem.category.CategoryPalette
import app.coshell.core.designsystem.category.categoryColor
import app.coshell.core.designsystem.components.CoShellHint
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.core.designsystem.components.CoShellMenuRow
import app.coshell.core.designsystem.components.CoShellRowGroup
import app.coshell.core.designsystem.components.CoShellTextButton
import app.coshell.core.designsystem.components.CoShellSwitchRow
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.settings.HomeCard
import app.coshell.core.domain.settings.HomeLayout
import app.coshell.core.domain.settings.ScreenOptions
import app.coshell.ui.list.SectionTitle
import kotlin.math.roundToInt
import app.coshell.core.designsystem.R as DesignR

// «Настройки → Экраны → Обзор» (boards Set1Home, Set1HomeMore): which cards «Обзор» shows and in what order, how far «Скоро»
// looks and how many «Последние» show. The balance stays on top. Editing right on «Обзор» (hold a card) and the choice of
// the two tiles are still to come.

@Composable
fun HomeSettingsRoute(
    onDetails: () -> Unit,
    onBalance: () -> Unit,
    onBack: () -> Unit,
    viewModel: ScreenOptionsViewModel = hiltViewModel(),
    accounts: SettingsAccountsViewModel = hiltViewModel(),
) {
    val o by viewModel.options.collectAsStateWithLifecycle()
    val list by accounts.list.collectAsStateWithLifecycle()
    HomeSettingsScreen(o, { f -> viewModel.change(change = f) }, onDetails, onBalance, list.count { it.excludedFromTotal }, onBack)
}

@Composable
fun HomeSettingsScreen(
    options: ScreenOptions,
    change: ((ScreenOptions) -> ScreenOptions) -> Unit,
    onDetails: () -> Unit,
    onBalance: () -> Unit,
    excludedAccounts: Int,
    onBack: () -> Unit,
) {
    val layout = options.home
    GroupScaffold(R.string.nav_home, onBack) {
        item { CoShellHint(stringResource(R.string.settings_home_intro), Modifier.padding(horizontal = Spacing.l, vertical = Spacing.m)) }
        item { SectionTitle(stringResource(R.string.settings_home_cards)) }
        item {
            CardList(
                layout = layout,
                recentCount = options.recentCount,
                onShown = { card, shown -> change { it.copy(home = it.home.withShown(card, shown)) } },
                onMove = { from, to -> change { it.copy(home = it.home.move(from, to)) } },
            )
        }
        item { SectionTitle(stringResource(R.string.settings_home_details)) }
        item {
            CoShellRowGroup(Modifier.padding(horizontal = Spacing.l).padding(bottom = Spacing.l)) {
                row {
                    CoShellMenuRow(
                        DesignR.drawable.ic_calendar_today, categoryColor(CategoryPalette.colors[8]),
                        stringResource(R.string.settings_home_details_title), onDetails,
                        subtitle = stringResource(R.string.settings_home_details_hint, options.soonDays, options.recentCount),
                    )
                }
            }
        }
        item { SectionTitle(stringResource(R.string.settings_home_balance_section)) }
        item {
            CoShellRowGroup(Modifier.padding(horizontal = Spacing.l).padding(bottom = Spacing.l)) {
                row {
                    CoShellMenuRow(
                        DesignR.drawable.ic_account_balance_wallet, categoryColor(CategoryPalette.colors[5]),
                        stringResource(R.string.settings_balance_title), onBalance,
                        subtitle = if (excludedAccounts == 0) stringResource(R.string.settings_balance_all) else stringResource(R.string.settings_balance_except, excludedAccounts),
                    )
                }
            }
        }
        item {
            ResetLink(enabled = options.home != HomeLayout() || options.soonDays != 7 || options.recentCount != 5) {
                change { it.copy(home = HomeLayout(), soonDays = ScreenOptions().soonDays, recentCount = ScreenOptions().recentCount) }
            }
        }
    }
}

/** The cards in one group: the balance (locked), then each with ⠿ to drag and a switch. */
@Composable
private fun CardList(layout: HomeLayout, recentCount: Int, onShown: (HomeCard, Boolean) -> Unit, onMove: (Int, Int) -> Unit) {
    var dragging by remember { mutableIntStateOf(-1) }
    var offset by remember { mutableFloatStateOf(0f) }
    var rowHeight by remember { mutableFloatStateOf(0f) }
    CoShellRowGroup(Modifier.padding(horizontal = Spacing.l).padding(bottom = Spacing.l)) {
        row {
            CoShellListRow(
                title = stringResource(R.string.settings_home_balance),
                subtitle = stringResource(R.string.settings_home_balance_hint),
                leading = { Box(Modifier.size(Sizes.badgeSmall)) },
                trailing = { Icon(painterResource(DesignR.drawable.ic_lock), contentDescription = null) },
            )
        }
        layout.order.forEachIndexed { index, card ->
            row {
                val shown = layout.isShown(card)
                Box(
                    Modifier
                        .onSizeChanged { if (rowHeight == 0f) rowHeight = it.height.toFloat() }
                        .zIndex(if (dragging == index) 1f else 0f)
                        .graphicsLayer { translationY = if (dragging == index) offset else 0f },
                ) {
                    CoShellSwitchRow(
                        title = stringResource(card.titleRes),
                        subtitle = if (card == HomeCard.RECENT) stringResource(R.string.settings_home_recent_hint, recentCount) else stringResource(card.hintRes),
                        checked = shown,
                        onCheckedChange = { onShown(card, it) },
                        leading = {
                            Icon(
                                painterResource(DesignR.drawable.ic_drag_indicator),
                                contentDescription = stringResource(R.string.settings_home_drag),
                                modifier = Modifier
                                    .size(Sizes.badgeSmall)
                                    .padding(Spacing.xs)
                                    .pointerInput(card, layout.order) {
                                        detectDragGestures(
                                            onDragStart = { dragging = index; offset = 0f },
                                            onDrag = { change, drag -> change.consume(); offset += drag.y },
                                            onDragEnd = {
                                                val steps = if (rowHeight > 0f) (offset / rowHeight).roundToInt() else 0
                                                onMove(index, (index + steps).coerceIn(0, layout.order.lastIndex))
                                                dragging = -1; offset = 0f
                                            },
                                            onDragCancel = { dragging = -1; offset = 0f },
                                        )
                                    },
                            )
                        },
                    )
                }
            }
        }
    }
}

/** «Обзор: подробности»: the horizon of «Скоро» and the count of «Последние». */
@Composable
fun HomeDetailsRoute(onBack: () -> Unit, viewModel: ScreenOptionsViewModel = hiltViewModel()) {
    val o by viewModel.options.collectAsStateWithLifecycle()
    HomeDetailsScreen(o, { f -> viewModel.change(change = f) }, onBack)
}

@Composable
fun HomeDetailsScreen(options: ScreenOptions, change: ((ScreenOptions) -> ScreenOptions) -> Unit, onBack: () -> Unit) {
    var picking by rememberSaveable { mutableIntStateOf(-1) } // 0 — the first tile, 1 — the second
    GroupScaffold(R.string.settings_home_details_title, onBack) {
        item {
            PickCard(
                title = stringResource(R.string.settings_home_soon_title),
                labels = ScreenOptions.SOON_DAYS.map { stringResource(R.string.settings_home_days, it) },
                selected = ScreenOptions.SOON_DAYS.indexOf(options.soonDays).coerceAtLeast(0),
                onSelect = { i -> change { it.copy(soonDays = ScreenOptions.SOON_DAYS[i]) } },
            )
        }
        item { SectionTitle(stringResource(R.string.settings_home_recent_section)) }
        item {
            PickCard(
                title = stringResource(R.string.settings_home_recent_title),
                labels = ScreenOptions.RECENT_COUNTS.map { it.toString() },
                selected = ScreenOptions.RECENT_COUNTS.indexOf(options.recentCount).coerceAtLeast(0),
                onSelect = { i -> change { it.copy(recentCount = ScreenOptions.RECENT_COUNTS[i]) } },
            )
        }
        item { SectionTitle(stringResource(R.string.settings_home_tiles_section)) }
        item {
            CoShellRowGroup(Modifier.padding(horizontal = Spacing.l).padding(bottom = Spacing.s)) {
                row {
                    CoShellMenuRow(DesignR.drawable.ic_payments, categoryColor(CategoryPalette.colors[8]), stringResource(R.string.settings_home_tile_first), { picking = 0 }, subtitle = stringResource(options.tileFirst.titleRes))
                }
                row {
                    CoShellMenuRow(DesignR.drawable.ic_bar_chart, categoryColor(CategoryPalette.colors[13]), stringResource(R.string.settings_home_tile_second), { picking = 1 }, subtitle = stringResource(options.tileSecond.titleRes))
                }
            }
        }
        item { CoShellHint(stringResource(R.string.settings_home_tiles_note), Modifier.padding(horizontal = Spacing.l, vertical = Spacing.s)) }
    }
    if (picking >= 0) {
        val second = picking == 1
        ChoiceSheet(
            title = stringResource(if (second) R.string.settings_home_tile_second else R.string.settings_home_tile_first),
            options = HomeTile.entries.map { ChoiceOption(it, stringResource(it.titleRes)) },
            selected = if (second) options.tileSecond else options.tileFirst,
            onPick = { tile -> change { if (second) it.withSecond(tile) else it.withFirst(tile) }; picking = -1 },
            onDismiss = { picking = -1 },
        )
    }
}

private val HomeCard.titleRes: Int
    get() = when (this) {
        HomeCard.REVIEW -> R.string.settings_home_review
        HomeCard.UPCOMING -> R.string.settings_home_upcoming
        HomeCard.TILES -> R.string.settings_home_tiles
        HomeCard.HOT -> R.string.settings_home_hot
        HomeCard.RECENT -> R.string.settings_home_recent
    }

private val HomeCard.hintRes: Int
    get() = when (this) {
        HomeCard.REVIEW -> R.string.settings_home_review_hint
        HomeCard.UPCOMING -> R.string.settings_home_upcoming_hint
        HomeCard.TILES -> R.string.settings_home_tiles_hint
        HomeCard.HOT -> R.string.settings_home_hot_hint
        HomeCard.RECENT -> R.string.settings_home_recent_hint
    }

/** «Вернуть стандартные»: the cards and details of «Обзор» as the app ships them. */
@Composable
private fun ResetLink(enabled: Boolean, onClick: () -> Unit) {
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        CoShellTextButton(onClick = onClick, enabled = enabled) { Text(stringResource(R.string.settings_reset)) }
    }
}

private val HomeTile.titleRes: Int
    get() = when (this) {
        HomeTile.SPENDING -> R.string.settings_home_tile_spending
        HomeTile.BUDGET -> R.string.settings_home_tile_budget
        HomeTile.UNCATEGORIZED -> R.string.settings_home_tile_uncategorized
    }
