package com.dp.padhobihar.ui.agent

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
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AgentViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val collegeRepository: CollegeRepository,
    private val userRepository: UserRepository,
    private val commissionRepository: CommissionRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val myId get() = auth.currentUser?.uid ?: ""

    private val _studentsState = MutableLiveData<UiState<List<Student>>>()
    val studentsState: LiveData<UiState<List<Student>>> = _studentsState

    private val _collegesState = MutableLiveData<UiState<List<College>>>()
    val collegesState: LiveData<UiState<List<College>>> = _collegesState

    private val _commissions = MutableLiveData<List<Commission>>()
    val commissions: LiveData<List<Commission>> = _commissions

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    private val _counts = MutableLiveData<Triple<Int, Int, Int>>()
    val counts: LiveData<Triple<Int, Int, Int>> = _counts

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    fun loadStudents() {
        Timber.d("loadStudents called")
        _studentsState.value = UiState.Loading
        viewModelScope.launch {
            studentRepository.getStudentsByAgent(myId).onSuccess { list ->
                Timber.d("loadStudents success: ${list.size} students")
                _studentsState.value = UiState.Success(list)
                val admitted = list.count { it.status == ApplicationStatus.ADMITTED }
                _counts.value = Triple(list.size, list.size - admitted, admitted)
            }.onFailure {
                Timber.e(it, "loadStudents failed")
                _studentsState.value = UiState.Error(it.message ?: "Failed to load students")
            }
        }
    }

    fun updateStudentStatus(studentId: String, newStatus: ApplicationStatus) {
        Timber.d("updateStudentStatus called")
        viewModelScope.launch {
            studentRepository.updateStatus(studentId, newStatus).onSuccess {
                Timber.d("updateStudentStatus success")
                _message.value = "Status updated"
                loadStudents()
            }.onFailure {
                Timber.e(it, "updateStudentStatus failed")
                _message.value = it.message
            }
        }
    }

    fun approveCollegeRequest(studentId: String) {
        Timber.d("approveCollegeRequest called")
        viewModelScope.launch {
            val student = (_studentsState.value as? UiState.Success)?.data?.find { it.id == studentId }
            val fields = mapOf(
                "confirmedCollegeId" to (student?.requestedCollegeId ?: ""),
                "status" to ApplicationStatus.COLLEGE_CONFIRMED.name
            )
            studentRepository.updateFields(studentId, fields).onSuccess {
                Timber.d("approveCollegeRequest success")
                _message.value = "College approved!"
                loadStudents()
            }.onFailure {
                Timber.e(it, "approveCollegeRequest failed")
                _message.value = it.message
            }
        }
    }

    fun suggestCollege(studentId: String, collegeId: String) {
        Timber.d("suggestCollege called")
        viewModelScope.launch {
            val fields = mapOf(
                "confirmedCollegeId" to collegeId,
                "suggestedCollegeId" to collegeId,
                "status" to ApplicationStatus.COLLEGE_SUGGESTED.name
            )
            studentRepository.updateFields(studentId, fields).onSuccess {
                Timber.d("suggestCollege success")
                _message.value = "College suggestion sent to student"
                loadStudents()
            }.onFailure {
                Timber.e(it, "suggestCollege failed")
                _message.value = it.message
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
            }.onFailure {
                Timber.e(it, "loadColleges failed")
                _collegesState.value = UiState.Error(it.message ?: "Failed to load colleges")
            }
        }
    }

    fun loadEarnings() {
        Timber.d("loadEarnings called")
        viewModelScope.launch {
            commissionRepository.getByAgent(myId).onSuccess {
                Timber.d("loadEarnings success: ${it.size} commissions")
                _commissions.value = it
            }.onFailure {
                Timber.e(it, "loadEarnings failed")
                _message.value = it.message
            }
        }
    }

    fun loadProfile() {
        Timber.d("loadProfile called")
        viewModelScope.launch {
            _user.value = userRepository.getUser(myId).getOrNull()
            Timber.d("loadProfile success")
        }
    }
}
