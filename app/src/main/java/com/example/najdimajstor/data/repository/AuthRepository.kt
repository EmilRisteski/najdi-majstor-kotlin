package com.example.najdimajstor.data.repository

import com.example.najdimajstor.data.model.UserRole
import com.google.firebase.auth.FirebaseAuth
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
                    onResult(false, "Неуспешна регистрација. Обидете се повторно.")
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
                    .addOnFailureListener { exception ->
                        onResult(false, exception.message ?: "Профилот не беше зачуван.")
                    }
            }
            .addOnFailureListener { exception ->
                onResult(false, exception.message ?: "Регистрацијата не успеа.")
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
}