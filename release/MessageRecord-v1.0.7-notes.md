MessageRecord 1.0.7 — 通知紀錄成為主畫面。

- 首頁主區改為可捲動的通知紀錄，至少佔可用畫面四分之三。上方只留攔截開關、時間範圍，以及「通知紀錄／應用程式」切換。
- 「應用程式」列出各 App 彙整；常用捷徑（LINE、Messenger、Instagram、Facebook、WhatsApp、Telegram、微信、Threads、Gmail、YouTube、蝦皮、Dcard）在這份清單上方。已安裝的排前面。
- 統計與通知存取設定收進右上角選單，不再佔清單高度。
- 單一應用程式頁的紀錄清單同樣佔主要高度，可捲動。

安裝：Android 8.0 以上，套件 ID `com.notiguard.debug`，versionCode 8，可直接覆蓋 v1.0.6 預覽版。此版本仍為 Debug 簽署預覽 APK。

驗證：30 項單元測試 0 失敗、assembleDebug、lintDebug（0 errors），以及真實資料遷移測試。
