package com.notiguard.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PopularAppsTest {

    @Test fun lineLeadsTheCatalog() {
        assertEquals("jp.naver.line.android", PopularApps.catalog.first().packageName)
        assertEquals("LINE", PopularApps.catalog.first().label)
    }

    @Test fun installedAppsKeepCatalogOrderAheadOfTheRest() {
        val installed = setOf("com.whatsapp", "jp.naver.line.android", "com.tencent.mm")
        val ordered = PopularApps.ordered { it in installed }.map { it.packageName }
        assertEquals(
            listOf("jp.naver.line.android", "com.whatsapp", "com.tencent.mm"),
            ordered.take(3),
        )
        assertTrue(ordered.drop(3).none { it in installed })
        assertEquals(PopularApps.catalog.size, ordered.size)
    }
}
