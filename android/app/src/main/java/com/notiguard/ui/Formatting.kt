package com.notiguard.ui

import java.util.Calendar
import java.util.Locale

/** 畫面上的時間格式。全部走當地時區。 */
object Fmt {

    private fun cal(millis: Long): Calendar =
        Calendar.getInstance().apply { timeInMillis = millis }

    /** 09:32 */
    fun time(millis: Long): String {
        val c = cal(millis)
        return String.format(
            Locale.TAIWAN, "%02d:%02d",
            c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE),
        )
    }

    /** 2026/09/10 08:15:32 */
    fun timestamp(millis: Long): String {
        val c = cal(millis)
        return String.format(
            Locale.TAIWAN, "%04d/%02d/%02d  %02d:%02d:%02d",
            c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH),
            c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND),
        )
    }

    /** 2026 年 9 月 10 日 08:15 */
    fun longDateTime(millis: Long): String {
        val c = cal(millis)
        return "${c.get(Calendar.YEAR)} 年 ${c.get(Calendar.MONTH) + 1} 月 " +
            "${c.get(Calendar.DAY_OF_MONTH)} 日 ${time(millis)}"
    }

    /** 分組標題：今天 / 昨天 / 9 月 8 日 */
    fun dayLabel(millis: Long): String = when (daysAgo(millis)) {
        0 -> "今天"
        1 -> "昨天"
        else -> {
            val c = cal(millis)
            "${c.get(Calendar.MONTH) + 1} 月 ${c.get(Calendar.DAY_OF_MONTH)} 日"
        }
    }

    /** 分組副標：9 月 10 日 */
    fun daySub(millis: Long): String {
        val c = cal(millis)
        return "${c.get(Calendar.MONTH) + 1} 月 ${c.get(Calendar.DAY_OF_MONTH)} 日"
    }

    /** 清單右側的相對時間。 */
    fun relative(millis: Long): String {
        val diff = System.currentTimeMillis() - millis
        val minutes = diff / 60_000
        return when {
            minutes < 1 -> "剛剛"
            minutes < 60 -> "$minutes 分鐘前"
            minutes < 60 * 24 && daysAgo(millis) == 0 -> "今天 ${time(millis)}"
            daysAgo(millis) == 1 -> "昨天 ${time(millis)}"
            else -> daySub(millis)
        }
    }

    /** 把紀錄依「日」切段，用來做分組標題。 */
    fun dayKey(millis: Long): Long {
        val c = cal(millis).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return c.timeInMillis
    }

    private fun daysAgo(millis: Long): Int {
        val today = dayKey(System.currentTimeMillis())
        val that = dayKey(millis)
        return ((today - that) / 86_400_000L).toInt()
    }
}
