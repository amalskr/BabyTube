package com.ceylonapz.babytube.data.local

/** Where the next search for a category should continue from. */
data class SearchCursor(val pageToken: String, val pageIndex: Int)

interface SearchCursorStore {
    suspend fun get(key: String): SearchCursor?

    /** Saves the cursor, or clears it when [cursor] is null (next search starts from page 1). */
    suspend fun set(key: String, cursor: SearchCursor?)
}
