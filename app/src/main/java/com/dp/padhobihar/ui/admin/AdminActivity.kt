package com.dp.padhobihar.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dp.padhobihar.R
import com.dp.padhobihar.databinding.ActivityAdminBinding
import com.dp.padhobihar.databinding.DialogAddUserBinding
import com.dp.padhobihar.domain.model.ApplicationStatus
import com.dp.padhobihar.ui.auth.AuthActivity
import com.dp.padhobihar.ui.common.AdminStudentAdapter
import com.dp.padhobihar.ui.common.CollegeAdapter
import com.dp.padhobihar.ui.common.CommissionAdapter
import com.dp.padhobihar.ui.common.UserAdapter
import com.dp.padhobihar.utils.UiState
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding
    private val viewModel: AdminViewModel by viewModels()
    private val agentAdapter = UserAdapter()
    private val collegeAdapter = CollegeAdapter()
    private val adminStudentAdapter = AdminStudentAdapter(
        onConfirm = { student -> showCommissionDialog(student.id) },
        onReject = { student -> viewModel.rejectStudent(student.id) }
    )
    private val commissionAdapter = CommissionAdapter { viewModel.markCommissionPaid(it.id) }
    private var currentTab = 0

    @Inject lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Store admin credentials for re-auth after creating agents
        val prefs = getSharedPreferences("admin_prefs", MODE_PRIVATE)
        viewModel.setAdminCredentials(
            prefs.getString("email", "") ?: "",
            prefs.getString("password", "") ?: ""
        )

        setupToolbar()
        setupRecyclerView()
        setupTabs()
        setupListeners()
        setupObservers()
        viewModel.loadAgents()
        viewModel.loadColleges()
        viewModel.loadStudents(ApplicationStatus.COLLEGE_CONFIRMED)
    }

    private fun setupToolbar() {
        binding.btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
        }
    }

    private fun setupRecyclerView() {
        binding.rvList.layoutManager = LinearLayoutManager(this)
        binding.rvList.adapter = agentAdapter
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentTab = tab?.position ?: 0
                when (currentTab) {
                    0 -> {
                        binding.rvList.adapter = agentAdapter
                        binding.fabAdd.text = "Add Agent"
                        binding.fabAdd.visibility = View.VISIBLE
                    }
                    1 -> {
                        binding.rvList.adapter = collegeAdapter
                        binding.fabAdd.text = "Add College"
                        binding.fabAdd.visibility = View.VISIBLE
                    }
                    2 -> {
                        binding.rvList.adapter = adminStudentAdapter
                        binding.fabAdd.visibility = View.GONE
                        viewModel.loadStudents(ApplicationStatus.COLLEGE_CONFIRMED)
                    }
                    3 -> {
                        binding.rvList.adapter = commissionAdapter
                        binding.fabAdd.visibility = View.GONE
                        viewModel.loadCommissions()
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) = Unit
            override fun onTabReselected(tab: TabLayout.Tab?) = Unit
        })
    }

    private fun setupListeners() {
        binding.fabAdd.setOnClickListener {
            when (currentTab) {
                0 -> showAddAgentDialog()
                1 -> showAddCollegeDialog()
            }
        }
    }

    private fun setupObservers() {
        viewModel.agentsState.observe(this) { state ->
            if (state is UiState.Success) agentAdapter.submitList(state.data)
            if (state is UiState.Error) Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
        }
        viewModel.collegesState.observe(this) { state ->
            if (state is UiState.Success) collegeAdapter.submitList(state.data)
            if (state is UiState.Error) Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
        }
        viewModel.studentsState.observe(this) { state ->
            if (state is UiState.Success) adminStudentAdapter.submitList(state.data)
            if (state is UiState.Error) Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
        }
        viewModel.commissions.observe(this) { commissionAdapter.submitList(it) }
        viewModel.counts.observe(this) { (agents, colleges, students) ->
            binding.tvAgentCount.text = agents.toString()
            binding.tvCollegeCount.text = colleges.toString()
            binding.tvStudentCount.text = students.toString()
        }
        viewModel.message.observe(this) { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
    }

    private fun showAddAgentDialog() {
        val dialogBinding = DialogAddUserBinding.inflate(layoutInflater)
        AlertDialog.Builder(this)
            .setTitle("Invite Agent")
            .setView(dialogBinding.root)
            .setPositiveButton("Send Invite") { _, _ ->
                val name = dialogBinding.etName.text.toString().trim()
                val email = dialogBinding.etEmail.text.toString().trim()
                val phone = dialogBinding.etPhone.text.toString().trim()
                val district = dialogBinding.etDistrict.text.toString().trim()
                if (name.isEmpty() || email.isEmpty()) {
                    Toast.makeText(this, "Enter name and email", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                viewModel.addAgent(name, email, phone, district)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private var selectedLocalCollege: com.dp.padhobihar.data.local.LocalCollege? = null
    private var brochureBase64: String = ""
    private var currentDialogBinding: com.dp.padhobihar.databinding.DialogAddCollegeSearchBinding? = null

    private val pickBrochure = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let {
            val bytes = contentResolver.openInputStream(it)?.readBytes() ?: return@let
            // Compress if > 800KB
            val data = if (bytes.size > 800_000) bytes.take(800_000).toByteArray() else bytes
            brochureBase64 = android.util.Base64.encodeToString(data, android.util.Base64.NO_WRAP)
            currentDialogBinding?.tvBrochureStatus?.text = "✅ Brochure attached (${data.size / 1024}KB)"
        }
    }

    private fun showAddCollegeDialog() {
        val dialogBinding = com.dp.padhobihar.databinding.DialogAddCollegeSearchBinding.inflate(layoutInflater)
        currentDialogBinding = dialogBinding
        selectedLocalCollege = null
        brochureBase64 = ""

        // Search results adapter
        val results = mutableListOf<com.dp.padhobihar.data.local.LocalCollege>()
        val rvAdapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val view = layoutInflater.inflate(R.layout.item_college_search, parent, false)
                return object : RecyclerView.ViewHolder(view) {}
            }
            override fun getItemCount() = results.size
            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val college = results[position]
                holder.itemView.findViewById<android.widget.TextView>(R.id.tvName).text = college.name
                holder.itemView.findViewById<android.widget.TextView>(R.id.tvInfo).text = "${college.state} • ${college.type}"
                holder.itemView.setOnClickListener {
                    selectedLocalCollege = college
                    dialogBinding.cardSelected.visibility = View.VISIBLE
                    dialogBinding.rvSearchResults.visibility = View.GONE
                    dialogBinding.tvSelectedName.text = college.name
                    dialogBinding.tvSelectedInfo.text = "${college.state} • ${college.type}"
                    dialogBinding.etCollegeSearch.setText(college.name)
                }
            }
        }

        dialogBinding.rvSearchResults.layoutManager = LinearLayoutManager(this)
        dialogBinding.rvSearchResults.adapter = rvAdapter

        // Search only on Enter/Search button press
        dialogBinding.etCollegeSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH ||
                actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                val query = dialogBinding.etCollegeSearch.text.toString().trim()
                if (query.length >= 3) {
                    results.clear()
                    results.addAll(com.dp.padhobihar.data.local.CollegeDataSource.search(this@AdminActivity, query))
                    rvAdapter.notifyDataSetChanged()
                    dialogBinding.rvSearchResults.visibility = if (results.isNotEmpty()) View.VISIBLE else View.GONE
                    if (results.isEmpty()) {
                        Toast.makeText(this, "No colleges found for '$query'", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Type at least 3 characters", Toast.LENGTH_SHORT).show()
                }
                // Hide keyboard
                val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                imm.hideSoftInputFromWindow(dialogBinding.etCollegeSearch.windowToken, 0)
                true
            } else false
        }

        dialogBinding.btnUploadBrochure.setOnClickListener {
            pickBrochure.launch("application/pdf")
        }

        AlertDialog.Builder(this)
            .setTitle("Add College")
            .setView(dialogBinding.root)
            .setPositiveButton("Add College") { _, _ ->
                val college = selectedLocalCollege
                if (college == null) {
                    Toast.makeText(this, "Search and select a college first", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val websiteUrl = dialogBinding.etWebsiteUrl.text.toString().trim()
                viewModel.addCollegeWithBrochure(college, websiteUrl, brochureBase64)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showCommissionDialog(studentId: String) {
        val input = android.widget.EditText(this).apply {
            hint = "Commission amount (₹)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setPadding(48, 32, 48, 32)
        }
        AlertDialog.Builder(this)
            .setTitle("Confirm Admission")
            .setMessage("Enter commission amount in ₹")
            .setView(input)
            .setPositiveButton("Confirm") { _, _ ->
                val amount = input.text.toString().toLongOrNull()
                if (amount != null && amount > 0) {
                    viewModel.confirmAdmission(studentId, amount)
                } else {
                    Toast.makeText(this, "Enter valid amount", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
