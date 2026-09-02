package com.example.najdimajstor.data.repository

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.example.najdimajstor.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException

class GoogleSignInClient(
    private val context: Context
) {
    private val credentialManager = CredentialManager.create(context)

    suspend fun getGoogleIdToken(): GoogleSignInTokenResult {
        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(context.getString(R.string.default_web_client_id))
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                context = context,
                request = request
            )

            val credential = result.credential

            if (
                credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential =
                    GoogleIdTokenCredential.createFrom(credential.data)

                GoogleSignInTokenResult(
                    idToken = googleIdTokenCredential.idToken
                )
            } else {
                GoogleSignInTokenResult(
                    errorMessage = "Google најавата не успеа. Обиди се повторно."
                )
            }
        } catch (exception: NoCredentialException) {
            GoogleSignInTokenResult(
                errorMessage = "Не е пронајден Google профил на уредот."
            )
        } catch (exception: GoogleIdTokenParsingException) {
            GoogleSignInTokenResult(
                errorMessage = "Google профилот не можеше да се прочита. Обиди се повторно."
            )
        } catch (exception: GetCredentialException) {
            GoogleSignInTokenResult(
                errorMessage = "Google најавата беше откажана или не успеа."
            )
        } catch (exception: Exception) {
            GoogleSignInTokenResult(
                errorMessage = exception.message ?: "Google најавата не успеа. Обиди се повторно."
            )
        }
    }
}

data class GoogleSignInTokenResult(
    val idToken: String? = null,
    val errorMessage: String? = null
)