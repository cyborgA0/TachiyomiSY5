package eu.kanade.tachiyomi.ui.browse.source

import androidx.compose.runtime.Composable
import eu.kanade.domain.source.model.Source
import eu.kanade.presentation.browse.SourcesFilterScreen
import eu.kanade.tachiyomi.ui.base.controller.FullComposeController

class SourceFilterController : FullComposeController<SourcesFilterPresenter>() {

    override fun createPresenter(): SourcesFilterPresenter = SourcesFilterPresenter()

    @Composable
    override fun ComposeContent() {
        SourcesFilterScreen(
            navigateUp = router::popCurrentController,
            presenter = presenter,
            onClickLang = { language ->
                presenter.toggleLanguage(language)
            },
            onClickSource = { source ->
                presenter.toggleSource(source)
            },
            // SY -->
            onClickSources = { isEnable, sources ->
                presenter.toggleSources(isEnable, sources)
            },
            // SY <--
        )
    }
}

sealed class FilterUiModel {
    data class Header(val language: String, val enabled: Boolean) : FilterUiModel()

    // SY -->
    data class ToggleHeader(val sources: List<Source>, val enabled: Boolean) : FilterUiModel()

    // SY <--
    data class Item(val source: Source, val enabled: Boolean) : FilterUiModel()
}
