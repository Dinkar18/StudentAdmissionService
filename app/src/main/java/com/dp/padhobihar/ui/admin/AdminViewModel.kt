package com.dp.padhobihar.ui.admin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dp.padhobihar.domain.model.ApplicationStatus
import com.dp.padhobihar.domain.model.College
import com.dp.padhobihar.domain.model.Commission
import com.dp.padhobihar.domain.model.Student
import com.dp.padhobihar.domain.model.User
import com.dp.padhobihar.domain.repository.CollegeRepository
import com.dp.padhobihar.domain.repository.CommissionRepository
import com.dp.padhobihar.domain.repository.StudentRepository
import com.dp.padhobihar.domain.repository.UserRepository
import com.dp.padhobihar.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val studentRepository: StudentRepository,
    private val collegeRepository: CollegeRepository,
    private val commissionRepository: CommissionRepository
) : ViewModel() {

    private val _agentsState = MutableLiveData<UiState<List<User>>>()
    val agentsState: LiveData<UiState<List<User>>> = _agentsState

    private val _collegesState = MutableLiveData<UiState<List<College>>>()
    val collegesState: LiveData<UiState<List<College>>> = _collegesState

    private val _studentsState = MutableLiveData<UiState<List<Student>>>()
    val studentsState: LiveData<UiState<List<Student>>> = _studentsState

    private val _counts = MutableLiveData<Triple<Int, Int, Int>>()
    val counts: LiveData<Triple<Int, Int, Int>> = _counts

    private val _commissions = MutableLiveData<List<Commission>>()
    val commissions: LiveData<List<Commission>> = _commissions

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    fun loadAgents() {
        Timber.d("loadAgents called")
        _agentsState.value = UiState.Loading
        viewModelScope.launch {
            userRepository.getUsersByRole("AGENT").onSuccess {
                Timber.d("loadAgents success: ${it.size} agents")
                _agentsState.value = UiState.Success(it)
                refreshCounts()
            }.onFailure {
                Timber.e(it, "loadAgents failed")
                _agentsState.value = UiState.Error(it.message ?: "Failed to load agents")
            }
        }
    }

    fun loadColleges() {
        Timber.d("loadColleges called")
        _collegesState.value = UiState.Loading
        viewModelScope.launch {
            collegeRepository.getActiveColleges().onSuccess {
                Timber.d("loadColleges success: ${it.size} colleges")
                _collegesState.value = UiState.Success(it)
                refreshCounts()
            }.onFailure {
                Timber.e(it, "loadColleges failed")
                _collegesState.value = UiState.Error(it.message ?: "Failed to load colleges")
            }
        }
    }

    fun loadStudents(filterStatus: ApplicationStatus? = null) {
        Timber.d("loadStudents called")
        _studentsState.value = UiState.Loading
        viewModelScope.launch {
            studentRepository.getAllStudents().onSuccess { all ->
                val list = if (filterStatus != null) all.filter { it.status == filterStatus } else all
                Timber.d("loadStudents success: ${list.size} students")
                _studentsState.value = UiState.Success(list)
                refreshCounts()
            }.onFailure {
                Timber.e(it, "loadStudents failed")
                _studentsState.value = UiState.Error(it.message ?: "Failed to load students")
            }
        }
    }

    private fun refreshCounts() {
        _counts.value = Triple(
            (_agentsState.value as? UiState.Success)?.data?.size ?: 0,
            (_collegesState.value as? UiState.Success)?.data?.size ?: 0,
            (_studentsState.value as? UiState.Success)?.data?.size ?: 0
        )
    }

    private var adminEmail: String = ""
    private var adminPassword: String = ""

    fun setAdminCredentials(email: String, password: String) {
        adminEmail = email
        adminPassword = password
    }

    fun addAgent(name: String, email: String, phone: String, district: String) {
        Timber.d("addAgent called")
        viewModelScope.launch {
            try {
                val firebaseAuth = com.google.firebase.auth.FirebaseAuth.getInstance()
                val randomPassword = java.util.UUID.randomUUID().toString()
                val authResult = firebaseAuth.createUserWithEmailAndPassword(email, randomPassword).await()
                val uid = authResult.user?.uid ?: throw Exception("Failed to create account")

                val referralCode = name.take(4).uppercase() + phone.takeLast(4)
                val user = User(id = uid, name = name, phone = phone, role = "AGENT", district = district, referralCode = referralCode)
                userRepository.createUser(user)

                firebaseAuth.signOut()
                firebaseAuth.sendPasswordResetEmail(email).await()

                if (adminEmail.isNotEmpty()) {
                    firebaseAuth.signInWithEmailAndPassword(adminEmail, adminPassword).await()
                }

                Timber.d("addAgent success: $name")
                _message.value = "Agent added! Password setup email sent to $email"
                loadAgents()
            } catch (e: Exception) {
                Timber.e(e, "addAgent failed")
                if (adminEmail.isNotEmpty()) {
                    try {
                        com.google.firebase.auth.FirebaseAuth.getInstance()
                            .signInWithEmailAndPassword(adminEmail, adminPassword).await()
                    } catch (_: Exception) {}
                }
                _message.value = e.message ?: "Failed to add agent"
            }
        }
    }

    fun searchColleges(context: android.content.Context, query: String): List<com.dp.padhobihar.data.local.LocalCollege> {
        Timber.d("searchColleges called: query=$query")
        return com.dp.padhobihar.data.local.CollegeDataSource.search(context, query)
    }

    fun addCollegeWithBrochure(
        localCollege: com.dp.padhobihar.data.local.LocalCollege,
        websiteUrl: String,
        brochureBase64: String
    ) {
        Timber.d("addCollegeWithBrochure called: ${localCollege.name}")
        viewModelScope.launch {
            val college = College(
                id = localCollege.id,
                name = localCollege.name,
                district = localCollege.state,
                type = localCollege.type,
                university = localCollege.university,
                websiteUrl = websiteUrl,
                brochureData = brochureBase64,
                creditCardAccepted = true,
                approvedByAdmin = true
            )
            collegeRepository.addCollege(college).onSuccess {
                Timber.d("addCollegeWithBrochure success")
                _message.value = "${localCollege.name} added!"
                loadColleges()
            }.onFailure {
                Timber.e(it, "addCollegeWithBrochure failed")
                _message.value = it.message ?: "Failed to add college"
            }
        }
    }

    fun confirmAdmission(studentId: String, commissionAmount: Long) {
        Timber.d("confirmAdmission called")
        viewModelScope.launch {
            try {
                val student = (_studentsState.value as? UiState.Success)?.data?.find { it.id == studentId }
                val agentId = student?.agentId ?: ""
                val collegeId = student?.confirmedCollegeId ?: ""

                studentRepository.updateStatus(studentId, ApplicationStatus.ADMITTED)

                val adminShare = (commissionAmount * 0.30).toLong()
                val commission = hashMapOf<String, Any>(
                    "studentId" to studentId,
                    "agentId" to agentId,
                    "collegeId" to collegeId,
                    "totalAmount" to commissionAmount,
                    "adminShare" to adminShare,
                    "agentShare" to (commissionAmount - adminShare),
                    "status" to "PENDING",
                    "createdAt" to System.currentTimeMillis()
                )
                commissionRepository.create(commission)

                Timber.d("confirmAdmission success")
                _message.value = "Admission confirmed! Commission ₹$commissionAmount created."
                loadStudents()
            } catch (e: Exception) {
                Timber.e(e, "confirmAdmission failed")
                _message.value = e.message ?: "Failed"
            }
        }
    }

    fun rejectStudent(studentId: String) {
        Timber.d("rejectStudent called")
        viewModelScope.launch {
            studentRepository.updateStatus(studentId, ApplicationStatus.REJECTED).onSuccess {
                Timber.d("rejectStudent success")
                _message.value = "Student rejected"
                loadStudents()
            }.onFailure {
                Timber.e(it, "rejectStudent failed")
                _message.value = it.message
            }
        }
    }

    fun loadCommissions() {
        Timber.d("loadCommissions called")
        viewModelScope.launch {
            commissionRepository.getAll().onSuccess {
                Timber.d("loadCommissions success: ${it.size} commissions")
                _commissions.value = it
            }.onFailure {
                Timber.e(it, "loadCommissions failed")
                _message.value = it.message
            }
        }
    }

    fun markCommissionPaid(commissionId: String) {
        Timber.d("markCommissionPaid called")
        viewModelScope.launch {
            commissionRepository.markPaid(commissionId).onSuccess {
                Timber.d("markCommissionPaid success")
                _message.value = "Marked as PAID"
                loadCommissions()
            }.onFailure {
                Timber.e(it, "markCommissionPaid failed")
                _message.value = it.message ?: "Failed"
            }
        }
    }
}
