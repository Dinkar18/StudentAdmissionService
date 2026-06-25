package com.dp.padhobihar.ui.common

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dp.padhobihar.R
import com.dp.padhobihar.databinding.ItemStudentAdminBinding
import com.dp.padhobihar.domain.model.Student

class AdminStudentAdapter(
    private val onConfirm: (Student) -> Unit,
    private val onReject: (Student) -> Unit
) : ListAdapter<Student, AdminStudentAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val binding: ItemStudentAdminBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(student: Student) {
            binding.tvStudentName.text = student.name
            binding.tvStudentPhone.text = student.phone
            binding.tvCourse.text = student.courseInterest
            binding.tvAvatar.text = student.name.firstOrNull()?.uppercase() ?: "?"
            binding.chipStatus.text = "Confirmed"
            binding.chipStatus.chipBackgroundColor = ColorStateList.valueOf(
                ContextCompat.getColor(binding.root.context, R.color.secondary)
            )
            binding.chipStatus.setTextColor(ContextCompat.getColor(binding.root.context, R.color.white))

            binding.btnConfirm.setOnClickListener { onConfirm(student) }
            binding.btnReject.setOnClickListener { onReject(student) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStudentAdminBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    private object DiffCallback : DiffUtil.ItemCallback<Student>() {
        override fun areItemsTheSame(oldItem: Student, newItem: Student) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Student, newItem: Student) = oldItem == newItem
    }
}
