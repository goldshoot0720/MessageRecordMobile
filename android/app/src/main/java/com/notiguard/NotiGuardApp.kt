package com.notiguard

import android.app.Application
import com.notiguard.data.NotiGuardDatabase
import com.notiguard.data.NotiGuardRepository
import com.notiguard.service.AppIdentity

/**
 * 手動組裝依賴。這個 App 的物件圖小到不值得引入 DI framework。
 */
class NotiGuardApp : Application() {


    val repository: NotiGuardRepository by lazy {
        NotiGuardRepository(this, NotiGuardDatabase.get(this).dao())
    }

    val appIdentity: AppIdentity by lazy { AppIdentity(this) }


}

