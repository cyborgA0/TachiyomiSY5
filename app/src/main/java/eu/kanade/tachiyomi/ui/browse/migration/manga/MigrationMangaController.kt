package eu.kanade.tachiyomi.ui.browse.migration.manga

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.core.os.bundleOf
import eu.kanade.presentation.browse.MigrateMangaScreen
import eu.kanade.tachiyomi.data.preference.PreferencesHelper
import eu.kanade.tachiyomi.ui.base.controller.FullComposeController
import eu.kanade.tachiyomi.ui.base.controller.pushController
import eu.kanade.tachiyomi.ui.browse.migration.advanced.design.PreMigrationController
import eu.kanade.tachiyomi.ui.manga.MangaController
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class MigrationMangaController : FullComposeController<MigrateMangaPresenter> {

    constructor(sourceId: Long, sourceName: String?) : super(
        bundleOf(
            SOURCE_ID_EXTRA to sourceId,
            SOURCE_NAME_EXTRA to sourceName,
        ),
    )

    @Suppress("unused")
    constructor(bundle: Bundle) : this(
        bundle.getLong(SOURCE_ID_EXTRA),
        bundle.getString(SOURCE_NAME_EXTRA),
    )

    private val sourceId: Long = args.getLong(SOURCE_ID_EXTRA)
    private val sourceName: String? = args.getString(SOURCE_NAME_EXTRA)

    override fun createPresenter() = MigrateMangaPresenter(sourceId)

    @Composable
    override fun ComposeContent() {
        MigrateMangaScreen(
            navigateUp = router::popCurrentController,
            title = sourceName,
            presenter = presenter,
            onClickItem = {
                PreMigrationController.navigateToMigration(
                    Injekt.get<PreferencesHelper>().skipPreMigration().get(),
                    router,
                    listOf(it.id),
                )
            },
            onClickCover = {
                router.pushController(MangaController(it.id))
            },
        )
    }

    companion object {
        const val SOURCE_ID_EXTRA = "source_id_extra"
        const val SOURCE_NAME_EXTRA = "source_name_extra"
    }
}
