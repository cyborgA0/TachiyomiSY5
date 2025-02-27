package eu.kanade.presentation.browse

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import eu.kanade.domain.source.model.Pin
import eu.kanade.domain.source.model.Source
import eu.kanade.presentation.browse.components.BaseSourceItem
import eu.kanade.presentation.components.EmptyScreen
import eu.kanade.presentation.components.LoadingScreen
import eu.kanade.presentation.components.PreferenceRow
import eu.kanade.presentation.components.ScrollbarLazyColumn
import eu.kanade.presentation.theme.header
import eu.kanade.presentation.util.bottomNavPaddingValues
import eu.kanade.presentation.util.horizontalPadding
import eu.kanade.presentation.util.plus
import eu.kanade.presentation.util.topPaddingValues
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.source.LocalSource
import eu.kanade.tachiyomi.ui.browse.source.SourcesPresenter
import eu.kanade.tachiyomi.ui.browse.source.SourcesPresenter.Dialog
import eu.kanade.tachiyomi.util.system.LocaleHelper
import eu.kanade.tachiyomi.util.system.toast
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SourcesScreen(
    nestedScrollInterop: NestedScrollConnection,
    presenter: SourcesPresenter,
    onClickItem: (Source) -> Unit,
    onClickDisable: (Source) -> Unit,
    onClickLatest: (Source) -> Unit,
    onClickPin: (Source) -> Unit,
    onClickSetCategories: (Source, List<String>) -> Unit,
    onClickToggleDataSaver: (Source) -> Unit,
) {
    val context = LocalContext.current
    when {
        presenter.isLoading -> LoadingScreen()
        presenter.isEmpty -> EmptyScreen(R.string.source_empty_screen)
        else -> {
            SourceList(
                nestedScrollConnection = nestedScrollInterop,
                state = presenter,
                onClickItem = onClickItem,
                onClickDisable = onClickDisable,
                onClickLatest = onClickLatest,
                onClickPin = onClickPin,
                onClickSetCategories = onClickSetCategories,
                onClickToggleDataSaver = onClickToggleDataSaver,
            )
        }
    }
    LaunchedEffect(Unit) {
        presenter.events.collectLatest { event ->
            when (event) {
                SourcesPresenter.Event.FailedFetchingSources -> {
                    context.toast(R.string.internal_error)
                }
            }
        }
    }
}

@Composable
fun SourceList(
    nestedScrollConnection: NestedScrollConnection,
    state: SourcesState,
    onClickItem: (Source) -> Unit,
    onClickDisable: (Source) -> Unit,
    onClickLatest: (Source) -> Unit,
    onClickPin: (Source) -> Unit,
    onClickSetCategories: (Source, List<String>) -> Unit,
    onClickToggleDataSaver: (Source) -> Unit,
) {
    ScrollbarLazyColumn(
        modifier = Modifier.nestedScroll(nestedScrollConnection),
        contentPadding = bottomNavPaddingValues + WindowInsets.navigationBars.asPaddingValues() + topPaddingValues,
    ) {
        items(
            items = state.items,
            contentType = {
                when (it) {
                    is SourceUiModel.Header -> "header"
                    is SourceUiModel.Item -> "item"
                }
            },
            key = {
                when (it) {
                    is SourceUiModel.Header -> it.hashCode()
                    is SourceUiModel.Item -> it.source.key()
                }
            },
        ) { model ->
            when (model) {
                is SourceUiModel.Header -> {
                    SourceHeader(
                        modifier = Modifier.animateItemPlacement(),
                        language = model.language,
                        isCategory = model.isCategory,
                    )
                }
                is SourceUiModel.Item -> SourceItem(
                    modifier = Modifier.animateItemPlacement(),
                    source = model.source,
                    showLatest = state.showLatest,
                    showPin = state.showPin,
                    onClickItem = onClickItem,
                    onLongClickItem = { state.dialog = Dialog.SourceLongClick(it) },
                    onClickLatest = onClickLatest,
                    onClickPin = onClickPin,
                )
            }
        }
    }

    // SY -->
    when (val dialog = state.dialog) {
        is Dialog.SourceCategories -> {
            SourceCategoriesDialog(
                source = dialog.source,
                categories = state.categories,
                onClickCategories = { source, newCategories ->
                    onClickSetCategories(source, newCategories)
                    state.dialog = null
                },
                onDismiss = { state.dialog = null },
            )
        }
        is Dialog.SourceLongClick -> {
            val source = dialog.source
            SourceOptionsDialog(
                source = source,
                onClickPin = {
                    onClickPin(source)
                    state.dialog = null
                },
                onClickDisable = {
                    onClickDisable(source)
                    state.dialog = null
                },
                onClickSetCategories = {
                    state.dialog = Dialog.SourceCategories(source)
                },
                onClickToggleDataSaver = {
                    onClickToggleDataSaver(source)
                    state.dialog = null
                },
                onDismiss = { state.dialog = null },
            )
        }
        null -> Unit
    }
    // SY <--
}

