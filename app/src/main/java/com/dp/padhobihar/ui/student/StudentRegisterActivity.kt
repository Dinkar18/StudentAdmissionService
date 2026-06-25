package com.dp.padhobihar.ui.student

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.dp.padhobihar.databinding.ActivityStudentRegisterBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StudentRegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentRegisterBinding
    private val viewModel: StudentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        setupObservers()
    }

    private fun setupListeners() {
        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            val referralCode = binding.etReferralCode.text.toString().trim().uppercase()

            if (name.isEmpty() || email.isEmpty() || password.length < 6 || phone.length != 10) {
                Toast.makeText(this, "Fill all fields. Password min 6 chars.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (referralCode.isEmpty()) {
                Toast.makeText(this, "Enter agent referral code", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.register(name, email, password, phone, referralCode)
        }

        binding.tvLogin.setOnClickListener { finish() }
    }

    private fun setupObservers() {
        viewModel.registerState.observe(this) { state ->
            when (state) {
                is RegisterState.Loading -> binding.progressBar.visibility = View.VISIBLE
                is RegisterState.VerificationSent -> {
                    binding.progressBar.visibility = View.GONE
                    // Skip verification for now - go directly to student dashboard
                    startActivity(Intent(this, StudentActivity::class.java))
                    finish()
                }
                is RegisterState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
                else -> binding.progressBar.visibility = View.GONE
            }
        }
    }
}
