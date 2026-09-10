# MessageRecord

Android 通知攔截與紀錄 App，使用 Kotlin、Jetpack Compose、Room 與 DataStore。需授予通知存取權限，才會記錄系統實際送達的通知。

## 下載

[Releases](https://github.com/goldshoot0720/MessageRecordMobile/releases) 提供 APK 與 SHA-256 校驗碼。目前版本為 **1.0.3-preview.1**，Android 8.0 以上可用，套件 ID 為 `com.notiguard.debug`，可覆蓋先前預覽版。

## 通知紀錄

- 通知送達後儲存於装置本機；攔截會移出通知欄，但保留紀錄。
- 群組摘要不另存為訊息，仍依設定攔截；常駐通知一律放行。
- 以通知識別碼、來源訊息時間及內容的穩定雜湊去重，避免同一通知更新時重複新增。
- 不同通知識別碼、不同訊息時間或內容仍會分別保存。來源未提供訊息時間時退回系統發送時間，避免誤合併新訊息。
- 可從首頁搜尋全部已儲存通知，也可在單一應用程式頁搜尋；支援 App 名稱、套件名稱、標題與內容，並可篩選全部、已攔截或已允許。
- 搜尋關鍵字以一般文字比對，`%`、`_` 與引號不會被當成 SQL 萬用字元或指令。
- 可從應用程式詳情頁匯出該應用程式的紀錄為跨平台 JSON。
- 舊版重複紀錄保留，因為缺少原始摘要旗標與訊息時間，無法安全自動清理。
- Room 1 → 2 遷移只刪除舊版示範資料，保留真實紀錄與規則。

## 建置與驗證

需要 JDK 17 與 Android SDK 35，設定 `ANDROID_HOME` 或 `android/local.properties` 的 `sdk.dir`。

```powershell
cd android
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug :app:lintDebug
cd ..
python tests/test_real_data_migration.py
```

通知回歸測試透過 Robolectric 呼叫實際監聽服務並檢查 Room 資料，涵蓋摘要、更新、不同訊息、服務重啟、並行回呼、撤回及攔截行為。搜尋測試涵蓋中文、英文大小寫、應用程式範圍、攔截狀態與特殊字元。

## 原始碼

- `android/app/src/main/java/com/notiguard/service/`：通知接收與攔截。
- `android/app/src/main/java/com/notiguard/data/`：本機資料與規則。
- `android/app/src/main/java/com/notiguard/ui/`：Compose 介面。
- `android/app/src/test/`：通知回歸測試。
- `shared/`：紀錄資料格式。
- `release/`：版本說明與校驗碼；APK 放在 GitHub Release 附件。

預覽版使用 Debug 簽章；公開建置不會持有原發布者的簽章金鑰。
