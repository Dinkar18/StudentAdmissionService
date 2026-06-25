package com.dp.padhobihar.ui.student

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.dp.padhobihar.databinding.FragmentStudentHomeBinding
import com.dp.padhobihar.domain.model.ApplicationStatus
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StudentHomeFragment : Fragment() {

    private var _binding: FragmentStudentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: StudentViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStudentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadStudentData()
        viewModel.loadAgentInfo()

        viewModel.student.observe(viewLifecycleOwner) { student ->
            if (student != null) {
                binding.tvStatus.text = student.status.name
                binding.tvStatusDesc.text = getStatusDesc(student.status)
                buildTimeline(student.status)
                showActionButtons(student.status)
            } else {
                binding.tvStatus.text = "REGISTERED"
                binding.tvStatusDesc.text = "Complete your profile to get started"
                buildTimeline(null)
                hideActionButtons()
            }
        }

        viewModel.agentInfo.observe(viewLifecycleOwner) { agent ->
            if (agent != null) {
                binding.cardAgentInfo.visibility = View.VISIBLE
                binding.tvAgentName.text = agent.name
                binding.tvAgentPhone.text = "📞 ${agent.phone}"
                binding.btnChatAgent.setOnClickListener {
                    com.dp.padhobihar.utils.WhatsAppHelper.openChat(
                        requireContext(),
                        agent.phone,
                        "Hi ${agent.name}, I am a PadhoBihar student. I need help with my admission."
                    )
                }
            }
        }

        viewModel.message.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }

        binding.btnAccept.setOnClickListener { viewModel.acceptSuggestedCollege() }
        binding.btnWithdraw.setOnClickListener { viewModel.withdraw() }
    }

    private fun showActionButtons(status: ApplicationStatus) {
        when (status) {
            ApplicationStatus.COLLEGE_SUGGESTED -> {
                binding.layoutActions.visibility = View.VISIBLE
                binding.btnAccept.visibility = View.VISIBLE
                binding.btnWithdraw.visibility = View.VISIBLE
            }
            ApplicationStatus.COLLEGE_REQUESTED, ApplicationStatus.COLLEGE_CONFIRMED -> {
                binding.layoutActions.visibility = View.VISIBLE
                binding.btnAccept.visibility = View.GONE
                binding.btnWithdraw.visibility = View.VISIBLE
            }
            else -> hideActionButtons()
        }
    }

    private fun hideActionButtons() {
        binding.layoutActions.visibility = View.GONE
        binding.btnAccept.visibility = View.GONE
        binding.btnWithdraw.visibility = View.GONE
    }

    private fun getStatusDesc(status: ApplicationStatus) = when (status) {
        ApplicationStatus.REGISTERED -> "Complete your profile to continue"
        ApplicationStatus.PROFILE_COMPLETE -> "Upload your documents"
        ApplicationStatus.DOCS_UPLOADED -> "Your agent is reviewing"
        ApplicationStatus.COLLEGE_REQUESTED -> "College request sent to agent"
        ApplicationStatus.COLLEGE_SUGGESTED -> "Agent suggested a college — check below"
        ApplicationStatus.COLLEGE_CONFIRMED -> "College confirmed! Waiting for admission"
        ApplicationStatus.ADMITTED -> "🎉 Congratulations! You're admitted!"
        ApplicationStatus.WITHDRAWN -> "You withdrew your application"
        ApplicationStatus.REJECTED -> "Application not successful"
    }

    private fun buildTimeline(currentStatus: ApplicationStatus?) {
        binding.layoutTimeline.removeAllViews()
        val statuses = ApplicationStatus.entries
        val currentIndex = if (currentStatus != null) statuses.indexOf(currentStatus) else -1

        statuses.forEachIndexed { index, status ->
            val tv = TextView(requireContext()).apply {
                text = if (index <= currentIndex) "✅  ${status.name.replace("_", " ")}"
                       else "⬜  ${status.name.replace("_", " ")}"
                textSize = 14f
                setPadding(0, 12, 0, 12)
                alpha = if (index <= currentIndex) 1f else 0.4f
            }
            binding.layoutTimeline.addView(tv)
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
