# ProGuard rules for Shield Security

# Keep Supabase classes
-keep class io.github.** { *; }
-keep class com.google.gson.** { *; }

# Keep Android framework classes
-keep public class android.** { *; }

# Keep our application classes
-keep class com.security.shield.** { *; }

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep custom application classes
-keep public class * extends android.app.Activity
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.app.Service