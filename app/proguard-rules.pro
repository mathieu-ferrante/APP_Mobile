# ── Modeles serialises avec Gson dans les SharedPreferences ──────────────────
# Sans ces regles, R8 renomme les champs en release et les comptes enregistres
# ainsi que le programme du coach deviennent illisibles apres mise a jour.
-keep class com.phoenix.fitpro.domain.model.** { *; }
-keep class com.phoenix.fitpro.domain.repository.AuthAccount { *; }
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**

# Gson
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# ── kotlinx.serialization (Supabase) ─────────────────────────────────────────
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault
-keep,includedescriptorclasses class com.phoenix.fitpro.**$$serializer { *; }
-keepclassmembers class com.phoenix.fitpro.** {
    *** Companion;
}
-keepclasseswithmembers class com.phoenix.fitpro.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ── Ktor / OkHttp ────────────────────────────────────────────────────────────
-dontwarn org.slf4j.**
-dontwarn io.ktor.**
-dontwarn okhttp3.**
-dontwarn okio.**
-keepclassmembers class io.ktor.** { volatile <fields>; }

# ── Retrofit ─────────────────────────────────────────────────────────────────
-dontwarn retrofit2.**
-keepattributes Exceptions
