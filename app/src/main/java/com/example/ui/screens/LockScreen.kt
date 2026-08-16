package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.PinEntryView

@Composable
fun LockScreen(
    isPinSet: Boolean,
    isBiometricEnabled: Boolean,
    onPinEntered: (String, (Boolean) -> Unit) -> Unit,
    onPinSetup: (String, (Boolean) -> Unit) -> Unit,
    onBiometricClick: () -> Unit,
    onBiometricResetClick: ((((Boolean) -> Unit) -> Unit))? = null,
    onPinReset: ((String, (Boolean) -> Unit) -> Unit)? = null
) {
    var enteredPin by remember { mutableStateOf("") }
    var setupFirstPin by remember { mutableStateOf("") }
    var isConfirmingSetup by remember { mutableStateOf(false) }
    var isResettingViaBiometric by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    com.example.ui.components.Background3DCanvas {
        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                com.example.ui.components.GlassmorphicCard(
                    modifier = Modifier
                        .padding(24.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
                    elevation = 10.dp
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        // Dastavej Box Logo Header
                        Image(
                            painter = painterResource(id = R.drawable.ic_dastavej_logo),
                            contentDescription = "Dastavej Box Logo",
                            modifier = Modifier.size(105.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Offline Digital Document Vault",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        if (!isPinSet || isResettingViaBiometric) {
                            // PIN Setup or Biometric Reset Mode
                            val title = if (!isConfirmingSetup) {
                                if (isResettingViaBiometric) "Set New 4-Digit PIN" else "Create Master PIN"
                            } else "Confirm Master PIN"
                            val subtitle = if (!isConfirmingSetup) {
                                if (isResettingViaBiometric) "Biometric verified. Enter your new vault PIN" else "Create a secure 4-digit PIN to encrypt your local documents"
                            } else "Re-enter the same 4 digits to confirm"
                            val stepIndicator = if (!isResettingViaBiometric) {
                                if (!isConfirmingSetup) "STEP 1 OF 2 : CREATE PIN" else "STEP 2 OF 2 : CONFIRM PIN"
                            } else null

                            PinEntryView(
                                enteredPin = enteredPin,
                                title = title,
                                subtitle = subtitle,
                                stepIndicator = stepIndicator,
                                errorMessage = errorMessage,
                                onPinDigitEntered = { digit ->
                                    if (enteredPin.length < 4) {
                                        enteredPin += digit
                                        errorMessage = null
                                        if (enteredPin.length == 4) {
                                            if (!isConfirmingSetup) {
                                                setupFirstPin = enteredPin
                                                enteredPin = ""
                                                isConfirmingSetup = true
                                            } else {
                                                if (enteredPin == setupFirstPin) {
                                                    if (isResettingViaBiometric && onPinReset != null) {
                                                        onPinReset(enteredPin) { success ->
                                                            if (success) {
                                                                isResettingViaBiometric = false
                                                                isConfirmingSetup = false
                                                                setupFirstPin = ""
                                                                enteredPin = ""
                                                            } else {
                                                                errorMessage = "Failed to update PIN"
                                                                enteredPin = ""
                                                            }
                                                        }
                                                    } else {
                                                        onPinSetup(enteredPin) { success ->
                                                            if (!success) {
                                                                errorMessage = "Failed to save PIN"
                                                                enteredPin = ""
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    errorMessage = "PINs do not match. Try again."
                                                    enteredPin = ""
                                                    isConfirmingSetup = false
                                                    setupFirstPin = ""
                                                }
                                            }
                                        }
                                    }
                                },
                                onPinDigitDeleted = {
                                    if (enteredPin.isNotEmpty()) {
                                        enteredPin = enteredPin.dropLast(1)
                                        errorMessage = null
                                    }
                                }
                            )

                            if (isResettingViaBiometric) {
                                Spacer(modifier = Modifier.height(12.dp))
                                TextButton(
                                    onClick = {
                                        isResettingViaBiometric = false
                                        isConfirmingSetup = false
                                        setupFirstPin = ""
                                        enteredPin = ""
                                        errorMessage = null
                                    }
                                ) {
                                    Text("Cancel Reset", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        } else {
                            // Unlock Vault Mode
                            PinEntryView(
                                enteredPin = enteredPin,
                                title = "Enter Vault PIN",
                                subtitle = "Private & offline local document storage",
                                errorMessage = errorMessage,
                                onBiometricClick = if (isBiometricEnabled) onBiometricClick else null,
                                onPinDigitEntered = { digit ->
                                    if (enteredPin.length < 4) {
                                        enteredPin += digit
                                        errorMessage = null
                                        if (enteredPin.length == 4) {
                                            onPinEntered(enteredPin) { isValid ->
                                                if (!isValid) {
                                                    errorMessage = "Incorrect PIN"
                                                    enteredPin = ""
                                                }
                                            }
                                        }
                                    }
                                },
                                onPinDigitDeleted = {
                                    if (enteredPin.isNotEmpty()) {
                                        enteredPin = enteredPin.dropLast(1)
                                        errorMessage = null
                                    }
                                }
                            )

                            if (onBiometricResetClick != null) {
                                Spacer(modifier = Modifier.height(12.dp))
                                TextButton(
                                    onClick = {
                                        onBiometricResetClick { authenticated ->
                                            if (authenticated) {
                                                isResettingViaBiometric = true
                                                isConfirmingSetup = false
                                                setupFirstPin = ""
                                                enteredPin = ""
                                                errorMessage = null
                                            }
                                        }
                                    },
                                    modifier = Modifier.testTag("btn_forgot_pin_reset")
                                ) {
                                    Text(
                                        text = "Forgot PIN? Reset with Biometric",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
