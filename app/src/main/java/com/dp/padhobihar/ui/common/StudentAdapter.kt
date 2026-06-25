package com.dp.padhobihar.ui.common

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dp.padhobihar.R
import com.dp.padhobihar.databinding.ItemStudentBinding
import com.dp.padhobihar.domain.model.ApplicationStatus
import com.dp.padhobihar.domain.model.Student

class StudentAdapter(
    private val onClick: ((Student) -> Unit)? = null
) : ListAdapter<Student, StudentAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val binding: ItemStudentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(student: Student) {
            binding.tvStudentName.text = student.name
            binding.tvStudentPhone.text = student.phone
            binding.tvCourse.text = student.courseInterest
            binding.tvAvatar.text = student.name.firstOrNull()?.uppercase() ?: "?"

            val (label, colorRes) = getStatusInfo(student.status)
            binding.chipStatus.text = label
            binding.chipStatus.chipBackgroundColor = ColorStateList.valueOf(
                ContextCompat.getColor(binding.root.context, colorRes)
            )
            binding.chipStatus.setTextColor(ContextCompat.getColor(binding.root.context, R.color.white))

            binding.root.setOnClickListener { onClick?.invoke(student) }
        }

        private fun getStatusInfo(status: ApplicationStatus): Pair<String, Int> = when (status) {
            ApplicationStatus.REGISTERED -> "New" to R.color.text_secondary
            ApplicationStatus.PROFILE_COMPLETE -> "Profile" to R.color.status_inquiry
            ApplicationStatus.DOCS_UPLOADED -> "Docs" to R.color.status_docs
            ApplicationStatus.COLLEGE_REQUESTED -> "Requested" to R.color.primary
            ApplicationStatus.COLLEGE_SUGGESTED -> "Suggested" to R.color.accent
            ApplicationStatus.COLLEGE_CONFIRMED -> "Confirmed" to R.color.secondary
            ApplicationStatus.ADMITTED -> "Admitted" to R.color.status_admitted
            ApplicationStatus.WITHDRAWN -> "Withdrawn" to R.color.text_secondary
            ApplicationStatus.REJECTED -> "Rejected" to R.color.status_rejected
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStudentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private object DiffCallback : DiffUtil.ItemCallback<Student>() {
        override fun areItemsTheSame(oldItem: Student, newItem: Student) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Student, newItem: Student) = oldItem == newItem
    }
}
