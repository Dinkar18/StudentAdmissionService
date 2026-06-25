package com.dp.padhobihar.utils

import android.content.Context
import android.content.Intent
import com.dp.padhobihar.domain.model.Role
import com.dp.padhobihar.ui.admin.AdminActivity
import com.dp.padhobihar.ui.agent.AgentActivity
import com.dp.padhobihar.ui.auth.AuthActivity
import com.dp.padhobihar.ui.student.StudentActivity

object Navigator {

    fun toHome(context: Context, role: Role) {
        val target = when (role) {
            Role.ADMIN -> AdminActivity::class.java
            Role.AGENT -> AgentActivity::class.java
            Role.STUDENT -> StudentActivity::class.java
        }
        context.startActivity(Intent(context, target).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
    }

    fun toLogin(context: Context) {
        context.startActivity(Intent(context, AuthActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
    }
}
