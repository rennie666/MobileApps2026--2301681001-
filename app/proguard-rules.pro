# ProGuard/R8 optimization rules for Task Planner App

# Room Database optimization rules
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>(...);
}
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.pooling.MulticastLoop

# Kotlin Coroutines rules
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.android.HandlerContext {
    *** init(...);
}

# Keep our Room Entities, DAOs and models to prevent database translation issues
-keep class com.app.tracker.data.local.entity.** { *; }
-keep interface com.app.tracker.data.local.dao.** { *; }
-keep class com.app.tracker.model.** { *; }

# Keep ZXing scanning library to support QR integration
-keep class com.journeyapps.barcodescanner.** { *; }
-keep class com.google.zxing.** { *; }
-dontwarn com.google.zxing.**