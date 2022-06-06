-keep class io.reactivex.rxjava3.** { *; }

-keepclassmembers class rx.internal.util.unsafe.** {
    long producerIndex;
    long consumerIndex;
}

-keepclassmembers class com.lyrebirdstudio.croppylib.utils.model.AnimatableRectF.** { *; }