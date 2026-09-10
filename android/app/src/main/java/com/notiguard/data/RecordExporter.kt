package com.notiguard.data

import org.json.JSONArray
import org.json.JSONObject

/**
 * 把紀錄匯出成跨平台格式，供 iOS 版匯入。
 *
 * 欄位與 shared/notiguard-record.schema.json 完全一致，時間一律是 Unix epoch 毫秒。
 * 用 org.json 而不是 kotlinx.serialization，是因為只有這一處要序列化，
 * 為它多拉一個 plugin 與 runtime 不划算。
 */
object RecordExporter {

    /** iOS 端的 `CompanionSync.Payload` 期待的外層結構。 */
    fun toJson(records: List<NotificationRecord>, deviceId: String): String {
        val array = JSONArray()
        records.forEach { array.put(it.toJson(deviceId)) }

        return JSONObject().apply {
            put("version", 1)
            put("exportedAt", System.currentTimeMillis())
            put("deviceId", deviceId)
            put("records", array)
        }.toString(2)
    }

    private fun NotificationRecord.toJson(deviceId: String): JSONObject = JSONObject().apply {
        put("uid", uid)
        put("packageName", packageName)
        put("appLabel", appLabel)
        put("title", title)
        put("text", text)
        put("channelId", channelId ?: JSONObject.NULL)
        put("category", category ?: JSONObject.NULL)
        put("postedAt", postedAt)
        put("removedAt", removedAt ?: JSONObject.NULL)
        put("blocked", blocked)
        put("ongoing", ongoing)
        put("source", source)
        put("deviceId", deviceId)
    }

    /** 匯出檔名。帶上 App 名稱與日期，使用者在檔案 App 裡找得到。 */
    fun fileName(appLabel: String?): String {
        val stamp = java.text.SimpleDateFormat("yyyyMMdd-HHmm", java.util.Locale.US)
            .format(java.util.Date())
        val scope = appLabel?.replace(Regex("[^\\p{L}\\p{N}]"), "") ?: "all"
        return "notiguard-$scope-$stamp.json"
    }
}
