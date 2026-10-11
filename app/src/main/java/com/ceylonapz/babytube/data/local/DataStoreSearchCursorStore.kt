package com.ceylonapz.babytube.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first

class DataStoreSearchCursorStore(
    private val dataStore: DataStore<Preferences>,
) : SearchCursorStore {

    override suspend fun get(key: String): SearchCursor? {
        val prefs = dataStore.data.first()
        val token = prefs[tokenKey(key)] ?: return null
        return SearchCursor(token, prefs[indexKey(key)] ?: 0)
    }

    override suspend fun set(key: String, cursor: SearchCursor?) {
        dataStore.edit { prefs ->
            if (cursor == null) {
                prefs.remove(tokenKey(key))
                prefs.remove(indexKey(key))
            } else {
                prefs[tokenKey(key)] = cursor.pageToken
                prefs[indexKey(key)] = cursor.pageIndex
            }
        }
    }

    private fun tokenKey(key: String) = stringPreferencesKey("page_token:$key")
    private fun indexKey(key: String) = intPreferencesKey("page_index:$key")
}
