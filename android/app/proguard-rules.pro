# NotificationListenerService 由系統以名稱綁定，不能被混淆或移除。
-keep class com.notiguard.service.NotiGuardListenerService { *; }

# Room 產生的實作。
-keep class com.notiguard.data.** { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase { public **; }
