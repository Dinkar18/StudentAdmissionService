package com.dp.padhobihar.ui.student

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.dp.padhobihar.databinding.FragmentStudentCollegesBinding
import com.dp.padhobihar.domain.model.ApplicationStatus
import com.dp.padhobihar.domain.model.College
import com.dp.padhobihar.ui.common.CollegeAdapter
import com.dp.padhobihar.utils.UiState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StudentCollegesFragment : Fragment() {

    private var _binding: FragmentStudentCollegesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: StudentViewModel by activityViewModels()
    private val adapter = CollegeAdapter { college -> onCollegeClick(college) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStudentCollegesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvColleges.layoutManager = LinearLayoutManager(requireContext())
        binding.rvColleges.adapter = adapter

        viewModel.collegesState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rvColleges.visibility = View.GONE
                }
                is UiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rvColleges.visibility = View.VISIBLE
                    adapter.submitList(state.data)
                }
                is UiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
        viewModel.message.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }
        viewModel.loadColleges()
    }

    private fun onCollegeClick(college: College) {
        val status = viewModel.student.value?.status
        if (status != ApplicationStatus.DOCS_UPLOADED && status != ApplicationStatus.COLLEGE_SUGGESTED) {
            val msg = when {
                status == ApplicationStatus.REGISTERED -> "Complete your profile first"
                status == ApplicationStatus.PROFILE_COMPLETE -> "Upload all documents first"
                status == ApplicationStatus.COLLEGE_REQUESTED -> "You already requested a college. Wait for agent review."
                status == ApplicationStatus.COLLEGE_CONFIRMED -> "Your college is already confirmed"
                status == ApplicationStatus.ADMITTED -> "You're already admitted!"
                else -> "Cannot request college at this stage"
            }
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Request Admission")
            .setMessage("Request admission to:\n\n${college.name}\n${college.district}")
            .setPositiveButton("Request") { _, _ -> viewModel.requestCollege(college.id) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
