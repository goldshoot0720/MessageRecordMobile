package com.notiguard.ui

/**
 * 首頁「常用」捷徑。這些套件名稱是各商店的正式 ID，
 * 沒安裝時仍顯示品牌圖示，點進去可以先看紀錄或預先設定攔截。
 */
data class PopularApp(
    val packageName: String,
    val label: String,
)

object PopularApps {
    val catalog: List<PopularApp> = listOf(
        PopularApp("jp.naver.line.android", "LINE"),
        PopularApp("com.facebook.orca", "Messenger"),
        PopularApp("com.instagram.android", "Instagram"),
        PopularApp("com.facebook.katana", "Facebook"),
        PopularApp("com.whatsapp", "WhatsApp"),
        PopularApp("org.telegram.messenger", "Telegram"),
        PopularApp("com.tencent.mm", "微信"),
        PopularApp("com.instagram.barcelona", "Threads"),
        PopularApp("com.google.android.gm", "Gmail"),
        PopularApp("com.google.android.youtube", "YouTube"),
        PopularApp("com.shopee.tw", "蝦皮"),
        PopularApp("com.dcard.mobile", "Dcard"),
    )

    /** 已安裝的排前面，同一組裡維持目錄順序，所以裝了 LINE 時它仍在最左。 */
    fun ordered(isInstalled: (String) -> Boolean): List<PopularApp> {
        val (installed, missing) = catalog.partition { isInstalled(it.packageName) }
        return installed + missing
    }
}
