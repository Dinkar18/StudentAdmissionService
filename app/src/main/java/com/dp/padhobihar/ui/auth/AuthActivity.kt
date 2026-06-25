package com.dp.padhobihar.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.dp.padhobihar.databinding.ActivityAuthBinding
import com.dp.padhobihar.domain.model.Role
import com.dp.padhobihar.ui.admin.AdminActivity
import com.dp.padhobihar.ui.agent.AgentActivity
import com.dp.padhobihar.ui.student.StudentActivity
import com.dp.padhobihar.ui.student.StudentRegisterActivity
import com.dp.padhobihar.ui.student.EmailVerifyActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private val viewModel: AuthViewModel by viewModels()
    private lateinit var googleSignInClient: GoogleSignInClient

    @Inject lateinit var auth: FirebaseAuth

    private val roles = listOf("Student", "Agent", "Admin")

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            account?.idToken?.let { viewModel.loginWithGoogle(it) }
        } catch (e: ApiException) {
            Toast.makeText(this, "Google sign-in failed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Keep splash visible for ~1.5 seconds
        var keepSplash = true
        val startTime = System.currentTimeMillis()
        splashScreen.setKeepOnScreenCondition { keepSplash && System.currentTimeMillis() - startTime < 1500 }
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        keepSplash = false

        setupGoogleSignIn()
        setupDropdown()
        setupListeners()
        setupObservers()
        viewModel.checkCurrentUser()
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("684334699775-compute@developer.gserviceaccount.com")
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun setupDropdown() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, roles)
        binding.spinnerRole.setAdapter(adapter)
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            if (email.isEmpty() || password.length < 6) {
                Toast.makeText(this, "Enter valid email and password (min 6 chars)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val selectedRole = binding.spinnerRole.text.toString()
            viewModel.loginWithRole(email, password, selectedRole)
        }

        binding.btnGoogleSignIn.setOnClickListener {
            googleSignInClient.signOut().addOnCompleteListener {
                val signInIntent = googleSignInClient.signInIntent
                googleSignInLauncher.launch(signInIntent)
            }
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, StudentRegisterActivity::class.java))
        }

        binding.tvForgotPassword.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Enter your email first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    Toast.makeText(this, "Password reset email sent to $email", Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, it.message ?: "Failed to send email", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun setupObservers() {
        viewModel.authState.observe(this) { state ->
            when (state) {
                is AuthState.Loading -> binding.progressBar.visibility = View.VISIBLE
                is AuthState.NotLoggedIn -> binding.progressBar.visibility = View.GONE
                is AuthState.OtpSent -> binding.progressBar.visibility = View.GONE
                is AuthState.EmailNotVerified -> {
                    binding.progressBar.visibility = View.GONE
                    startActivity(Intent(this, EmailVerifyActivity::class.java))
                }
                is AuthState.Verified -> {
                    binding.progressBar.visibility = View.GONE
                    state.user?.let { navigateToRole(it.getUserRole()) }
                        ?: Toast.makeText(this, "Account not registered. Contact Admin.", Toast.LENGTH_LONG).show()
                }
                is AuthState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun navigateToRole(role: Role) {
        val intent = when (role) {
            Role.ADMIN -> {
                // Save admin credentials for re-auth when creating agents
                val email = binding.etEmail.text.toString().trim()
                val password = binding.etPassword.text.toString().trim()
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    getSharedPreferences("admin_prefs", MODE_PRIVATE).edit()
                        .putString("email", email)
                        .putString("password", password)
                        .apply()
                }
                Intent(this, AdminActivity::class.java)
            }
            Role.AGENT -> Intent(this, AgentActivity::class.java)
            Role.STUDENT -> Intent(this, StudentActivity::class.java)
        }
        startActivity(intent)
        finish()
    }
}
