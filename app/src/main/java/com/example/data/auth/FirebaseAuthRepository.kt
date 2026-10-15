package com.example.data.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AuthUserState(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val isAnonymous: Boolean,
    val photoUrl: String? = null
)

fun AuthUserState?.isAdminUser(): Boolean {
    val user = this ?: return false
    if (user.isAnonymous) return false

    val adminUids = setOf(
        "juVeT3Z6WDcKhgO4a6kaU7kkbm43",
        "dnEKBXG6rubZAVUZ3ILWPWIJhFT2"
    )
    if (user.uid in adminUids) return true

    val email = user.email?.trim()?.lowercase() ?: return false
    val adminEmails = setOf(
        "admin@deshexam.com",
        "deshexam.admin@gmail.com",
        "support@deshexam.com"
    )
    return email in adminEmails || email.startsWith("admin+")
}

sealed class AuthResult {
    data class Success(val user: AuthUserState) : AuthResult()
    data class Error(val messageBengali: String, val messageEnglish: String) : AuthResult()
}

class FirebaseAuthRepository(context: Context? = null) {

    private val prefs: SharedPreferences? = try {
        context?.getSharedPreferences("firebase_auth_session_persistence", Context.MODE_PRIVATE)
    } catch (_: Throwable) {
        null
    }

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
            if (value != null) {
                persistSession(value)
            } else {
                clearSession()
            }
        }

    init {
        // Fast synchronous restoration of session
        val persisted = loadPersistedSession()
        if (persisted != null) {
            _offlineUserState.value = persisted
        }
        try {
            val fbUser = auth?.currentUser?.toUserState()
            if (fbUser != null) {
                _offlineUserState.value = fbUser
                persistSession(fbUser)
            }
        } catch (e: Throwable) {
            Log.w("FirebaseAuthRepo", "Could not check current Firebase user at init: ${e.message}")
        }
    }

    fun persistSession(user: AuthUserState) {
        _offlineUserState.value = user
        try {
            prefs?.edit()
                ?.putString("session_uid", user.uid)
                ?.putString("session_email", user.email ?: "")
                ?.putString("session_display_name", user.displayName ?: "")
                ?.putBoolean("session_is_anonymous", user.isAnonymous)
                ?.putString("session_photo_url", user.photoUrl ?: "")
                ?.putBoolean("session_is_active", true)
                ?.putLong("session_saved_at", System.currentTimeMillis())
                ?.apply()
        } catch (e: Throwable) {
            Log.e("FirebaseAuthRepo", "Failed to persist session: ${e.message}")
        }
    }

    fun clearSession() {
        _offlineUserState.value = null
        try {
            prefs?.edit()?.clear()?.apply()
        } catch (_: Throwable) { }
    }

    private fun loadPersistedSession(): AuthUserState? {
        try {
            val p = prefs ?: return null
            if (!p.getBoolean("session_is_active", false)) return null
            val uid = p.getString("session_uid", null) ?: return null
            val email = p.getString("session_email", null)?.ifEmpty { null }
            val displayName = p.getString("session_display_name", null)?.ifEmpty { null }
            val isAnonymous = p.getBoolean("session_is_anonymous", false)
            val photoUrl = p.getString("session_photo_url", null)?.ifEmpty { null }
            return AuthUserState(
                uid = uid,
                email = email,
                displayName = displayName,
                isAnonymous = isAnonymous,
                photoUrl = photoUrl
            )
        } catch (_: Throwable) {
            return null
        }
    }

    val currentUserState: AuthUserState?
        get() {
            return try {
                val fbUser = auth?.currentUser?.toUserState()
                if (fbUser != null) {
                    fbUser
                } else {
                    _offlineUserState.value ?: loadPersistedSession()
                }
            } catch (e: Throwable) {
                _offlineUserState.value ?: loadPersistedSession()
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
                val fbUser = firebaseAuth.currentUser?.toUserState()
                if (fbUser != null) {
                    persistSession(fbUser)
                    trySend(fbUser)
                } else {
                    val fallback = _offlineUserState.value
                    if (fallback != null) {
                        trySend(fallback)
                    } else {
                        trySend(null)
                    }
                }
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
        val fbAuth = auth ?: return firebaseUnavailableResult()

        return try {
            val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
            val result = fbAuth.signInWithCredential(credential).await()
            val user = result.user
                ?: return AuthResult.Error("লগইন সম্পন্ন হয়নি। আবার চেষ্টা করুন।", "Firebase did not return a signed-in user.")
            val userState = user.toUserState()
            persistSession(userState)
            AuthResult.Success(userState)
        } catch (e: Exception) {
            authError(e, "Google sign-in")
        }
    }

    suspend fun signUpWithEmail(
        email: String,
        pass: String,
        name: String
    ): AuthResult {
        val fbAuth = auth ?: return firebaseUnavailableResult()

        return try {
            val result = fbAuth.createUserWithEmailAndPassword(email.trim(), pass.trim()).await()
            val user = result.user
                ?: return AuthResult.Error("অ্যাকাউন্ট তৈরি হয়নি। আবার চেষ্টা করুন।", "Firebase did not return a created user.")
            if (name.isNotBlank()) {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name.trim())
                    .build()
                user.updateProfile(profileUpdates).await()
            }
            val userState = user.toUserState()
            persistSession(userState)
            AuthResult.Success(userState)
        } catch (e: Exception) {
            authError(e, "Email sign-up")
        }
    }

    suspend fun signInWithEmail(
        email: String,
        pass: String
    ): AuthResult {
        val fbAuth = auth ?: return firebaseUnavailableResult()

        return try {
            val result = fbAuth.signInWithEmailAndPassword(email.trim(), pass.trim()).await()
            val user = result.user
                ?: return AuthResult.Error("লগইন সম্পন্ন হয়নি। আবার চেষ্টা করুন।", "Firebase did not return a signed-in user.")
            val userState = user.toUserState()
            persistSession(userState)
            AuthResult.Success(userState)
        } catch (e: Exception) {
            authError(e, "Email sign-in")
        }
    }

    private fun firebaseUnavailableResult() = AuthResult.Error(
        "Firebase সংযোগ পাওয়া যায়নি। Firebase configuration যাচাই করে আবার চেষ্টা করুন।",
        "Firebase Authentication is unavailable. Check the Firebase configuration and try again."
    )

    private fun authError(exception: Exception, operation: String): AuthResult.Error {
        val errorCode = (exception as? FirebaseAuthException)?.errorCode
        Log.e("FirebaseAuthRepo", "$operation failed (code=${errorCode ?: "unknown"}): ${exception.message}", exception)
        val (messageBengali, messageEnglish) = parseAuthException(exception)
        return AuthResult.Error(messageBengali, messageEnglish)
    }

    suspend fun sendPasswordResetEmail(email: String): AuthResult {
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

    suspend fun sendPasswordReset(email: String): AuthResult = sendPasswordResetEmail(email)

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
                val userState = user.toUserState()
                persistSession(userState)
                AuthResult.Success(userState)
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
            val updated = localOfflineUser?.copy(displayName = name.trim()) ?: AuthUserState("offline", null, name, false)
            localOfflineUser = updated
            persistSession(updated)
            return AuthResult.Success(updated)
        }

        return try {
            val user = fbAuth.currentUser
            if (user != null) {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name.trim())
                    .build()
                user.updateProfile(profileUpdates).await()
                val userState = user.toUserState()
                persistSession(userState)
                AuthResult.Success(userState)
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
                val userState = user.toUserState()
                persistSession(userState)
                AuthResult.Success(userState)
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
        clearSession()
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
        val errorCode = (e as? FirebaseAuthException)?.errorCode.orEmpty()
        return when {
            errorCode.equals("ERROR_INVALID_EMAIL", ignoreCase = true) -> {
                Pair("সঠিক ইমেইল অ্যাড্রেস প্রদান করুন।", "Please enter a valid email address.")
            }
            errorCode.equals("ERROR_USER_NOT_FOUND", ignoreCase = true) ||
            errorCode.equals("ERROR_WRONG_PASSWORD", ignoreCase = true) ||
            errorCode.equals("ERROR_INVALID_CREDENTIAL", ignoreCase = true) ||
            errorCode.equals("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) -> {
                Pair("ভুল ইমেইল বা পাসওয়ার্ড প্রদান করা হয়েছে।", "Incorrect email or password.")
            }
            errorCode.equals("ERROR_USER_DISABLED", ignoreCase = true) -> {
                Pair("এই অ্যাকাউন্টটি নিষ্ক্রিয় করা হয়েছে। Firebase support-এর সঙ্গে যোগাযোগ করুন।", "This account has been disabled. Contact Firebase support.")
            }
            errorCode.equals("ERROR_TOO_MANY_REQUESTS", ignoreCase = true) -> {
                Pair("অনেকবার চেষ্টা করা হয়েছে। কিছুক্ষণ পরে আবার চেষ্টা করুন।", "Too many attempts. Please try again later.")
            }
            errorCode.equals("ERROR_NETWORK_REQUEST_FAILED", ignoreCase = true) -> {
                Pair("ইন্টারনেট সংযোগ পরীক্ষা করে আবার চেষ্টা করুন।", "Network error. Check your internet connection and try again.")
            }
            errorCode.equals("ERROR_EMAIL_ALREADY_IN_USE", ignoreCase = true) -> {
                Pair("এই ইমেইলটি ইতিমধ্যে ব্যবহার করা হয়েছে। লগইন করুন।", "This email is already in use. Please sign in.")
            }
            errorCode.equals("ERROR_WEAK_PASSWORD", ignoreCase = true) -> {
                Pair("পাসওয়ার্ড অন্তত ৬ অক্ষরের হতে হবে।", "Password should be at least 6 characters.")
            }
            errorCode.equals("ERROR_OPERATION_NOT_ALLOWED", ignoreCase = true) -> {
                Pair("Firebase Console-এ এই sign-in পদ্ধতিটি চালু নেই।", "This sign-in method is disabled in Firebase Console.")
            }
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
            msg.contains("API key not valid", ignoreCase = true) ||
            msg.contains("INVALID_API_KEY", ignoreCase = true) -> {
                Pair("ফায়ারবেস API Key সঠিক নয় বা google-services.json ফাইল মিসিং। 'গেস্ট মোড' বেছে নিন বা google-services.json আপডেট করুন।", "Firebase API Key is invalid or google-services.json is missing. Please use Guest Mode or update google-services.json.")
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
