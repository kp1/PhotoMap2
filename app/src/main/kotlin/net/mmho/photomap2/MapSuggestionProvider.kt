package net.mmho.photomap2

import android.content.SearchRecentSuggestionsProvider

class MapSuggestionProvider : SearchRecentSuggestionsProvider() {

    init {
        setupSuggestions(AUTHORITY, MODE)
    }

    companion object {
        const val AUTHORITY = "net.mmho.MapSuggestionProvider"
        const val MODE = SearchRecentSuggestionsProvider.DATABASE_MODE_QUERIES
    }
}
