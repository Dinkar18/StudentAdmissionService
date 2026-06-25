package com.dp.padhobihar.ui.student

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dp.padhobihar.R
import com.dp.padhobihar.databinding.ActivityStudentBinding
import com.dp.padhobihar.ui.auth.AuthActivity
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class StudentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentBinding

    @Inject lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.nav_home -> StudentHomeFragment()
                R.id.nav_colleges -> StudentCollegesFragment()
                R.id.nav_documents -> StudentDocumentsFragment()
                R.id.nav_profile -> StudentProfileFragment()
                else -> StudentHomeFragment()
            }
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                .replace(R.id.fragmentContainer, fragment)
                .commit()
            true
        }

        // Default tab
        binding.bottomNav.selectedItemId = R.id.nav_home
    }
}
