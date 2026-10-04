package com.example.data.auth

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AuthUserState(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val isAnonymous: Boolean,
    val photoUrl: String? = null
)

sealed class AuthResult {
    data class Success(val user: AuthUserState) : AuthResult()
    data class Error(val messageBengali: String, val messageEnglish: String) : AuthResult()
}

class FirebaseAuthRepository {

    private val auth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Throwable) {
            Log.w("FirebaseAuthRepo", "FirebaseAuth not available, using offline auth mode: ${e.message}")
            null
        }
    }

    private val _offlineUserState = MutableStateFlow<AuthUserState?>(null)

    private var localOfflineUser: AuthUserState?
        get() = _offlineUserState.value
        set(value) {
            _offlineUserState.value = value
        }

    val currentUserState: AuthUserState?
        get() {
            return try {
                auth?.currentUser?.toUserState() ?: _offlineUserState.value
            } catch (e: Throwable) {
                _offlineUserState.value
            }
        }

    val authStateFlow: Flow<AuthUserState?> = callbackFlow {
        val fbAuth = auth
        if (fbAuth == null) {
            val job = CoroutineScope(Dispatchers.Default).launch {
                _offlineUserState.collect { user ->
                    trySend(user)
                }
            }
            awaitClose { job.cancel() }
            return@callbackFlow
        }

        try {
            val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                trySend(firebaseAuth.currentUser?.toUserState() ?: _offlineUserState.value)
            }
            fbAuth.addAuthStateListener(listener)
            val job = CoroutineScope(Dispatchers.Default).launch {
                _offlineUserState.collect { offlineUser ->
                    if (fbAuth.currentUser == null) {
                        trySend(offlineUser)
                    }
                }
            }
            awaitClose {
                try {
                    fbAuth.removeAuthStateListener(listener)
                } catch (_: Throwable) { }
                job.cancel()
            }
        } catch (e: Throwable) {
            val job = CoroutineScope(Dispatchers.Default).launch {
                _offlineUserState.collect { user ->
                    trySend(user)
                }
            }
            awaitClose { job.cancel() }
        }
    }

    suspend fun signInWithGoogleCredential(idToken: String, displayName: String?, email: String?): AuthResult {
        val fbAuth = auth
        if (fbAuth == null) {
            val offlineUser = AuthUserState(
                uid = "google_${System.currentTimeMillis()}",
                email = email ?: "user@gmail.com",
                displayName = displayName ?: "Google User",
                isAnonymous = false
            )
            localOfflineUser = offlineUser
            return AuthResult.Success(offlineUser)
        }

        return try {
            val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
            val result = fbAuth.signInWithCredential(credential).await()
            val user = result.user
            if (user != null) {
                AuthResult.Success(user.toUserState())
            } else {
                AuthResult.Error(
                    messageBengali = "গুগল সাইন-ইন ব্যর্থ হয়েছে।",
                    messageEnglish = "Google sign-in failed."
                )
            }
        } catch (e: Exception) {
            val (msgBn, msgEn) = parseAuthException(e)
            AuthResult.Error(messageBengali = msgBn, messageEnglish = msgEn)
        }
    }

    suspend fun signUpWithEmail(
        email: String,
        pass: String,
        name: String
    ): AuthResult {
        val fbAuth = auth
        if (fbAuth == null) {
            val offlineUser = AuthUserState(
                uid = "offline_${System.currentTimeMillis()}",
                email = email.trim(),
                displayName = name.trim().ifEmpty { "Madhyamik Aspirant" },
                isAnonymous = false
            )
            localOfflineUser = offlineUser
            return AuthResult.Success(offlineUser)
        }

        return try {
            val result = fbAuth.createUserWithEmailAndPassword(email.trim(), pass.trim()).await()
            val user = result.user
            if (user != null && name.isNotBlank()) {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name.trim())
                    .build()
                user.updateProfile(profileUpdates).await()
            }
            if (user != null) {
                AuthResult.Success(user.toUserState())
            } else {
                AuthResult.Error(
                    messageBengali = "সাইন আপ করতে সমস্যা হয়েছে। আবার চেষ্টা করুন।",
                    messageEnglish = "Sign up failed. Please try again."
                )
            }
        } catch (e: Exception) {
            val (msgBn, msgEn) = parseAuthException(e)
            AuthResult.Error(messageBengali = msgBn, messageEnglish = msgEn)
        }
    }

    suspend fun signInWithEmail(
        email: String,
        pass: String
    ): AuthResult {
        val fbAuth = auth
        if (fbAuth == null) {
            val offlineUser = AuthUserState(
                uid = "offline_${System.currentTimeMillis()}",
                email = email.trim(),
                displayName = email.substringBefore("@").replaceFirstChar { it.uppercase() },
                isAnonymous = false
            )
            localOfflineUser = offlineUser
            return AuthResult.Success(offlineUser)
        }

        return try {
            val result = fbAuth.signInWithEmailAndPassword(email.trim(), pass.trim()).await()
            val user = result.user
            if (user != null) {
                AuthResult.Success(user.toUserState())
            } else {
                AuthResult.Error(
                    messageBengali = "লগইন করতে সমস্যা হয়েছে। আবার চেষ্টা করুন।",
                    messageEnglish = "Login failed. Please try again."
                )
            }
        } catch (e: Exception) {
            val (msgBn, msgEn) = parseAuthException(e)
            AuthResult.Error(messageBengali = msgBn, messageEnglish = msgEn)
        }
    }

    suspend fun sendPasswordReset(email: String): AuthResult {
        val fbAuth = auth
        if (fbAuth == null) {
            return AuthResult.Success(
                AuthUserState(
                    uid = "",
                    email = email,
                    displayName = null,
                    isAnonymous = false
                )
            )
        }

        return try {
            fbAuth.sendPasswordResetEmail(email.trim()).await()
            AuthResult.Success(
                AuthUserState(
                    uid = "",
                    email = email,
                    displayName = null,
                    isAnonymous = false
                )
            )
        } catch (e: Exception) {
            val (msgBn, msgEn) = parseAuthException(e)
            AuthResult.Error(messageBengali = msgBn, messageEnglish = msgEn)
        }
    }

    suspend fun signInAnonymously(): AuthResult {
        val fbAuth = auth
        if (fbAuth == null) {
            val offlineUser = AuthUserState(
                uid = "guest_${System.currentTimeMillis()}",
                email = null,
                displayName = "Guest Student / অতিথি পরীক্ষার্থী",
                isAnonymous = true
            )
            localOfflineUser = offlineUser
            return AuthResult.Success(offlineUser)
        }

        return try {
            val result = fbAuth.signInAnonymously().await()
            val user = result.user
            if (user != null) {
                AuthResult.Success(user.toUserState())
            } else {
                AuthResult.Error(
                    messageBengali = "অতিথি হিসেবে প্রবেশ করতে ব্যর্থ হয়েছে।",
                    messageEnglish = "Failed to continue as guest."
                )
            }
        } catch (e: Exception) {
            val (msgBn, msgEn) = parseAuthException(e)
            AuthResult.Error(messageBengali = msgBn, messageEnglish = msgEn)
        }
    }

    suspend fun updateDisplayName(name: String): AuthResult {
        val fbAuth = auth
        if (fbAuth == null) {
            localOfflineUser = localOfflineUser?.copy(displayName = name.trim())
            return AuthResult.Success(localOfflineUser ?: AuthUserState("offline", null, name, false))
        }

        return try {
            val user = fbAuth.currentUser
            if (user != null) {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name.trim())
                    .build()
                user.updateProfile(profileUpdates).await()
                AuthResult.Success(user.toUserState())
            } else {
                AuthResult.Error("ব্যবহারকারী লগইন করা নেই।", "No authenticated user.")
            }
        } catch (e: Exception) {
            val (msgBn, msgEn) = parseAuthException(e)
            AuthResult.Error(messageBengali = msgBn, messageEnglish = msgEn)
        }
    }

    suspend fun updatePassword(newPass: String): AuthResult {
        val fbAuth = auth
        if (fbAuth == null) {
            return AuthResult.Success(localOfflineUser ?: AuthUserState("offline", null, null, false))
        }

        return try {
            val user = fbAuth.currentUser
            if (user != null) {
                user.updatePassword(newPass.trim()).await()
                AuthResult.Success(user.toUserState())
            } else {
                AuthResult.Error("ব্যবহারকারী লগইন করা নেই।", "No authenticated user.")
            }
        } catch (e: Exception) {
            val (msgBn, msgEn) = parseAuthException(e)
            AuthResult.Error(messageBengali = msgBn, messageEnglish = msgEn)
        }
    }

    fun signOut() {
        try {
            auth?.signOut()
        } catch (_: Throwable) { }
        localOfflineUser = null
    }

    private fun FirebaseUser.toUserState(): AuthUserState {
        return AuthUserState(
            uid = uid,
            email = email,
            displayName = displayName,
            isAnonymous = isAnonymous,
            photoUrl = photoUrl?.toString()
        )
    }

    private fun parseAuthException(e: Exception): Pair<String, String> {
        val msg = e.message ?: ""
        return when {
            msg.contains("The email address is already in use", ignoreCase = true) ||
            msg.contains("ERROR_EMAIL_ALREADY_IN_USE", ignoreCase = true) -> {
                Pair("এই ইমেইলটি ইতিমধ্যে ব্যবহার করা হয়েছে। লগইন করুন।", "This email is already in use. Please sign in.")
            }
            msg.contains("The email address is badly formatted", ignoreCase = true) ||
            msg.contains("ERROR_INVALID_EMAIL", ignoreCase = true) -> {
                Pair("সঠিক ইমেইল অ্যাড্রেস প্রদান করুন।", "Please enter a valid email address.")
            }
            msg.contains("password is invalid", ignoreCase = true) ||
            msg.contains("ERROR_WRONG_PASSWORD", ignoreCase = true) ||
            msg.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) ||
            msg.contains("user-not-found", ignoreCase = true) -> {
                Pair("ভুল ইমেইল বা পাসওয়ার্ড প্রদান করা হয়েছে।", "Incorrect email or password.")
            }
            msg.contains("weak password", ignoreCase = true) ||
            msg.contains("WEAK_PASSWORD", ignoreCase = true) -> {
                Pair("পাসওয়ার্ড অন্তত ৬ অক্ষরের হতে হবে।", "Password should be at least 6 characters.")
            }
            msg.contains("network error", ignoreCase = true) ||
            msg.contains("NETWORK_ERROR", ignoreCase = true) -> {
                Pair("ইন্টারনেট সংযোগ পরীক্ষা করুন।", "Network error. Please check your internet connection.")
            }
            else -> {
                Pair(
                    "একটি ত্রুটি ঘটেছে: ${e.localizedMessage ?: "আবার চেষ্টা করুন"}",
                    "An error occurred: ${e.localizedMessage ?: "Please try again."}"
                )
            }
        }
    }
}
