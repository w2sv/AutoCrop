-keep class io.reactivex.rxjava3.** { *; }

-keepclassmembers class rx.internal.util.unsafe.** {
    long producerIndex;
    long consumerIndex;
}