package com.example.najdimajstor.data.repository

import com.example.najdimajstor.data.model.UserRole
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun registerUser(
        fullName: String,
        email: String,
        phone: String,
        password: String,
        role: UserRole,
        onResult: (Boolean, String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val userId = result.user?.uid

                if (userId == null) {
                    onResult(false, "Неуспешна регистрација. Обиди се повторно.")
                    return@addOnSuccessListener
                }

                val userProfile = hashMapOf(
                    "uid" to userId,
                    "fullName" to fullName,
                    "email" to email,
                    "phone" to phone,
                    "role" to role.name,
                    "createdAt" to FieldValue.serverTimestamp()
                )

                firestore.collection("users")
                    .document(userId)
                    .set(userProfile)
                    .addOnSuccessListener {
                        onResult(true, null)
                    }
                    .addOnFailureListener {
                        onResult(
                            false,
                            "Профилот е креиран, но информациите не беа зачувани. Обиди се повторно."
                        )
                    }
            }
            .addOnFailureListener { exception ->
                onResult(false, getAuthErrorMessage(exception))
            }
    }

    fun loginUser(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener {
                onResult(false, "Грешна е-пошта или лозинка.")
            }
    }

    fun logout() {
        auth.signOut()
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    private fun getAuthErrorMessage(exception: Exception): String {
        return when (exception) {
            is FirebaseAuthWeakPasswordException ->
                "Лозинката е премногу слаба. Внеси најмалку 6 знаци."

            is FirebaseAuthUserCollisionException ->
                "Веќе постои профил со оваа е-пошта."

            is FirebaseAuthInvalidCredentialsException ->
                "Внеси валидна е-пошта."

            is FirebaseNetworkException ->
                "Нема интернет-конекција. Провери ја мрежата и обиди се повторно."

            is FirebaseTooManyRequestsException ->
                "Премногу обиди. Почекај малку и обиди се повторно."

            else ->
                "Регистрацијата не успеа. Обиди се повторно."
        }
    }
}