package com.notiguard.data

import androidx.room.Room
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
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
class NotificationSearchTest {
    private lateinit var db: NotiGuardDatabase
    private lateinit var repo: NotiGuardRepository

    @Before fun setup() = runBlocking {
        val context = RuntimeEnvironment.getApplication()
        db = Room.inMemoryDatabaseBuilder(context, NotiGuardDatabase::class.java).build()
        repo = NotiGuardRepository(context, db.dao())
        db.dao().insertRecords(listOf(
            record("a", "jp.line", "LINE", "小明", "明天開會 100%", 1, true),
            record("b", "jp.line", "LINE", "開會通知", "O'Brien_a", 3, false),
            record("c", "com.mail", "Mail", "工作", "開會資料", 2, true),
        ))
    }

    @After fun teardown() { db.close() }

    private fun record(uid: String, pkg: String, label: String, title: String,
                       text: String, time: Long, blocked: Boolean) = NotificationRecord(
        uid = uid, packageName = pkg, appLabel = label, title = title, text = text,
        postedAt = time, blocked = blocked)

    private suspend fun find(query: String, pkg: String? = null, filter: RecordFilter = RecordFilter.ALL) =
        repo.searchRecords(query, pkg, filter).first().map { it.uid }

    @Test fun matchesChineseTitleAndBodyInNewestFirstOrder() = runBlocking {
        assertEquals(listOf("b", "c", "a"), find(" 開會 "))
        assertEquals(listOf("a"), find("小明"))
    }

    @Test fun matchesAppNamesAndPackagesIgnoringAsciiCase() = runBlocking {
        assertEquals(listOf("b", "a"), find("line"))
        assertEquals(listOf("c"), find("COM.MAIL"))
    }

    @Test fun combinesAppScopeWithBlockedAndAllowedFilters() = runBlocking {
        assertEquals(listOf("a"), find("開會", "jp.line", RecordFilter.BLOCKED))
        assertEquals(listOf("b"), find("開會", "jp.line", RecordFilter.ALLOWED))
        assertEquals(listOf("c", "a"), find("開會", filter = RecordFilter.BLOCKED))
    }

    @Test fun treatsSqlWildcardsAndQuotesAsLiteralText() = runBlocking {
        assertEquals(listOf("a"), find("%"))
        assertEquals(listOf("b"), find("_"))
        assertEquals(listOf("b"), find("O'Brien"))
        assertEquals(emptyList<String>(), find("' OR 1=1 --"))
    }

    @Test fun blankAndMissingQueriesReturnNoResults() = runBlocking {
        assertEquals(emptyList<String>(), find("  "))
        assertEquals(emptyList<String>(), find("不存在的內容"))
    }

    @Test fun activeSearchReceivesNewMatchingNotifications() = runBlocking {
        withTimeout(5000) {
            val updated = async {
                repo.searchRecords("新通知", null, RecordFilter.ALL).first { it.isNotEmpty() }
            }
            db.dao().insertRecord(record("d", "com.mail", "Mail", "新通知", "內容", 4, false))
            assertEquals("d", updated.await().single().uid)
        }
    }
}
