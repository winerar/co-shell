# Project-specific R8 rules. Add keep rules here when a library requires them.

# Credential Manager (Google sign-in): the Play Services provider is loaded by reflection.
-if class androidx.credentials.CredentialManager
-keep class androidx.credentials.playservices.** { *; }
