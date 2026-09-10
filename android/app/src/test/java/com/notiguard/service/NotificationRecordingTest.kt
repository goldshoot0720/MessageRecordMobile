package com.notiguard.service

import android.app.Notification
import android.os.UserHandle
import android.service.notification.StatusBarNotification
import android.service.notification.NotificationListenerService
import com.notiguard.NotiGuardApp
import com.notiguard.data.NotiGuardDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = NotiGuardApp::class)
class NotificationRecordingTest {
    private val app get() = RuntimeEnvironment.getApplication() as NotiGuardApp
    private val dao get() = NotiGuardDatabase.get(app).dao()

    @Before
    fun reset() = runBlocking {
        dao.deleteRecordsFor(PACKAGE)
        app.repository.setMasterEnabled(false)
    }

    @After fun closeDatabase() {
        NotiGuardDatabase.get(app).close()
        val instance = NotiGuardDatabase::class.java.getDeclaredField("instance")
        instance.isAccessible = true
        instance.set(null, null)
    }

    private fun event(id: Int, posted: Long, messageTime: Long = 1000L,
                      text: String = "37", summary: Boolean = false,
                      embeddedTime: Long? = null): StatusBarNotification {
        val builder = Notification.Builder(app, "messages")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Sender")
            .setContentText(text)
            .setWhen(messageTime)
            .setGroup("conversation")
            .setGroupSummary(summary)
        if (embeddedTime != null) builder.setStyle(Notification.MessagingStyle("Me")
            .addMessage(text, embeddedTime, "Sender"))
        val notification = builder.build()
        return StatusBarNotification(PACKAGE, PACKAGE, id, null, 10001, 0, 0,
            notification, UserHandle.getUserHandleForUid(10001), posted)
    }

    private fun record(vararg events: StatusBarNotification): Int = runBlocking {
        val controller = Robolectric.buildService(NotiGuardListenerService::class.java).create()
        try {
            for (event in events) {
                controller.get().onNotificationPosted(event)
                // Join actual listener writes; no timing-dependent sleeps.
                awaitWrites(controller.get())
            }
            dao.observeRecords(PACKAGE).first().size
        } finally {
            controller.destroy()
        }
    }

    private suspend fun awaitWrites(service: NotiGuardListenerService) {
        val scopeField = NotiGuardListenerService::class.java.getDeclaredField("scope")
        scopeField.isAccessible = true
        val scope = scopeField.get(service) as CoroutineScope
        scope.coroutineContext[Job]!!.children.toList().joinAll()
    }

    @Test fun summaryAndChildProduceOneRecord() {
        assertEquals(1, record(event(1, 2000), event(2, 2001, summary = true)))
    }

    @Test fun unchangedUpdateProducesOneRecord() {
        assertEquals(1, record(event(1, 2000), event(1, 2001)))
    }

    @Test fun sameTextAtNewMessageTimeIsPreserved() {
        assertEquals(2, record(event(1, 2000), event(1, 2001, messageTime = 1001)))
    }

    @Test fun differentConversationIsPreserved() {
        assertEquals(2, record(event(1, 2000), event(2, 2001)))
    }

    @Test fun changedContentIsPreserved() {
        assertEquals(2, record(event(1, 2000), event(1, 2001, text = "(3)(7)")))
    }

    @Test fun replayAfterServiceRestartIsDeduplicated() {
        assertEquals(1, record(event(1, 2000)))
        assertEquals(1, record(event(1, 2001)))
    }

    @Test fun missingMessageTimeDoesNotMergePotentialNewMessages() {
        assertEquals(2, record(event(1, 2000, messageTime = 0), event(1, 2001, messageTime = 0)))
    }

    @Test fun messagingTimestampDistinguishesNewIdenticalMessages() {
        assertEquals(2, record(event(1, 2000, embeddedTime = 900), event(1, 2001, embeddedTime = 901)))
    }

    @Test fun messagingUpdateWithNewNotificationTimeIsDeduplicated() {
        assertEquals(1, record(event(1, 2000, embeddedTime = 900),
            event(1, 2001, messageTime = 1001, embeddedTime = 900)))
    }

    @Test fun concurrentUpdatesStillProduceOneRecordAndRemovalMarksIt() = runBlocking {
        val controller = Robolectric.buildService(NotiGuardListenerService::class.java).create()
        try {
            repeat(20) { controller.get().onNotificationPosted(event(1, 2000L + it)) }
            awaitWrites(controller.get())
            assertEquals(1, dao.observeRecords(PACKAGE).first().size)
            controller.get().onNotificationRemoved(event(1, 2019))
            awaitWrites(controller.get())
            org.junit.Assert.assertNotNull(dao.observeRecords(PACKAGE).first().single().removedAt)
        } finally { controller.destroy() }
    }

    @Test fun summaryIsStillCancelledWhenBlockingIsEnabled() = runBlocking {
        app.repository.setMasterEnabled(true)
        val controller = Robolectric.buildService(NotiGuardListenerService::class.java).create()
        try {
            val summary = event(2, 2000, summary = true)
            shadowOf(controller.get() as NotificationListenerService).addActiveNotification(summary)
            controller.get().onNotificationPosted(summary)
            awaitWrites(controller.get())
            assertEquals(0, controller.get().activeNotifications.size)
            assertEquals(0, dao.observeRecords(PACKAGE).first().size)
        } finally { controller.destroy() }
    }

    companion object { private const val PACKAGE = "test.messages" }
}
