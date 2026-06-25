package com.dp.padhobihar.ui.student

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.dp.padhobihar.databinding.ActivityEmailVerifyBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EmailVerifyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmailVerifyBinding
    private val viewModel: StudentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmailVerifyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        setupObservers()
    }

    private fun setupListeners() {
        binding.btnCheckVerification.setOnClickListener {
            viewModel.checkEmailVerified()
        }

        binding.btnResendEmail.setOnClickListener {
            viewModel.resendVerification()
            Toast.makeText(this, "Verification email sent again", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupObservers() {
        viewModel.registerState.observe(this) { state ->
            when (state) {
                is RegisterState.Verified -> {
                    Toast.makeText(this, "Email verified!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, StudentActivity::class.java))
                    finish()
                }
                is RegisterState.Error -> {
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }
}
