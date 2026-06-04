# ── Stack traces legibles en producción ──────────────────────────────────────
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ── Kotlin ────────────────────────────────────────────────────────────────────
-keepclassmembers class **$WhenMappings { <fields>; }
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Lazy { *; }

# ── Data classes usadas como modelos de Firebase (no ofuscar campos) ──────────
-keep class com.example.edutrack.dataclass.** { *; }

# ── Firebase Auth ─────────────────────────────────────────────────────────────
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.firebase.auth.** { *; }
-keep class com.google.android.gms.auth.** { *; }

# ── Firebase Realtime Database ────────────────────────────────────────────────
-keep class com.google.firebase.database.** { *; }
-keepclassmembers class * {
    @com.google.firebase.database.PropertyName <fields>;
    @com.google.firebase.database.PropertyName <methods>;
}
# Evitar que R8 elimine getters/setters de clases deserializadas por Firebase
-keepclassmembers class com.example.edutrack.** {
    public <init>();
    public <fields>;
    public <methods>;
}

# ── Firebase Storage ──────────────────────────────────────────────────────────
-keep class com.google.firebase.storage.** { *; }

# ── Google Play Billing ───────────────────────────────────────────────────────
-keep class com.android.billingclient.** { *; }
-keepclassmembers class com.android.billingclient.** { *; }

# ── Google Credential Manager / Sign-In ──────────────────────────────────────
-keep class com.google.android.libraries.identity.googleid.** { *; }
-keep class androidx.credentials.** { *; }
-keepclassmembers class androidx.credentials.** { *; }

# ── ZXing (QR codes) ─────────────────────────────────────────────────────────
-keep class com.google.zxing.** { *; }

# ── Coil ──────────────────────────────────────────────────────────────────────
-keep class coil.** { *; }
-keepclassmembers class coil.** { *; }

# ── DataStore ─────────────────────────────────────────────────────────────────
-keep class androidx.datastore.** { *; }

# ── AdMob ─────────────────────────────────────────────────────────────────────
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.ads.** { *; }

# ── Jetpack Compose (salvaguardas extra sobre el plugin) ──────────────────────
-keep class androidx.compose.** { *; }
-keepclassmembers class androidx.compose.** { *; }

# ── Enums (necesario para when-expressions) ───────────────────────────────────
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ── Serializable ─────────────────────────────────────────────────────────────
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
