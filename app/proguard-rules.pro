# dadb uses okio and plain JCA; keep its public API intact.
-keep class dadb.** { *; }
-dontwarn dadb.**
-dontwarn okio.**
-dontwarn javax.annotation.**
