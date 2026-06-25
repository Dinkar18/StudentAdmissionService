package com.dp.padhobihar.ui.agent

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dp.padhobihar.R
import com.dp.padhobihar.databinding.ActivityAgentBinding
import com.dp.padhobihar.ui.auth.AuthActivity
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AgentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAgentBinding

    @Inject lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAgentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.nav_students -> AgentStudentsFragment()
                R.id.nav_colleges -> AgentCollegesFragment()
                R.id.nav_earnings -> AgentEarningsFragment()
                R.id.nav_profile -> AgentProfileFragment()
                else -> AgentStudentsFragment()
            }
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                .replace(R.id.fragmentContainer, fragment)
                .commit()
            true
        }

        binding.bottomNav.selectedItemId = R.id.nav_students
    }
}
