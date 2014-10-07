package net.mmho.photomap2;

import android.content.SearchRecentSuggestionsProvider;

public class MapSuggestionProvider extends SearchRecentSuggestionsProvider{
    public final static String AUTHORITY = "net.mmho.MapSuggestionProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public MapSuggestionProvider(){
        setupSuggestions(AUTHORITY,MODE);
    }
}
