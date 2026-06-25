package com.dp.padhobihar.ui.student

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.dp.padhobihar.databinding.FragmentStudentProfileBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StudentProfileFragment : Fragment() {

    private var _binding: FragmentStudentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: StudentViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStudentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val qualifications = listOf("10th Pass", "12th Pass")
        binding.spinnerQualification.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, qualifications)
        )

        viewModel.user.observe(viewLifecycleOwner) { user ->
            user ?: return@observe
            binding.etName.setText(user.name)
            binding.etPhone.setText(user.phone)
            binding.etDistrict.setText(user.district)
        }

        viewModel.profile.observe(viewLifecycleOwner) { profile ->
            profile ?: return@observe
            binding.etFatherName.setText(profile["fatherName"] as? String ?: "")
            binding.etVillage.setText(profile["village"] as? String ?: "")
            binding.etMarks.setText((profile["marks"] as? Number)?.toString() ?: "")
            binding.etCourseInterest.setText(profile["courseInterest"] as? String ?: "")
            val qual = profile["qualification"] as? String ?: ""
            if (qual.isNotEmpty()) binding.spinnerQualification.setText(qual, false)
        }

        binding.btnSaveProfile.setOnClickListener {
            val data = mapOf(
                "name" to binding.etName.text.toString().trim(),
                "phone" to binding.etPhone.text.toString().trim(),
                "fatherName" to binding.etFatherName.text.toString().trim(),
                "village" to binding.etVillage.text.toString().trim(),
                "district" to binding.etDistrict.text.toString().trim(),
                "qualification" to binding.spinnerQualification.text.toString().trim(),
                "marks" to (binding.etMarks.text.toString().toFloatOrNull() ?: 0f),
                "courseInterest" to binding.etCourseInterest.text.toString().trim()
            )
            viewModel.saveProfile(data)
        }

        viewModel.message.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }

        viewModel.loadProfile()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
