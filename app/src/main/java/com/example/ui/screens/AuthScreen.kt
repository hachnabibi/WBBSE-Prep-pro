package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
    var school by remember { mutableStateOf("Kolkata Model High School") }
    var district by remember { mutableStateOf("কলকাতা") }
    var targetExam by remember { mutableStateOf("WBBSE Madhyamik 2026") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val credentialManager = CredentialManager.create(context)

    val isDark = isSystemInDarkTheme()

    // Modern color tokens
    val headingTextColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val subtitleTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val cardBgColor = if (isDark) Color(0xFF1E293B).copy(alpha = 0.95f) else Color.White
    val cardBorderColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
    val pillContainerColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9)
    val activePillBgColor = if (isDark) Color(0xFF334155) else Color.White

    // Modern Compact Text Field Styling
    val modernTextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.7f) else Color(0xFFF8FAFC),
        unfocusedContainerColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.5f) else Color(0xFFF8FAFC),
        focusedBorderColor = PrimaryIndigoLight,
        unfocusedBorderColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0),
        focusedLabelColor = PrimaryIndigoLight,
        unfocusedLabelColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
        focusedLeadingIconColor = PrimaryIndigoLight,
        unfocusedLeadingIconColor = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8),
        focusedTextColor = if (isDark) Color.White else Color(0xFF0F172A),
        unfocusedTextColor = if (isDark) Color.White else Color(0xFF0F172A),
        cursorColor = PrimaryIndigoLight
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                if (isDark) {
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF090D1A),
                            Color(0xFF0F172A),
                            Color(0xFF131032),
                            Color(0xFF0B0F19)
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFF3F6FF),
                            Color(0xFFF8FAFC),
                            Color(0xFFEEF2FF),
                            Color(0xFFF1F5F9)
                        )
                    )
                }
            )
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        // Decorative ambient gradient glow circles
        Box(
            modifier = Modifier
                .size(240.dp)
                .offset(x = (-80).dp, y = (-80).dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            if (isDark) Color(0xFF4F46E5).copy(alpha = 0.18f) else Color(0xFF6366F1).copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 60.dp, y = 60.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            if (isDark) Color(0xFF7C3AED).copy(alpha = 0.14f) else Color(0xFF818CF8).copy(alpha = 0.10f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Non-scrolling compact container perfectly fitted to screen
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Compact Header Row (Logo + Title + Badge)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF4338CA), Color(0xFF6366F1))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "App Logo",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "WBBSE Prep",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 17.sp
                            ),
                            color = headingTextColor
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = if (isDark) Color(0x336366F1) else Color(0xFFEEF2FF),
                            border = BorderStroke(0.5.dp, if (isDark) Color(0x55818CF8) else Color(0xFFC7D2FE))
                        ) {
                            Text(
                                text = "2026",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = if (isDark) Color(0xFFA5B4FC) else PrimaryIndigo,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Text(
                        text = "মাধ্যমিক পরীক্ষার সম্পূর্ণ প্রস্তুতি প্ল্যাটফর্ম",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = subtitleTextColor
                    )
                }
            }

            // Main Auth Form Card - Clean, Fits Screen with No Scroll
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = cardBgColor),
                border = BorderStroke(1.dp, cardBorderColor),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 2.dp else 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_form_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Modern Segmented Mode Toggle (Sign In vs Sign Up)
                    if (authMode != AuthMode.FORGOT_PASSWORD) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(pillContainerColor)
                                .padding(2.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            val isSignIn = authMode == AuthMode.SIGN_IN
                            val isSignUp = authMode == AuthMode.SIGN_UP

                            // Sign In Tab
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSignIn) activePillBgColor else Color.Transparent)
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
                                        fontSize = 12.sp
                                    ),
                                    color = if (isSignIn) {
                                        if (isDark) Color.White else PrimaryIndigo
                                    } else subtitleTextColor
                                )
                            }

                            // Sign Up Tab
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSignUp) activePillBgColor else Color.Transparent)
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
                                        fontSize = 12.sp
                                    ),
                                    color = if (isSignUp) {
                                        if (isDark) Color.White else PrimaryIndigo
                                    } else subtitleTextColor
                                )
                            }
                        }
                    } else {
                        // Forgot Password Back Header
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(
                                onClick = {
                                    authMode = AuthMode.SIGN_IN
                                    onClearError()
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back to Sign In",
                                    tint = headingTextColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "লগইন স্ক্রিনে ফিরে যান",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = headingTextColor
                            )
                        }
                    }

                    // Alerts: Error / Success Messages
                    val displayError = errorMessage ?: localError
                    if (!displayError.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
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
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = "Error",
                                    tint = WrongRed,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = displayError,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 11.sp,
                                        lineHeight = 14.sp
                                    ),
                                    color = if (isDark) Color(0xFFFCA5A5) else Color(0xFFB91C1C),
                                    modifier = Modifier.weight(1f)
                                )
                                if (displayError.contains("SHA-1") || displayError.contains("Developer console", ignoreCase = true) || displayError.contains("10")) {
                                    Text(
                                        text = "সাহায্য",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                        color = PrimaryIndigoLight,
                                        modifier = Modifier.padding(start = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (!successMessage.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CorrectGreen.copy(alpha = 0.08f),
                            border = BorderStroke(0.5.dp, CorrectGreen.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Success",
                                    tint = CorrectGreen,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = successMessage,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 11.sp,
                                        lineHeight = 14.sp
                                    ),
                                    color = if (isDark) Color(0xFF6EE7B7) else Color(0xFF047857),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Sign Up Specific Fields (Name)
                    AnimatedVisibility(visible = authMode == AuthMode.SIGN_UP) {
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
                            shape = RoundedCornerShape(10.dp),
                            colors = modernTextFieldColors,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_full_name")
                        )
                    }

                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("ইমেইল অ্যাড্রেস", fontSize = 12.sp) },
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
                        shape = RoundedCornerShape(10.dp),
                        colors = modernTextFieldColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_email")
                    )

                    // Password Field (Hidden in Forgot Password mode)
                    if (authMode != AuthMode.FORGOT_PASSWORD) {
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { 
                                Text(
                                    if (isBengali) {
                                        if (authMode == AuthMode.SIGN_UP) "পাসওয়ার্ড তৈরি করুন" else "পাসওয়ার্ড"
                                    } else {
                                        if (authMode == AuthMode.SIGN_UP) "Create Password" else "Password"
                                    }, 
                                    fontSize = 12.sp
                                ) 
                            },
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
                                        modifier = Modifier.size(18.dp)
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
                            shape = RoundedCornerShape(10.dp),
                            colors = modernTextFieldColors,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_password")
                        )
                        if (authMode == AuthMode.SIGN_UP) {
                            Text(
                                text = "কমপক্ষে ৮ অক্ষরের পাসওয়ার্ড ব্যবহার করুন",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                color = subtitleTextColor,
                                modifier = Modifier.padding(start = 4.dp, top = 2.dp, bottom = 4.dp)
                            )
                        }
                    }

                    // Confirm Password (Only in Sign Up mode)
                    if (authMode == AuthMode.SIGN_UP) {
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("পাসওয়ার্ড নিশ্চিত করুন", fontSize = 12.sp) },
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
                                        modifier = Modifier.size(18.dp)
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
                                        onSignUp(name, "", "", targetExam, email, password)
                                    }
                                }
                            ),
                            shape = RoundedCornerShape(10.dp),
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
                                    checkedColor = PrimaryIndigoLight,
                                    uncheckedColor = subtitleTextColor
                                ),
                                modifier = Modifier.size(20.dp).padding(end = 8.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = androidx.compose.ui.text.buildAnnotatedString {
                                    append("আমি ব্যবহারের ")
                                    withStyle(androidx.compose.ui.text.SpanStyle(color = PrimaryIndigoLight, textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline)) {
                                        append("শর্তাবলী")
                                    }
                                    append(" ও ")
                                    withStyle(androidx.compose.ui.text.SpanStyle(color = PrimaryIndigoLight, textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline)) {
                                        append("গোপনীয়তা নীতি")
                                    }
                                    append(" মেনে নিচ্ছি")
                                },
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = subtitleTextColor,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Forgot Password link (in Sign In mode)
                    if (authMode == AuthMode.SIGN_IN) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = "পাসওয়ার্ড ভুলে গেছেন?",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                ),
                                color = PrimaryIndigoLight,
                                modifier = Modifier
                                    .clickable {
                                        authMode = AuthMode.FORGOT_PASSWORD
                                        onClearError()
                                    }
                                    .padding(vertical = 1.dp)
                            )
                        }
                    }

                    // Primary Action Button (Modern Gradient Fill - Compact 42dp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isLoading) {
                                    Brush.horizontalGradient(listOf(Color(0xFF94A3B8), Color(0xFF94A3B8)))
                                } else {
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color(0xFF4338CA),
                                            Color(0xFF4F46E5),
                                            Color(0xFF6366F1)
                                        )
                                    )
                                }
                            )
                            .clickable(enabled = !isLoading) {
                                focusManager.clearFocus()
                                when (authMode) {
                                    AuthMode.SIGN_IN -> onSignIn(email, password)
                                    AuthMode.SIGN_UP -> onSignUp(name, "", "", targetExam, email, password)
                                    AuthMode.FORGOT_PASSWORD -> onForgotPassword(email)
                                }
                            }
                            .testTag("auth_submit_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = when (authMode) {
                                        AuthMode.SIGN_IN -> "সাইন ইন করুন"
                                        AuthMode.SIGN_UP -> "অ্যাকাউন্ট তৈরি করুন"
                                        AuthMode.FORGOT_PASSWORD -> "রিসেট লিংক পাঠান"
                                    },
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    ),
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Social Google Sign-In Button (Available in Sign In & Sign Up modes)
                    if (authMode != AuthMode.FORGOT_PASSWORD) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                color = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
                            )
                            Text(
                                text = "অথবা",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = subtitleTextColor,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                color = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
                            )
                        }

                        // Original Google Social Login Button - Compact 40dp
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
                                        // User dismissed the account chooser
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
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFDADCE0)),
                            shadowElevation = 1.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
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
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (isBengali) {
                                        if (authMode == AuthMode.SIGN_UP) "Google দিয়ে অ্যাকাউন্ট তৈরি করুন" else "Google দিয়ে সাইন ইন"
                                    } else {
                                        if (authMode == AuthMode.SIGN_UP) "Sign up with Google" else "Sign in with Google"
                                    },
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp
                                    ),
                                    color = Color(0xFF1F1F1F)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Action Card
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isDark) Color(0xFF1A2639) else Color.White.copy(alpha = 0.85f),
                border = BorderStroke(0.5.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
                modifier = Modifier
                    .fillMaxWidth()
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = if (authMode == AuthMode.SIGN_UP) Arrangement.Center else Arrangement.SpaceBetween
                ) {
                    if (authMode == AuthMode.SIGN_UP) {
                        Text(
                            text = androidx.compose.ui.text.buildAnnotatedString {
                                append("আগেই অ্যাকাউন্ট আছে? ")
                                withStyle(androidx.compose.ui.text.SpanStyle(color = PrimaryIndigoLight, fontWeight = FontWeight.Bold)) {
                                    append("সাইন ইন করুন ->")
                                }
                            },
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = subtitleTextColor
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF312E81)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Explore,
                                    contentDescription = null,
                                    tint = Color(0xFFA5B4FC),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "লগইন ছাড়াই দেখতে চান?\nগেস্ট মোড",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = subtitleTextColor
                            )
                        }
                        Text(
                            text = "প্রবেশ করুন →",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            ),
                            color = PrimaryIndigoLight
                        )
                    }
                }
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
                            tint = PrimaryIndigoLight,
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
                            color = headingTextColor
                        )

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9),
                            border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Package: com.wbbseprep.app",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = PrimaryIndigoLight
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "SHA-1 Fingerprint:",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                    color = subtitleTextColor
                                )
                                Text(
                                    text = appSha1,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = headingTextColor
                                )
                            }
                        }

                        Text(
                            text = if (isBengali)
                                "সমাধান:\n1. Firebase Console -> Project Settings এ যান।\n2. 'com.wbbseprep.app' এর নিচে 'Add fingerprint' ক্লিক করে SHA-1 পেস্ট করুন।"
                            else
                                "Fix:\n1. Go to Firebase Console -> Project Settings.\n2. Under 'com.wbbseprep.app', click 'Add fingerprint' and paste this SHA-1.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 15.sp),
                            color = subtitleTextColor
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
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigoLight),
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
                                color = SecondaryEmeraldLight
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
                            tint = PrimaryIndigoLight,
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
                            color = headingTextColor
                        )

                        Text(
                            text = if (isBengali)
                                "আপনি ফোনের Settings -> Accounts এ গিয়ে গুগল অ্যাকাউন্ট যোগ করতে পারেন, অথবা এখনই সরাসরি অ্যাপ টেস্ট করার জন্য নিচের 'টেস্ট গুগল সাইন-ইন' বাটন চাপুন।"
                            else
                                "You can add a Google account in phone Settings -> Accounts, or tap 'Demo Google Sign-In' below to test immediately.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = subtitleTextColor
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showNoAccountDialog = false
                            onGoogleSignIn("demo_token", "Google Student", "student@gmail.com")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigoLight),
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
}