@Composable
fun SourceHeader(
    modifier: Modifier = Modifier,
    language: String,
    isCategory: Boolean,
) {
    val context = LocalContext.current
    Text(
        text = if (!isCategory) {
            LocaleHelper.getSourceDisplayName(language, context)
        } else language,
        modifier = modifier
            .padding(horizontal = horizontalPadding, vertical = 8.dp),
        style = MaterialTheme.typography.header,
    )
}

@Composable
fun SourceItem(
    modifier: Modifier = Modifier,
    source: Source,
    // SY -->
    showLatest: Boolean,
    showPin: Boolean,
    // SY <--
    onClickItem: (Source) -> Unit,
    onLongClickItem: (Source) -> Unit,
    onClickLatest: (Source) -> Unit,
    onClickPin: (Source) -> Unit,
) {
    BaseSourceItem(
        modifier = modifier,
        source = source,
        onClickItem = { onClickItem(source) },
        onLongClickItem = { onLongClickItem(source) },
        action = { source ->
            if (source.supportsLatest /* SY --> */ && showLatest /* SY <-- */) {
                TextButton(onClick = { onClickLatest(source) }) {
                    Text(
                        text = stringResource(R.string.latest),
                        style = LocalTextStyle.current.copy(
                            color = MaterialTheme.colorScheme.primary,
                        ),
                    )
                }
            }
            // SY -->
            if (showPin) {
                SourcePinButton(
                    isPinned = Pin.Pinned in source.pin,
                    onClick = { onClickPin(source) },
                )
            }
            // SY <--
        },
    )
}

@Composable
fun SourcePinButton(
    isPinned: Boolean,
    onClick: () -> Unit,
) {
    val icon = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin
    val tint = if (isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
    IconButton(onClick = onClick) {
        Icon(
            imageVector = icon,
            contentDescription = "",
            tint = tint,
        )
    }
}

@Composable
fun SourceOptionsDialog(
    source: Source,
    onClickPin: () -> Unit,
    onClickDisable: () -> Unit,
    onClickSetCategories: () -> Unit,
    onClickToggleDataSaver: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        title = {
            Text(text = source.visualName)
        },
        text = {
            Column {
                val textId = if (Pin.Pinned in source.pin) R.string.action_unpin else R.string.action_pin
                Text(
                    text = stringResource(textId),
                    modifier = Modifier
                        .clickable(onClick = onClickPin)
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                )
                if (source.id != LocalSource.ID) {
                    Text(
                        text = stringResource(R.string.action_disable),
                        modifier = Modifier
                            .clickable(onClick = onClickDisable)
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                    )
                }
                // SY -->
                Text(
                    text = stringResource(id = R.string.categories),
                    modifier = Modifier
                        .clickable(onClick = onClickSetCategories)
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                )
                Text(
                    text = if (source.isExcludedFromDataSaver) {
                        stringResource(id = R.string.data_saver_stop_exclude)
                    } else {
                        stringResource(id = R.string.data_saver_exclude)
                    },
                    modifier = Modifier
                        .clickable(onClick = onClickToggleDataSaver)
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                )
                // SY <--
            }
        },
        onDismissRequest = onDismiss,
        confirmButton = {},
    )
}

sealed class SourceUiModel {
    data class Item(val source: Source) : SourceUiModel()
    data class Header(val language: String, val isCategory: Boolean) : SourceUiModel()
}

// SY -->
@Composable
fun SourceCategoriesDialog(
    source: Source?,
    categories: List<String>,
    onClickCategories: (Source, List<String>) -> Unit,
    onDismiss: () -> Unit,
) {
    source ?: return
    val newCategories = remember(source) {
        mutableStateListOf<String>().also { it += source.categories }
    }
    AlertDialog(
        title = {
            Text(text = source.visualName)
        },
        text = {
            Column {
                categories.forEach {
                    PreferenceRow(
                        title = it,
                        onClick = {
                            if (it in newCategories) {
                                newCategories -= it
                            } else {
                                newCategories += it
                            }
                        },
                        action = {
                            Checkbox(checked = it in newCategories, onCheckedChange = null)
                        },
                    )
                }
            }
        },
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onClickCategories(source, newCategories.toList()) }) {
                Text(text = stringResource(android.R.string.ok))
            }
        },
    )
}
// SY <--
