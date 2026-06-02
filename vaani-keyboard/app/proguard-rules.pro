# ONNX Runtime
-keep class ai.onnxruntime.** { *; }
-dontwarn ai.onnxruntime.**

# SentencePiece tokenizer (if bundled as .so or jar)
-keep class com.google.sentencepiece.** { *; }
-dontwarn com.google.sentencepiece.**

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Keep model paths from being obfuscated
-keepclassmembers class com.vaani.keyboard.** {
    java.lang.String modelPath;
}
