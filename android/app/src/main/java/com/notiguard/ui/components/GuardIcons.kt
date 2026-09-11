package com.notiguard.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.notiguard.R

/**
 * Meowa 產出的彩色可愛 PNG 圖示（各密度 drawable-*dpi）。
 * 顏色固定、不跟隨主題 tint。
 */
object GuardIcons {

    // ---- 通知／狀態 ----
    @DrawableRes val Bell = R.drawable.ic_cute_bell
    @DrawableRes val BellOff = R.drawable.ic_cute_bell_off
    @DrawableRes val Shield = R.drawable.ic_cute_shield
    @DrawableRes val Clock = R.drawable.ic_cute_clock
    @DrawableRes val Message = R.drawable.ic_cute_message
    @DrawableRes val Block = R.drawable.ic_cute_block
    @DrawableRes val Check = R.drawable.ic_cute_check_circle
    @DrawableRes val CheckMark = R.drawable.ic_cute_check

    // ---- 導覽 ----
    @DrawableRes val Back = R.drawable.ic_cute_back
    @DrawableRes val ChevronRight = R.drawable.ic_cute_chevron_right
    @DrawableRes val Home = R.drawable.ic_cute_home
    @DrawableRes val Chart = R.drawable.ic_cute_chart
    @DrawableRes val Settings = R.drawable.ic_cute_settings
    @DrawableRes val Search = R.drawable.ic_cute_search
    @DrawableRes val Close = R.drawable.ic_cute_close
    @DrawableRes val More = R.drawable.ic_cute_more

    // ---- 動作 ----
    @DrawableRes val Filter = R.drawable.ic_cute_filter
    @DrawableRes val Export = R.drawable.ic_cute_export
    @DrawableRes val Trash = R.drawable.ic_cute_trash
    @DrawableRes val Refresh = R.drawable.ic_cute_refresh
    @DrawableRes val Tag = R.drawable.ic_cute_tag
    @DrawableRes val Info = R.drawable.ic_cute_info
    @DrawableRes val Apps = R.drawable.ic_cute_apps
}

/** 彩色 PNG 圖示；不套用 tint，保留 Meowa 原色。 */
@Composable
fun GuardIcon(
    @DrawableRes icon: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    Image(
        painter = painterResource(icon),
        contentDescription = contentDescription,
        modifier = modifier,
    )
}
