package com.mindforce.mindlog.ui.screens.login

import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.material.textfield.TextInputEditText
import com.mindforce.mindlog.R
import com.mindforce.mindlog.data.repository.AuthRepository

@Composable
fun LoginScreen(
    authRepository: AuthRepository,
    onLoginSuccess: () -> Unit
) {
    val viewModel: LoginViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return LoginViewModel(authRepository) as T
        }
    })

    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.loginSuccess) {
        if (state.loginSuccess) onLoginSuccess()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                val contextWrapper = ContextThemeWrapper(context, R.style.Theme_MindForce)
                val view = LayoutInflater.from(contextWrapper).inflate(R.layout.fragment_login, null)
                view.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                
                // Initialisation des vues et des listeners
                val emailInput = view.findViewById<TextInputEditText>(R.id.emailInput)
                val passwordInput = view.findViewById<TextInputEditText>(R.id.passwordInput)
                val loginButton = view.findViewById<Button>(R.id.loginButton)
                
                val codeInput = view.findViewById<TextInputEditText>(R.id.codeInput)
                val verifyButton = view.findViewById<Button>(R.id.verifyButton)
                val backButton = view.findViewById<Button>(R.id.backToCredentialsButton)

                // Listeners pour mettre à jour le ViewModel (approche simplifiée)
                loginButton.setOnClickListener {
                    viewModel.onEmailChange(emailInput.text.toString())
                    viewModel.onPasswordChange(passwordInput.text.toString())
                    viewModel.submitCredentials()
                }

                verifyButton.setOnClickListener {
                    viewModel.onCodeChange(codeInput.text.toString())
                    viewModel.submitVerification()
                }

                backButton.setOnClickListener {
                    viewModel.backToCredentials()
                }

                view
            },
            update = { view ->
                // Mise à jour de l'UI selon l'état du ViewModel
                val errorBanner = view.findViewById<TextView>(R.id.errorBanner)
                val credentialsLayout = view.findViewById<LinearLayout>(R.id.credentialsLayout)
                val verificationLayout = view.findViewById<LinearLayout>(R.id.verificationLayout)
                val subtitleText = view.findViewById<TextView>(R.id.subtitleText)
                val verificationInfoText = view.findViewById<TextView>(R.id.verificationInfoText)
                val loginButton = view.findViewById<Button>(R.id.loginButton)
                val verifyButton = view.findViewById<Button>(R.id.verifyButton)

                // Gestion de l'erreur
                errorBanner.visibility = if (state.errorMessage != null) View.VISIBLE else View.GONE
                errorBanner.text = state.errorMessage

                // Gestion des étapes
                if (state.step == LoginStep.CREDENTIALS) {
                    credentialsLayout.visibility = View.VISIBLE
                    verificationLayout.visibility = View.GONE
                    subtitleText.text = "Connectez-vous à votre compte"
                } else {
                    credentialsLayout.visibility = View.GONE
                    verificationLayout.visibility = View.VISIBLE
                    subtitleText.text = "Vérification en 2 étapes"
                    verificationInfoText.text = "Un code à 6 chiffres a été envoyé à ${state.email}"
                }

                // État de chargement
                loginButton.isEnabled = !state.isLoading
                loginButton.text = if (state.isLoading) "Chargement..." else "Se connecter"
                verifyButton.isEnabled = !state.isLoading
                verifyButton.text = if (state.isLoading) "Chargement..." else "Vérifier"
            }
        )

        if (state.step == LoginStep.VERIFICATION) {
            IconButton(
                onClick = { viewModel.backToCredentials() },
                modifier = Modifier
                    .padding(top = 16.dp, start = 16.dp)
                    .align(Alignment.TopStart)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Retour",
                    tint = com.mindforce.mindlog.ui.theme.MindBlack
                )
            }
        }
    }
}
