package com.notiguard.data

import org.junit.Assert.assertEquals
import org.junit.Test

class RecentSearchesTest {

    @Test fun blankQueriesAreIgnored() {
        assertEquals(listOf("開會"), RecentSearches.remember(listOf("開會"), "  "))
        assertEquals(listOf("開會"), RecentSearches.remember(listOf("開會"), "\u001f"))
    }

    @Test fun newestQueryMovesToFrontAndDropsCaseDuplicates() {
        val next = RecentSearches.remember(listOf("LINE", "開會"), "  line ")
        assertEquals(listOf("line", "開會"), next)
    }

    @Test fun keepsOnlyTheNewestTen() {
        val existing = (1..10).map { "q$it" }
        val next = RecentSearches.remember(existing, "新的")
        assertEquals("新的", next.first())
        assertEquals(10, next.size)
        assertEquals("q9", next.last())
    }

    @Test fun roundTripsQueriesThatContainCommasAndQuotes() {
        val queries = listOf("O'Brien", "100%", "a,b")
        assertEquals(queries, RecentSearches.decode(RecentSearches.encode(queries)))
    }

    @Test fun forgetRemovesOneTermWithoutTouchingTheOthers() {
        assertEquals(listOf("開會"), RecentSearches.forget(listOf("LINE", "開會"), "line"))
    }
}
