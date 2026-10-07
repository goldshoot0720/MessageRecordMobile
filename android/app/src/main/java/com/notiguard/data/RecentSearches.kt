package com.notiguard.data

/**
 * 搜尋通知的最近關鍵字。最新的在最前面，重複的只留一筆，空白不記。
 */
object RecentSearches {
    const val LIMIT = 10
    private const val SEPARATOR = "\u001f"

    fun remember(existing: List<String>, query: String): List<String> {
        val text = normalize(query)
        if (text.isEmpty()) return existing
        return (listOf(text) + existing.filterNot { it.equals(text, ignoreCase = true) }).take(LIMIT)
    }

    fun forget(existing: List<String>, query: String): List<String> {
        val text = normalize(query)
        if (text.isEmpty()) return existing
        return existing.filterNot { it.equals(text, ignoreCase = true) }
    }

    fun encode(queries: List<String>): String =
        queries.map { normalize(it) }.filter { it.isNotEmpty() }.take(LIMIT).joinToString(SEPARATOR)

    fun decode(raw: String?): List<String> =
        raw.orEmpty()
            .split(SEPARATOR)
            .map { normalize(it) }
            .filter { it.isNotEmpty() }
            .distinctBy { it.lowercase() }
            .take(LIMIT)

    private fun normalize(query: String): String = query.trim().replace(SEPARATOR, "")
}
