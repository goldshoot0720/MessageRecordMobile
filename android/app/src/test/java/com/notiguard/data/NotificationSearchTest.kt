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
@Config(sdk = [28], application = android.app.Application::class)
class NotificationSearchTest {
    private lateinit var db: NotiGuardDatabase
    private lateinit var repo: NotiGuardRepository
    private val dao get() = db.dao()

    @Before
    fun setup() = runBlocking {
        val context = RuntimeEnvironment.getApplication()
        db = Room.inMemoryDatabaseBuilder(context, NotiGuardDatabase::class.java).build()
        repo = NotiGuardRepository(context, dao)
        dao.insertRecords(listOf(
            record("1", "com.chat", "Chat", "會議提醒", "明天討論 Project Alpha", true, 30),
            record("2", "com.chat", "Chat", "優惠", "優惠 50%_off \\ 今天", false, 20),
            record("3", "com.chat", "Chat", "一般訊息", "沒有關鍵字", true, 10),
            record("4", "com.mail", "Mail", "Project Alpha", "會議附件", false, 40),
        ))
        dao.upsertRule(AppRule("com.chat", "Chat", blocking = false))
    }

    @After
    fun close() { db.close() }

    @Test
    fun searchesTitlesBodiesNamesAndPackagesIgnoringCaseAndOuterSpaces() = runBlocking {
        assertEquals(setOf("com.chat", "com.mail"), repo.searchApps("  project ALPHA  ").first().map { it.packageName }.toSet())
        assertEquals(listOf("1"), repo.searchRecords("com.chat", RecordFilter.ALL, "  會議  ").first().map { it.uid })
        assertEquals(3, repo.searchRecords("com.chat", RecordFilter.ALL, "CHAT").first().size)
        assertEquals(3, repo.searchRecords("com.chat", RecordFilter.ALL, "COM.CHAT").first().size)
    }

    @Test
    fun appResultsKeepFullCountsAndRules() = runBlocking {
        val app = repo.searchApps("明天").first().single()
        assertEquals("com.chat", app.packageName)
        assertEquals(3, app.total)
        assertEquals(2, app.blockedCount)
        assertEquals(false, app.isBlocking)
        assertEquals(4, repo.stats.first().total)
    }

    @Test
    fun combinesSearchWithStatusAndPackageScope() = runBlocking {
        assertEquals(listOf("1"), repo.searchRecords("com.chat", RecordFilter.BLOCKED, "alpha").first().map { it.uid })
        assertEquals(emptyList<NotificationRecord>(), repo.searchRecords("com.chat", RecordFilter.ALLOWED, "alpha").first())
        assertEquals(listOf("4"), repo.searchRecords("com.mail", RecordFilter.ALLOWED, "alpha").first().map { it.uid })
    }

    @Test
    fun punctuationIsLiteralAndNoMatchesAreEmpty() = runBlocking {
        for (query in listOf("%", "_", "\\", "50%_off")) {
            assertEquals(listOf("2"), repo.searchRecords("com.chat", RecordFilter.ALL, query).first().map { it.uid })
        }
        assertEquals(0, repo.searchApps("' OR 1=1 --").first().size)
        assertEquals(0, repo.searchRecords("com.chat", RecordFilter.ALL, "不存在").first().size)
    }

    @Test
    fun clearingSearchRestoresNewestFirstRecordsAndAllApps() = runBlocking {
        assertEquals(listOf("1", "2", "3"), repo.searchRecords("com.chat", RecordFilter.ALL, " \n ").first().map { it.uid })
        assertEquals(2, repo.searchApps("").first().size)
        assertEquals(listOf("1", "3"), repo.searchRecords("com.chat", RecordFilter.BLOCKED, "").first().map { it.uid })
    }

    @Test
    fun activeSearchReceivesNewMatchingNotifications() = runBlocking {
        withTimeout(5_000) {
            val update = async { repo.searchRecords("com.chat", RecordFilter.ALL, "新訊息").first { it.isNotEmpty() } }
            dao.insertRecord(record("5", "com.chat", "Chat", "新訊息", "即時搜尋", true, 50))
            assertEquals(listOf("5"), update.await().map { it.uid })
            assertEquals("com.chat", repo.searchApps("新訊息").first().single().packageName)
        }
    }

    private fun record(uid: String, pkg: String, label: String, title: String, text: String, blocked: Boolean, time: Long) =
        NotificationRecord(uid = uid, packageName = pkg, appLabel = label, title = title,
            text = text, blocked = blocked, postedAt = time)
}
