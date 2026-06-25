package com.dp.padhobihar.ui.agent

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.dp.padhobihar.R
import com.dp.padhobihar.databinding.FragmentAgentStudentsBinding
import com.dp.padhobihar.ui.common.StudentAdapter
import com.dp.padhobihar.utils.UiState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AgentStudentsFragment : Fragment() {

    private var _binding: FragmentAgentStudentsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AgentViewModel by activityViewModels()
    private val adapter = StudentAdapter { student ->
        startActivity(Intent(requireContext(), AgentStudentDetailActivity::class.java).apply {
            putExtra(AgentStudentDetailActivity.EXTRA_STUDENT_ID, student.id)
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAgentStudentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvStudents.layoutManager = LinearLayoutManager(requireContext())
        binding.rvStudents.adapter = adapter
        binding.fabAddStudent.visibility = View.GONE
        binding.fabAddStudent.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.fab_show))

        viewModel.studentsState.observe(viewLifecycleOwner) { state ->
            val emptyLayout = view.findViewById<View>(R.id.layoutEmpty)
            when (state) {
                is UiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rvStudents.visibility = View.GONE
                    emptyLayout?.visibility = View.GONE
                }
                is UiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val list = state.data
                    adapter.submitList(list)
                    if (list.isEmpty()) {
                        binding.rvStudents.visibility = View.GONE
                        emptyLayout?.visibility = View.VISIBLE
                        view.findViewById<TextView>(R.id.tvEmptyTitle)?.text = "No students yet"
                        view.findViewById<TextView>(R.id.tvEmptySubtitle)?.text = "Share your referral code to get students"
                    } else {
                        binding.rvStudents.visibility = View.VISIBLE
                        emptyLayout?.visibility = View.GONE
                    }
                }
                is UiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rvStudents.visibility = View.GONE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.counts.observe(viewLifecycleOwner) { (total, pending, admitted) ->
            binding.tvTotal.text = total.toString()
            binding.tvPending.text = pending.toString()
            binding.tvAdmitted.text = admitted.toString()
        }

        viewModel.message.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }

        viewModel.loadStudents()
        viewModel.loadColleges()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
