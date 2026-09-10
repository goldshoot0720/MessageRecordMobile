package com.notiguard.service

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.util.concurrent.ConcurrentHashMap

/**
 * 把 package name 換成人看得懂的名稱與圖示。
 *
 * 需要 QUERY_ALL_PACKAGES 才能查到任意套件。若不想申請該權限，
 * 可以只快取「通知曾出現過」的套件（監聽服務本來就拿得到 ApplicationInfo），
 * 或直接顯示 package name — 把 [label] 改成回傳 packageName 即可。
 */
class AppIdentity(context: Context) {

    private val pm: PackageManager = context.packageManager
    private val labels = ConcurrentHashMap<String, String>()
    private val icons = ConcurrentHashMap<String, ImageBitmap>()

    fun label(packageName: String): String = labels.getOrPut(packageName) {
        runCatching {
            pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString()
        }.getOrElse { packageName.substringAfterLast('.') }
    }

    /** 回傳 App 圖示；查不到時回 null，UI 會退回文字圖示。 */
    fun icon(packageName: String): ImageBitmap? = icons.getOrPut(packageName) {
        val drawable = runCatching { pm.getApplicationIcon(packageName) }.getOrNull()
            ?: return null
        drawable.toImageBitmap()
    }

    private fun Drawable.toImageBitmap(): ImageBitmap {
        if (this is BitmapDrawable && bitmap != null) return bitmap.asImageBitmap()
        val size = if (intrinsicWidth > 0) intrinsicWidth else DEFAULT_ICON_PX
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        setBounds(0, 0, canvas.width, canvas.height)
        draw(canvas)
        return bitmap.asImageBitmap()
    }

    private companion object {
        const val DEFAULT_ICON_PX = 108
    }
}
