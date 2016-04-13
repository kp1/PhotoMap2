package net.mmho.photomap2

import android.content.SearchRecentSuggestionsProvider

class MapSuggestionProvider : SearchRecentSuggestionsProvider() {

    init {
        setupSuggestions(AUTHORITY, MODE)
    }

    companion object {
        val AUTHORITY = "net.mmho.net.mmho.photomap2.MapSuggestionProvider"
        val MODE = SearchRecentSuggestionsProvider.DATABASE_MODE_QUERIES
    }
}
