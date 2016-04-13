package net.mmho.photomap2

import android.content.SearchRecentSuggestionsProvider

class MapSuggestionProvider : SearchRecentSuggestionsProvider() {

    init {
        setupSuggestions(AUTHORITY, MODE)
    }

    companion object {
        val AUTHORITY = "net.mmho.MapSuggestionProvider"
        val MODE = SearchRecentSuggestionsProvider.DATABASE_MODE_QUERIES
    }
}
