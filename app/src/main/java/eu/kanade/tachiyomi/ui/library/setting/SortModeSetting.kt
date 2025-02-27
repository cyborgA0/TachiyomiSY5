package eu.kanade.tachiyomi.ui.library.setting

import eu.kanade.domain.category.model.Category
import eu.kanade.tachiyomi.data.preference.PreferencesHelper
import eu.kanade.tachiyomi.ui.library.LibraryGroup

enum class SortModeSetting(val flag: Long) {
    ALPHABETICAL(0b00000000),
    LAST_READ(0b00000100),
    LAST_MANGA_UPDATE(0b00001000),
    UNREAD_COUNT(0b00001100),
    TOTAL_CHAPTERS(0b00010000),
    LATEST_CHAPTER(0b00010100),
    CHAPTER_FETCH_DATE(0b00011000),
    DATE_ADDED(0b00011100),

    // SY -->
    @Deprecated("Was unused")
    DRAG_AND_DROP(0b00100000),
    TAG_LIST(0b00100100),
    // SY <--

    @Deprecated("Use LAST_MANGA_UPDATE")
    LAST_CHECKED(0b00001000),

    @Deprecated("Use UNREAD_COUNT")
    UNREAD(0b00001100),

    @Deprecated("Use CHAPTER_FETCH_DATE")
    DATE_FETCHED(0b00011000),
    ;

    companion object {
        // Mask supports for more sorting flags if necessary
        const val MASK = 0b00111100L

        fun fromFlag(flag: Long?): SortModeSetting {
            return values().find { mode -> mode.flag == flag } ?: ALPHABETICAL
        }

        fun get(preferences: PreferencesHelper, category: Category?): SortModeSetting {
            return if (category != null && preferences.categorizedDisplaySettings().get() /* SY --> */ && preferences.groupLibraryBy().get() == LibraryGroup.BY_DEFAULT/* SY <-- */) {
                fromFlag(category.sortMode)
            } else {
                preferences.librarySortingMode().get()
            }
        }
    }
}
