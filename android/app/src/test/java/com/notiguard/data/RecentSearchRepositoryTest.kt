package com.notiguard.data

import androidx.room.Room
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class RecentSearchRepositoryTest {
    private lateinit var db: NotiGuardDatabase
    private lateinit var repo: NotiGuardRepository

    @Before fun setup() = runBlocking {
        val context = RuntimeEnvironment.getApplication()
        db = Room.inMemoryDatabaseBuilder(context, NotiGuardDatabase::class.java).build()
        repo = NotiGuardRepository(context, db.dao())
        repo.clearRecentSearches()
    }

    @After fun teardown() { db.close() }

    @Test fun remembersNotificationSearchesNewestFirst() = runBlocking {
        repo.rememberSearch("開會")
        repo.rememberSearch(" LINE ")
        repo.rememberSearch("line")
        assertEquals(listOf("line", "開會"), repo.recentSearches.first())
    }

    @Test fun forgetAndClearRemoveStoredSearches() = runBlocking {
        repo.rememberSearch("開會")
        repo.rememberSearch("LINE")
        repo.forgetSearch("開會")
        assertEquals(listOf("LINE"), repo.recentSearches.first())
        repo.clearRecentSearches()
        assertEquals(emptyList<String>(), repo.recentSearches.first())
    }
}
