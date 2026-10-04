package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import android.widget.Toast
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch
import com.example.R
import com.example.ui.theme.*

enum class AuthMode {
    SIGN_IN,
    SIGN_UP,
    FORGOT_PASSWORD
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    isBengali: Boolean,
    isLoading: Boolean,
    errorMessage: String?,
    successMessage: String?,
    districts: List<String> = com.example.util.WestBengalData.districts,
    districtSchoolsMap: Map<String, List<String>> = com.example.util.WestBengalData.districtSchoolsMap,
    isLoadingDistricts: Boolean = false,
    onSignIn: (email: String, pass: String) -> Unit,
    onSignUp: (name: String, school: String, district: String, targetExam: String, email: String, pass: String) -> Unit,
    onForgotPassword: (email: String) -> Unit,
    onContinueAsGuest: () -> Unit,
    onClearError: () -> Unit,
    onGoogleSignIn: (idToken: String, displayName: String?, email: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var authMode by remember { mutableStateOf(AuthMode.SIGN_IN) }
    var localError by remember { mutableStateOf<String?>(null) }
    var showSha1Dialog by remember { mutableStateOf(false) }
    var showNoAccountDialog by remember { mutableStateOf(false) }

    // Form inputs
    var name by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("কলকাতা") }
    var school by remember { mutableStateOf("Kolkata Model High School") }
    var targetExam by remember { mutableStateOf("WBBSE Madhyamik 2026") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    var districtExpanded by remember { mutableStateOf(false) }
    var schoolExpanded by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val credentialManager = CredentialManager.create(context)

    // Deep immersive Space/Dark Theme inspired by screenshot
    val spaceDarkBg = Brush.verticalGradient(
        listOf(
            Color(0xFF030712),
            Color(0xFF070B19),
            Color(0xFF0B1120),
            Color(0xFF02040A)
        )
    )

    // Glassmorphic translucent card design parameters
    val glassCardBgColor = Color(0xFF1E293B).copy(alpha = 0.35f)
    val glassCardBorderColor = Color.White.copy(alpha = 0.12f)
    val inputBgColor = Color(0xFF0F172A).copy(alpha = 0.3f)
    val inputBorderColor = Color.White.copy(alpha = 0.12f)

    // Text Field Color styling to achieve the sleek translucent inputs
    val modernTextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = inputBgColor,
        unfocusedContainerColor = inputBgColor,
        focusedBorderColor = Color(0xFFA78BFA), // Pastel Violet Focus border
        unfocusedBorderColor = inputBorderColor,
        focusedLabelColor = Color(0xFFA78BFA),
        unfocusedLabelColor = Color(0xFF94A3B8),
        focusedLeadingIconColor = Color(0xFFA78BFA),
        unfocusedLeadingIconColor = Color(0xFF94A3B8),
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        cursorColor = Color(0xFFA78BFA)
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(spaceDarkBg)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        // Glowing visual spheres in corners matching upload
        Box(
            modifier = Modifier
                .size(340.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-120).dp, y = 120.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            Color(0xFF7C3AED).copy(alpha = 0.18f),
                            Color.Transparent
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .size(320.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 100.dp, y = 100.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            Color(0xFF3B82F6).copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Fully scrollable container to prevent any keyboard truncation
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            
            // 1. App Logo Badge
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF9333EA), // Purple
                                Color(0xFF3B82F6)  // Indigo Blue
                            )
                        )
                    )
                    .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = "Logo Cap",
                    tint = Color.White,
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Form Column directly on layout (No outer Card)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_form_container"),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Segmented Toggle control with Gradient Active Tab
                if (authMode != AuthMode.FORGOT_PASSWORD) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B).copy(alpha = 0.5f))
                            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)), CircleShape)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val isSignIn = authMode == AuthMode.SIGN_IN
                        val isSignUp = authMode == AuthMode.SIGN_UP

                        // Sign In tab item
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(CircleShape)
                                .then(
                                    if (isSignIn) Modifier.background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                Color(0xFF9333EA), // Purple
                                                Color(0xFF3B82F6)  // Blue
                                            )
                                        ),
                                        shape = CircleShape
                                    ) else Modifier
                                )
                                .clickable {
                                    authMode = AuthMode.SIGN_IN
                                    onClearError()
                                }
                                .testTag("tab_signin"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "সাইন ইন",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSignIn) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 14.sp
                                ),
                                color = if (isSignIn) Color.White else Color(0xFF94A3B8)
                            )
                        }

                        // Sign Up tab item
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(CircleShape)
                                .then(
                                    if (isSignUp) Modifier.background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                Color(0xFF9333EA), // Purple
                                                Color(0xFF3B82F6)  // Blue
                                            )
                                        ),
                                        shape = CircleShape
                                    ) else Modifier
                                )
                                .clickable {
                                    authMode = AuthMode.SIGN_UP
                                    onClearError()
                                }
                                .testTag("tab_signup"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "নতুন অ্যাকাউন্ট",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSignUp) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 14.sp
                                ),
                                color = if (isSignUp) Color.White else Color(0xFF94A3B8)
                            )
                        }
                    }
                } else {
                            // Back Navigation Link for Forgot Password mode
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                            ) {
                                IconButton(
                                    onClick = {
                                        authMode = AuthMode.SIGN_IN
                                        onClearError()
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "লগইন স্ক্রিনে ফিরে যান",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                        }

                        // Alerts Support
                        val displayError = errorMessage ?: localError
                        if (!displayError.isNullOrBlank()) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = WrongRed.copy(alpha = 0.08f),
                                border = BorderStroke(0.5.dp, WrongRed.copy(alpha = 0.3f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (displayError.contains("SHA-1") || displayError.contains("Developer console", ignoreCase = true) || displayError.contains("10")) {
                                            showSha1Dialog = true
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ErrorOutline,
                                        contentDescription = "Error",
                                        tint = WrongRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = displayError,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 12.sp,
                                            lineHeight = 15.sp
                                        ),
                                        color = Color(0xFFFCA5A5),
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (displayError.contains("SHA-1") || displayError.contains("Developer console", ignoreCase = true) || displayError.contains("10")) {
                                        Text(
                                            text = "সাহায্য",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                            color = PrimaryIndigoLight,
                                            modifier = Modifier.padding(start = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        if (!successMessage.isNullOrBlank()) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = CorrectGreen.copy(alpha = 0.08f),
                                border = BorderStroke(0.5.dp, CorrectGreen.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Success",
                                        tint = CorrectGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = successMessage,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 12.sp,
                                            lineHeight = 15.sp
                                        ),
                                        color = Color(0xFF6EE7B7),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        // Form input fields
                        AnimatedVisibility(visible = authMode == AuthMode.SIGN_UP) {
                            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                // Full Name Field
                                OutlinedTextField(
                                    value = name,
                                    onValueChange = { name = it },
                                    label = { Text("আপনার নাম", fontSize = 12.sp) },
                                    leadingIcon = {
                                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                                    },
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = modernTextFieldColors,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_full_name")
                                )
                            }
                        }

                        // Email Field matching "আপনার ইমেইল বা ফোন" layout
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            placeholder = { Text("আপনার ইমেইল বা ফোন", fontSize = 13.sp, color = Color(0xFF64748B)) },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(18.dp))
                            },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = if (authMode == AuthMode.FORGOT_PASSWORD) ImeAction.Done else ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) },
                                onDone = {
                                    focusManager.clearFocus()
                                    if (authMode == AuthMode.FORGOT_PASSWORD) onForgotPassword(email)
                                }
                            ),
                            shape = RoundedCornerShape(16.dp),
                            colors = modernTextFieldColors,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_email")
                        )

                        // Password Field matching "পাসওয়ার্ড দিন" layout
                        if (authMode != AuthMode.FORGOT_PASSWORD) {
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                placeholder = { Text("পাসওয়ার্ড দিন", fontSize = 13.sp, color = Color(0xFF64748B)) },
                                leadingIcon = {
                                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                                },
                                trailingIcon = {
                                    IconButton(
                                        onClick = { showPassword = !showPassword },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = if (showPassword) "Hide password" else "Show password",
                                            modifier = Modifier.size(18.dp),
                                            tint = Color(0xFF64748B)
                                        )
                                    }
                                },
                                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = if (authMode == AuthMode.SIGN_UP) ImeAction.Next else ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { focusManager.moveFocus(FocusDirection.Down) },
                                    onDone = {
                                        focusManager.clearFocus()
                                        onSignIn(email, password)
                                    }
                                ),
                                shape = RoundedCornerShape(16.dp),
                                colors = modernTextFieldColors,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_password")
                            )
                            if (authMode == AuthMode.SIGN_UP) {
                                Text(
                                    text = "কমপক্ষে ৮ অক্ষরের পাসওয়ার্ড ব্যবহার করুন",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = Color(0xFF94A3B8),
                                    modifier = Modifier.padding(start = 4.dp, top = 1.dp)
                                )
                            }
                        }

                        // Confirm Password (SignUp Mode)
                        if (authMode == AuthMode.SIGN_UP) {
                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                placeholder = { Text("পাসওয়ার্ড নিশ্চিত করুন", fontSize = 13.sp, color = Color(0xFF64748B)) },
                                leadingIcon = {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                },
                                trailingIcon = {
                                    IconButton(
                                        onClick = { showPassword = !showPassword },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = if (showPassword) "Hide password" else "Show password",
                                            modifier = Modifier.size(18.dp),
                                            tint = Color(0xFF64748B)
                                        )
                                    }
                                },
                                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        focusManager.clearFocus()
                                        if (password == confirmPassword) {
                                            onSignUp(name, school, district, targetExam, email, password)
                                        }
                                    }
                                ),
                                shape = RoundedCornerShape(16.dp),
                                colors = modernTextFieldColors,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_confirm_password")
                            )
                            
                            var termsAccepted by remember { mutableStateOf(false) }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Checkbox(
                                    checked = termsAccepted,
                                    onCheckedChange = { termsAccepted = it },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = Color(0xFFA78BFA),
                                        uncheckedColor = Color(0xFF64748B)
                                    ),
                                    modifier = Modifier.size(20.dp).padding(end = 8.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = androidx.compose.ui.text.buildAnnotatedString {
                                        append("আমি ব্যবহারের ")
                                        withStyle(androidx.compose.ui.text.SpanStyle(color = Color(0xFFA78BFA), textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline)) {
                                            append("শর্তাবলী")
                                        }
                                        append(" ও ")
                                        withStyle(androidx.compose.ui.text.SpanStyle(color = Color(0xFFA78BFA), textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline)) {
                                            append("গোপনীয়তা নীতি")
                                        }
                                        append(" মেনে নিচ্ছি")
                                    },
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = Color(0xFF94A3B8),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // Forgot Password Link exactly matching image
                        if (authMode == AuthMode.SIGN_IN) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Text(
                                    text = "পাসওয়ার্ড ভুলে গেছেন?",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp
                                    ),
                                    color = Color(0xFFA5B4FC), // Lavender / Purple link
                                    modifier = Modifier
                                        .clickable {
                                            authMode = AuthMode.FORGOT_PASSWORD
                                            onClearError()
                                        }
                                        .padding(vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Gradient Action Button matching image
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isLoading) {
                                        Brush.horizontalGradient(listOf(Color(0xFF64748B), Color(0xFF64748B)))
                                    } else {
                                        Brush.horizontalGradient(
                                            listOf(
                                                Color(0xFF9333EA), // Purple
                                                Color(0xFF3B82F6)  // Blue
                                            )
                                        )
                                    }
                                )
                                .clickable(enabled = !isLoading) {
                                    focusManager.clearFocus()
                                    when (authMode) {
                                        AuthMode.SIGN_IN -> onSignIn(email, password)
                                        AuthMode.SIGN_UP -> onSignUp(name, school, district, targetExam, email, password)
                                        AuthMode.FORGOT_PASSWORD -> onForgotPassword(email)
                                    }
                                }
                                .testTag("auth_submit_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
                                ) {
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(
                                        text = when (authMode) {
                                            AuthMode.SIGN_IN -> "সাইন ইন"
                                            AuthMode.SIGN_UP -> "অ্যাকাউন্ট তৈরি করুন"
                                            AuthMode.FORGOT_PASSWORD -> "রিসেট লিংক পাঠান"
                                        },
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            letterSpacing = 0.5.sp
                                        ),
                                        color = Color.White,
                                        modifier = Modifier.padding(start = 18.dp)
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        // Divider matching the "বা" in image
                        if (authMode != AuthMode.FORGOT_PASSWORD) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                HorizontalDivider(
                                    modifier = Modifier.weight(1f),
                                    color = Color.White.copy(alpha = 0.08f)
                                )
                                Text(
                                    text = "বা",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    ),
                                    color = Color(0xFF64748B),
                                    modifier = Modifier.padding(horizontal = 14.dp)
                                )
                                HorizontalDivider(
                                    modifier = Modifier.weight(1f),
                                    color = Color.White.copy(alpha = 0.08f)
                                )
                            }

                            // Google Sign In button matching pure white design
                            Surface(
                                onClick = {
                                    val googleIdOption = GetGoogleIdOption.Builder()
                                        .setFilterByAuthorizedAccounts(false)
                                        .setServerClientId("461242639157-dbbqm1tkbdh82q2djn8r3se8vced5119.apps.googleusercontent.com")
                                        .setAutoSelectEnabled(false)
                                        .build()

                                    val request = GetCredentialRequest.Builder()
                                        .addCredentialOption(googleIdOption)
                                        .build()

                                    scope.launch {
                                        try {
                                            localError = null
                                            val result = credentialManager.getCredential(
                                                request = request,
                                                context = context
                                            )
                                            val credential = result.credential
                                            if (credential is GoogleIdTokenCredential) {
                                                onGoogleSignIn(
                                                    credential.idToken,
                                                    credential.displayName,
                                                    credential.id
                                                )
                                            }
                                        } catch (e: GetCredentialCancellationException) {
                                            localError = null
                                        } catch (e: NoCredentialException) {
                                            showNoAccountDialog = true
                                            localError = if (isBengali) {
                                                "ডিভাইসে কোনো গুগল অ্যাকাউন্ট পাওয়া যায়নি। ফোনে একটি গুগল অ্যাকাউন্ট যোগ করুন অথবা টেস্ট সাইন-ইন ব্যবহার করুন।"
                                            } else {
                                                "No Google account found on this device. Please add a Google account or use Demo Login."
                                            }
                                        } catch (e: GetCredentialException) {
                                            val errorMsg = e.message.orEmpty()
                                            if (errorMsg.contains("No credentials available", ignoreCase = true) || errorMsg.contains("28434")) {
                                                showNoAccountDialog = true
                                                localError = if (isBengali) {
                                                    "ডিভাইসে কোনো গুগল অ্যাকাউন্ট পাওয়া যায়নি। ফোনে গুগল অ্যাকাউন্ট যোগ করুন অথবা টেস্ট সাইন-ইন ব্যবহার করুন।"
                                                } else {
                                                    "No Google account found on this device. Please add a Google account or use Demo Login."
                                                }
                                            } else if (errorMsg.contains("10") || errorMsg.contains("Developer console is not set up correctly", ignoreCase = true)) {
                                                showSha1Dialog = true
                                                localError = if (isBengali) {
                                                    "গুগল সাইন-ইন সেটআপ প্রয়োজন: ফায়ারবেস কনসোলে অ্যাপের SHA-1 ফিঙ্গারপ্রিন্ট যোগ করতে হবে।"
                                                } else {
                                                    "Google Sign-In setup needed: Add app's SHA-1 fingerprint to Firebase Console."
                                                }
                                            } else if (errorMsg.contains("cancel", ignoreCase = true) || errorMsg.contains("16")) {
                                                localError = null
                                            } else {
                                                localError = if (isBengali) {
                                                    "গুগল সাইন-ইন বাতিল বা ব্যর্থ হয়েছে: ${e.message}"
                                                } else {
                                                    "Google Sign-In cancelled or failed: ${e.message}"
                                                }
                                            }
                                        } catch (e: Exception) {
                                            val errorMsg = e.localizedMessage.orEmpty()
                                            if (errorMsg.contains("No credentials available", ignoreCase = true) || errorMsg.contains("28434")) {
                                                showNoAccountDialog = true
                                                localError = if (isBengali) {
                                                    "ডিভাইসে কোনো গুগল অ্যাকাউন্ট পাওয়া যায়নি। ফোনে গুগল অ্যাকাউন্ট যোগ করুন অথবা টেস্ট সাইন-ইন ব্যবহার করুন।"
                                                } else {
                                                    "No Google account found on this device. Please add a Google account or use Demo Login."
                                                }
                                            } else if (errorMsg.contains("10") || errorMsg.contains("Developer console is not set up correctly", ignoreCase = true)) {
                                                showSha1Dialog = true
                                                localError = if (isBengali) {
                                                    "গুগল সাইন-ইন সেটআপ প্রয়োজন: ফায়ারবেস কনসোলে অ্যাপের SHA-1 ফিঙ্গারপ্রিন্ট যোগ করতে হবে।"
                                                } else {
                                                    "Google Sign-In setup needed: Add app's SHA-1 fingerprint to Firebase Console."
                                                }
                                            } else {
                                                localError = if (isBengali) {
                                                    "ত্রুটি: ${e.localizedMessage ?: "Unknown error"}"
                                                } else {
                                                    "Error: ${e.localizedMessage ?: "Unknown error"}"
                                                }
                                            }
                                        }
                                    }
                                },
                                enabled = !isLoading,
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, Color(0xFFDADCE0)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("google_signin_btn")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_google_logo),
                                        contentDescription = "Google Logo",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Google দিয়ে সাইন ইন করুন",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        ),
                                         color = Color(0xFF1F1F1F)
                                    )
                                }
                            }
                        }
                    }
                }

            Spacer(modifier = Modifier.height(16.dp))

            // Compact Centered Guest Mode Button (Not Full Width)
            Surface(
                shape = CircleShape,
                color = Color(0xFF1E1B4B).copy(alpha = 0.6f),
                border = BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(Color(0xFF818CF8), Color(0xFF06B6D4)))),
                modifier = Modifier
                    .wrapContentWidth()
                    .clickable {
                        if (authMode == AuthMode.SIGN_UP) {
                            authMode = AuthMode.SIGN_IN
                        } else {
                            onContinueAsGuest()
                        }
                    }
                    .testTag(if (authMode == AuthMode.SIGN_UP) "go_to_signin_btn" else "continue_as_guest_btn")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (authMode == AuthMode.SIGN_UP) {
                        Text(
                            text = androidx.compose.ui.text.buildAnnotatedString {
                                append("আগেই অ্যাকাউন্ট আছে? ")
                                withStyle(
                                    androidx.compose.ui.text.SpanStyle(
                                        color = Color(0xFF38BDF8),
                                        fontWeight = FontWeight.Bold
                                    )
                                ) {
                                    append("সাইন ইন করুন →")
                                }
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                            color = Color(0xFFE2E8F0)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.horizontalGradient(listOf(Color(0xFF4F46E5), Color(0xFF06B6D4)))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Explore,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(10.dp))
                        
                        Text(
                            text = "গেস্ট মোড",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        
                        Text(
                            text = " — প্রবেশ →",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF38BDF8)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Beautiful interactive sparkling 4-pointed star drawn in SVG Canvas to match bottom-right corner decoration
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Canvas(modifier = Modifier.size(24.dp)) {
                    val path = Path().apply {
                        moveTo(size.width / 2, 0f)
                        quadraticTo(size.width / 2, size.height / 2, size.width, size.height / 2)
                        quadraticTo(size.width / 2, size.height / 2, size.width / 2, size.height)
                        quadraticTo(size.width / 2, size.height / 2, 0f, size.height / 2)
                        quadraticTo(size.width / 2, size.height / 2, size.width / 2, 0f)
                    }
                    drawPath(path, color = Color.White.copy(alpha = 0.35f))
                }
                Spacer(modifier = Modifier.width(16.dp))
            }
        }

        // Dialog for Google Sign-In Setup & SHA-1 Copy
        if (showSha1Dialog) {
            val appSha1 = "F4:B0:7D:33:31:35:45:96:A4:25:D8:BB:5B:60:95:37:49:DE:FD:3D"
            AlertDialog(
                onDismissRequest = { showSha1Dialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = Color(0xFFA78BFA),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "গুগল সাইন-ইন সেটআপ (SHA-1)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = if (isBengali)
                                "গুগল প্লে সার্ভিসেস Error 10 নির্দেশ করে যে এই ডিভাইসের সাইনিং কি (SHA-1) ফায়ারবেস কনসোলে নিবন্ধিত নেই।"
                            else
                                "Google Play Services Error 10 means this app's signing certificate (SHA-1) is not registered in Firebase Console.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = Color.White
                        )

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0F172A),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Package: com.wbbseprep.app",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFA78BFA)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "SHA-1 Fingerprint:",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                    color = Color(0xFF94A3B8)
                                )
                                Text(
                                    text = appSha1,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = Color.White
                                )
                            }
                        }

                        Text(
                            text = if (isBengali)
                                "সমাধান:\n1. Firebase Console -> Project Settings এ যান।\n2. 'com.wbbseprep.app' এর নিচে 'Add fingerprint' ক্লিক করে SHA-1 পেস্ট করুন।"
                            else
                                "Fix:\n1. Go to Firebase Console -> Project Settings.\n2. Under 'com.wbbseprep.app', click 'Add fingerprint' and paste this SHA-1.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 15.sp),
                            color = Color(0xFF94A3B8)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(appSha1))
                            Toast.makeText(
                                context,
                                "SHA-1 ক্লিপবোর্ডে কপি করা হয়েছে!",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA78BFA)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("📋 SHA-1 কপি করুন")
                    }
                },
                dismissButton = {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TextButton(
                            onClick = {
                                showSha1Dialog = false
                                onGoogleSignIn("demo_token", "Google Student", "student@gmail.com")
                            }
                        ) {
                            Text(
                                text = "টেস্ট সাইন-ইন",
                                color = Color(0xFF10B981)
                            )
                        }
                        TextButton(onClick = { showSha1Dialog = false }) {
                            Text("বন্ধ করুন")
                        }
                    }
                }
            )
        }

        // Dialog when no Google accounts exist on device/emulator
        if (showNoAccountDialog) {
            AlertDialog(
                onDismissRequest = { showNoAccountDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = Color(0xFFA78BFA),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "গুগল অ্যাকাউন্ট পাওয়া যায়নি",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = if (isBengali)
                                "আপনার এই ডিভাইস বা এমুলেটরে কোনো সক্রিয় গুগল অ্যাকাউন্ট লগইন করা নেই। গুগল সাইন-ইন করতে হলে ফোনে অন্তত একটি গুগল অ্যাকাউন্ট থাকা আবশ্যক।"
                            else
                                "No active Google account is signed in on this device or emulator. Google Sign-In requires at least one Google account on the device.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = Color.White
                        )

                        Text(
                            text = if (isBengali)
                                "আপনি ফোনের Settings -> Accounts এ গিয়ে গুগল অ্যাকাউন্ট যোগ করতে পারেন, অথবা এখনই সরাসরি অ্যাপ টেস্ট করার জন্য নিচের 'টেস্ট গুগল সাইন-ইন' বাটন চাপুন।"
                            else
                                "You can add a Google account in phone Settings -> Accounts, or tap 'Demo Google Sign-In' below to test immediately.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = Color(0xFF94A3B8)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showNoAccountDialog = false
                            onGoogleSignIn("demo_token", "Google Student", "student@gmail.com")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA78BFA)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("⚡ টেস্ট গুগল সাইন-ইন (Demo)")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showNoAccountDialog = false }) {
                        Text("বন্ধ করুন")
                    }
                }
            )
        }
    }
