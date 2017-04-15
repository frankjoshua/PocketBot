#Millenial Media
-keepclassmembers class com.millennialmedia** {
public *;
}
-keep class com.millennialmedia**
-dontwarn com.millennialmedia**
#InMobi
-keep class com.inmobi.** { *; }
-dontwarn com.inmobi.**
#Crashlytics
-keep class com.crashlytics.** { *; }
-keep class com.crashlytics.android.**
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*
-keep public class * extends java.lang.Exception
#Solitaire
-keepattributes InnerClasses
-keep class com.tesseractmobile**
-dontwarn org.apache.http.**
-dontwarn android.net.http.AndroidHttpClient
-dontwarn com.google.android.gms.**
-dontwarn com.android.volley.toolbox.**
-dontwarn android.test.*
-keepattributes EnclosingMethod
-dontwarn com.squareup.javawriter.JavaWriter
-dontwarn sun.misc.Unsafe
-dontwarn org.hamcrest.**
-dontwarn junit.**
#Firebase
-keep class com.firebase.** { *; }
-keep class org.apache.** { *; }
-keepnames class com.fasterxml.jackson.** { *; }
-keepnames class javax.servlet.** { *; }
-keepnames class org.ietf.jgss.** { *; }
-dontwarn org.w3c.dom.**
-dontwarn org.joda.time.**
-dontwarn org.shaded.apache.**
-dontwarn org.ietf.jgss.**
##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-keep class sun.misc.Unsafe { *; }
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.quickblox.core.account.model.** { *; }

##---------------End: proguard configuration for Gson  ----------

##---------------Begin: proguard configuration for quickblox  ----------

#quickblox core module
-keep class com.quickblox.auth.parsers.** { *; }
-keep class com.quickblox.auth.model.** { *; }
-keep class com.quickblox.core.parser.** { *; }
-keep class com.quickblox.core.model.** { *; }
-keep class com.quickblox.core.server.** { *; }
-keep class com.quickblox.core.rest.** { *; }
-keep class com.quickblox.core.error.** { *; }
-keep class com.quickblox.core.Query { *; }

#quickblox users module
-keep class com.quickblox.users.parsers.** { *; }
-keep class com.quickblox.users.model.** { *; }

#quickblox messages module
-keep class com.quickblox.messages.parsers.** { *; }
-keep class com.quickblox.messages.model.** { *; }

#quickblox content module
-keep class com.quickblox.content.parsers.** { *; }
-keep class com.quickblox.content.model.** { *; }

#quickblox chat module
-keep class com.quickblox.chat.parser.** { *; }
-keep class com.quickblox.chat.model.** { *; }
-keep class org.jivesoftware.** { *; }
-dontwarn org.jivesoftware.smackx.**

#quickblox videochat-webrtc module
-keep class org.webrtc.** { *; }

##---------------End: proguard configuration for quickblox  ----------
#Catch all - remove when debugging
-dontwarn **