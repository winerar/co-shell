package app.coshell.family.data

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

/** Result of the system "Sign in with Google" sheet. */
sealed interface GoogleSignInResult {
    data class Success(val idToken: String) : GoogleSignInResult
    data object Cancelled : GoogleSignInResult
    data class Failed(val reason: String) : GoogleSignInResult
}

/**
 * Google account picker via Credential Manager. Needs an Activity [context] (it shows UI).
 * [webClientId] is the OAuth "Web client" of the Firebase project — the ID token is issued for
 * it, and Firebase Auth accepts it (docs/setup/sync.md).
 */
suspend fun signInWithGoogle(context: Context, webClientId: String): GoogleSignInResult {
    val request = GetCredentialRequest.Builder()
        .addCredentialOption(GetSignInWithGoogleOption.Builder(webClientId).build())
        .build()
    return try {
        val credential = CredentialManager.create(context).getCredential(context, request).credential
        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            GoogleSignInResult.Success(GoogleIdTokenCredential.createFrom(credential.data).idToken)
        } else {
            GoogleSignInResult.Failed("Unexpected credential ${credential.type}")
        }
    } catch (e: GetCredentialCancellationException) {
        GoogleSignInResult.Cancelled
    } catch (e: GetCredentialException) {
        GoogleSignInResult.Failed(e.message ?: e.type)
    }
}
