package com.dp.padhobihar.ui.agent

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.dp.padhobihar.databinding.FragmentAgentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AgentProfileFragment : Fragment() {

    private var _binding: FragmentAgentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AgentViewModel by activityViewModels()

    @Inject lateinit var auth: FirebaseAuth

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAgentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvEmail.text = auth.currentUser?.email ?: ""

        viewModel.user.observe(viewLifecycleOwner) { user ->
            user ?: return@observe
            binding.tvName.text = user.name
            binding.tvPhone.text = "Phone: ${user.phone}"
            binding.tvDistrict.text = "District: ${user.district}"
            binding.tvStatus.text = "Status: ${user.status}"
            binding.tvReferralCode.text = user.referralCode

            binding.btnShareWhatsapp.setOnClickListener {
                val text = "Join PadhoBihar for free admission guidance! Use my code: ${user.referralCode} Download app: https://play.google.com/store/apps/details?id=com.dp.padhobihar"
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    `package` = "com.whatsapp"
                    putExtra(Intent.EXTRA_TEXT, text)
                }
                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "WhatsApp not installed", Toast.LENGTH_SHORT).show()
                }
            }
        }
        viewModel.loadProfile()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
