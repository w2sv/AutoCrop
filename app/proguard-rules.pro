-keepclasseswithmembers class * {
    * inflate(...);
}
-dontwarn com.google.j2objc.annotations.RetainedWith

# keep model classes, which are for some reason not automatically kept
-keep class com.w2sv.domain.model.** { *; }
