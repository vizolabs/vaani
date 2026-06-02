# ONNX Runtime
-keep class ai.onnxruntime.** { *; }
-keep class com.microsoft.onnxruntime.** { *; }
-keep class org.onnxruntime.** { *; }
-dontwarn ai.onnxruntime.**
-dontwarn com.microsoft.onnxruntime.**
-dontwarn org.onnxruntime.**

# SentencePiece tokenizer (if bundled as .so or jar)
-keep class com.google.sentencepiece.** { *; }
-dontwarn com.google.sentencepiece.**

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Keep Vaani model and translate classes
-keep class com.vaani.keyboard.util.ModelLoader { *; }
-keep class com.vaani.keyboard.translate.TranslationPipeline { *; }
