package com.dp.padhobihar.ui.agent

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.dp.padhobihar.databinding.ActivityAgentStudentDetailBinding
import com.dp.padhobihar.domain.model.ApplicationStatus
import com.dp.padhobihar.utils.UiState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AgentStudentDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAgentStudentDetailBinding
    private val viewModel: AgentViewModel by viewModels()

    companion object {
        const val EXTRA_STUDENT_ID = "student_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAgentStudentDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val studentId = intent.getStringExtra(EXTRA_STUDENT_ID) ?: run { finish(); return }

        viewModel.loadStudents()
        viewModel.loadColleges()

        viewModel.studentsState.observe(this) { state ->
            if (state !is UiState.Success) return@observe
            val student = state.data.find { it.id == studentId } ?: return@observe

            binding.tvName.text = student.name
            binding.tvPhone.text = "📞 ${student.phone}"
            binding.tvFatherName.text = "Father: ${student.fatherName.ifEmpty { "N/A" }}"
            binding.tvVillage.text = "Village: ${student.village.ifEmpty { "N/A" }}"
            binding.tvDistrict.text = "District: ${student.district.ifEmpty { "N/A" }}"
            binding.tvQualification.text = "Qualification: ${student.qualification.name}"
            binding.tvMarks.text = "Marks: ${if (student.marks > 0) "${student.marks}%" else "N/A"}"
            binding.tvCourseInterest.text = "Course: ${student.courseInterest.ifEmpty { "N/A" }}"
            binding.tvDocStatus.text = if (student.documents.isNotEmpty()) "📄 Documents: Uploaded" else "📄 Documents: Not uploaded"
            binding.tvStatus.text = "Status: ${student.status.name}"

            // Show buttons based on status
            when (student.status) {
                ApplicationStatus.COLLEGE_REQUESTED -> {
                    binding.btnApprove.visibility = View.VISIBLE
                    binding.btnSuggest.visibility = View.VISIBLE
                    binding.btnApprove.setOnClickListener {
                        viewModel.approveCollegeRequest(student.id)
                    }
                    binding.btnSuggest.setOnClickListener { showSuggestDialog(student.id) }
                }
                ApplicationStatus.DOCS_UPLOADED -> {
                    binding.btnApprove.visibility = View.GONE
                    binding.btnSuggest.visibility = View.VISIBLE
                    binding.btnSuggest.setOnClickListener { showSuggestDialog(student.id) }
                }
                else -> {
                    binding.btnApprove.visibility = View.GONE
                    binding.btnSuggest.visibility = View.GONE
                }
            }
        }

        viewModel.message.observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showSuggestDialog(studentId: String) {
        val colleges = (viewModel.collegesState.value as? UiState.Success)?.data ?: return
        if (colleges.isEmpty()) {
            Toast.makeText(this, "No colleges available", Toast.LENGTH_SHORT).show()
            return
        }
        val names = colleges.map { it.name }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Suggest College")
            .setItems(names) { _, which ->
                viewModel.suggestCollege(studentId, colleges[which].id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
