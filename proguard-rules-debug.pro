#Turn off obfuscation in debug mode
-dontobfuscate
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*,!code/allocation/variable

-keepclassmembers public class com.tesseractmobile.** {
    *;
}
-keepclassmembernames public class com.tesseractmobile.** {
    *;
}
