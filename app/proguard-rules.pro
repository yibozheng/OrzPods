# OzPods ProGuard Rules
# Keep AirPods model enum
-keep class com.ozpods.data.model.AirPodsModel { *; }
# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
