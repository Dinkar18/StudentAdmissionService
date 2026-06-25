package com.dp.padhobihar.ui.student

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dp.padhobihar.domain.model.ApplicationStatus
import com.dp.padhobihar.domain.model.College
import com.dp.padhobihar.domain.model.Student
import com.dp.padhobihar.domain.model.User
import com.dp.padhobihar.domain.repository.AuthRepository
import com.dp.padhobihar.domain.repository.CollegeRepository
import com.dp.padhobihar.domain.repository.StudentRepository
import com.dp.padhobihar.domain.repository.UserRepository
import com.dp.padhobihar.utils.RateLimiter
import com.dp.padhobihar.utils.UiState
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class StudentViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val studentRepository: StudentRepository,
    private val collegeRepository: CollegeRepository,
    private val firestore: FirebaseFirestore
) : ViewModel() {
    private val uid get() = authRepository.getCurrentUid() ?: ""

    private val _registerState = MutableLiveData<RegisterState>()
    val registerState: LiveData<RegisterState> = _registerState
    private val _student = MutableLiveData<Student?>()
    val student: LiveData<Student?> = _student
    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user
    private val _profile = MutableLiveData<Map<String, Any>?>()
    val profile: LiveData<Map<String, Any>?> = _profile
    private val _collegesState = MutableLiveData<UiState<List<College>>>()
    val collegesState: LiveData<UiState<List<College>>> = _collegesState
    private val _documents = MutableLiveData<Set<String>>()
    val documents: LiveData<Set<String>> = _documents
    private val _agentInfo = MutableLiveData<User?>()
    val agentInfo: LiveData<User?> = _agentInfo
    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    fun register(name: String, email: String, password: String, phone: String, referralCode: String) {
        Timber.d("register called")
        if (!RateLimiter.canProceed(email)) {
            _registerState.value = RegisterState.Error("Too many attempts. Try again in ${RateLimiter.remainingSeconds(email)} seconds.")
            return
        }
        RateLimiter.record(email)
        _registerState.value = RegisterState.Loading
        viewModelScope.launch {
            try {
                val uid = authRepository.registerWithEmail(email, password).getOrElse {
                    _registerState.value = RegisterState.Error(it.message ?: "Registration failed"); return@launch
                }
                val agentDoc = firestore.collection("users")
                    .whereEqualTo("referralCode", referralCode)
                    .whereEqualTo("role", "AGENT").limit(1).get().await()
                if (agentDoc.isEmpty) {
                    com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.delete()?.await()
                    _registerState.value = RegisterState.Error("Invalid referral code. Ask your agent."); return@launch
                }
                val agentId = agentDoc.documents.first().id
                userRepository.createUser(User(id = uid, name = name, phone = phone, role = "STUDENT", status = "ACTIVE"))
                firestore.collection("students").add(hashMapOf(
                    "userId" to uid, "name" to name, "phone" to phone, "agentId" to agentId,
                    "agentCode" to referralCode, "status" to "REGISTERED", "createdAt" to System.currentTimeMillis()
                )).await()
                Timber.d("register success")
                _registerState.value = RegisterState.VerificationSent
            } catch (e: Exception) {
                Timber.e(e, "register failed")
                _registerState.value = RegisterState.Error(e.message ?: "Registration failed")
            }
        }
    }

    fun checkEmailVerified() {
        Timber.d("checkEmailVerified called")
        _registerState.value = RegisterState.Loading
        viewModelScope.launch {
            try {
                val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                auth.currentUser?.reload()?.await()
                if (auth.currentUser?.isEmailVerified == true) {
                    Timber.d("checkEmailVerified success: verified")
                    _registerState.value = RegisterState.Verified
                } else {
                    _registerState.value = RegisterState.Error("Email not verified yet. Check your inbox.")
                }
            } catch (e: Exception) {
                Timber.e(e, "checkEmailVerified failed")
                _registerState.value = RegisterState.Error(e.message ?: "Check failed")
            }
        }
    }

    fun resendVerification() {
        Timber.d("resendVerification called")
        viewModelScope.launch {
            authRepository.sendEmailVerification()
            Timber.d("resendVerification success")
            _message.value = "Verification email sent"
        }
    }

    fun loadStudentData() {
        Timber.d("loadStudentData called")
        viewModelScope.launch {
            _user.value = userRepository.getUser(uid).getOrNull()
            _student.value = studentRepository.getStudentByUserId(uid).getOrNull()
            Timber.d("loadStudentData success")
        }
    }

    fun loadAgentInfo() {
        Timber.d("loadAgentInfo called")
        viewModelScope.launch {
            try {
                val agentId = studentRepository.getStudentByUserId(uid).getOrNull()?.agentId ?: return@launch
                val doc = firestore.collection("users").document(agentId).get().await()
                _agentInfo.value = doc.toObject<User>()?.copy(id = doc.id)
                Timber.d("loadAgentInfo success")
            } catch (e: Exception) { Timber.e(e, "loadAgentInfo failed") }
        }
    }

    fun loadProfile() {
        Timber.d("loadProfile called")
        viewModelScope.launch {
            _user.value = userRepository.getUser(uid).getOrNull()
            try {
                _profile.value = firestore.collection("student_profiles").document(uid).get().await().data
                Timber.d("loadProfile success")
            } catch (e: Exception) { Timber.e(e, "loadProfile failed") }
        }
    }

    fun saveProfile(data: Map<String, Any>) {
        Timber.d("saveProfile called")
        viewModelScope.launch {
            try {
                firestore.collection("student_profiles").document(uid).set(data).await()
                _user.value?.copy(name = data["name"] as? String ?: "", phone = data["phone"] as? String ?: "",
                    district = data["district"] as? String ?: "")?.let { userRepository.updateUser(it) }
                val student = studentRepository.getStudentByUserId(uid).getOrNull() ?: return@launch
                studentRepository.updateFields(student.id, mapOf(
                    "name" to (data["name"] ?: ""), "phone" to (data["phone"] ?: ""),
                    "fatherName" to (data["fatherName"] ?: ""), "village" to (data["village"] ?: ""),
                    "district" to (data["district"] ?: ""), "qualification" to (data["qualification"] ?: ""),
                    "marks" to (data["marks"] ?: 0f), "courseInterest" to (data["courseInterest"] ?: "")
                ))
                if (student.status == ApplicationStatus.REGISTERED)
                    studentRepository.updateStatus(student.id, ApplicationStatus.PROFILE_COMPLETE)
                Timber.d("saveProfile success")
                _message.value = "Profile saved!"; loadStudentData()
            } catch (e: Exception) { Timber.e(e, "saveProfile failed"); _message.value = e.message ?: "Save failed" }
        }
    }

    private val allDocs = listOf("10th_marksheet", "12th_marksheet", "aadhaar", "income_certificate", "caste_certificate", "domicile", "photo", "bank_passbook")
    private val requiredDocs = listOf("10th_marksheet", "aadhaar", "photo")

    fun loadDocuments() {
        Timber.d("loadDocuments called")
        viewModelScope.launch {
            try {
                _documents.value = allDocs.filter {
                    firestore.collection("student_documents").document("${uid}_${it}").get().await().exists()
                }.toSet()
                Timber.d("loadDocuments success: ${_documents.value?.size} docs")
            } catch (e: Exception) { Timber.e(e, "loadDocuments failed"); _documents.value = emptySet() }
        }
    }

    fun uploadDocument(type: String, uri: Uri) {
        Timber.d("uploadDocument called: type=$type")
        viewModelScope.launch {
            try {
                _message.value = "Uploading..."
                val context = com.google.firebase.FirebaseApp.getInstance().applicationContext
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: throw Exception("Cannot open file")
                val base64 = android.util.Base64.encodeToString(compressImage(bytes), android.util.Base64.NO_WRAP)
                firestore.collection("student_documents").document("${uid}_${type}")
                    .set(mapOf("data" to base64, "type" to type, "userId" to uid)).await()
                Timber.d("uploadDocument success: $type")
                _message.value = "${type.replaceFirstChar { it.uppercase() }} uploaded!"
                checkRequiredDocsStatus(); loadDocuments(); loadStudentData()
            } catch (e: Exception) { Timber.e(e, "uploadDocument failed"); _message.value = "Upload failed: ${e.message}" }
        }
    }

    private suspend fun checkRequiredDocsStatus() {
        val allUploaded = requiredDocs.all { firestore.collection("student_documents").document("${uid}_${it}").get().await().exists() }
        if (!allUploaded) return
        val student = studentRepository.getStudentByUserId(uid).getOrNull() ?: return
        if (student.status == ApplicationStatus.PROFILE_COMPLETE || student.status == ApplicationStatus.REGISTERED) {
            studentRepository.updateStatus(student.id, ApplicationStatus.DOCS_UPLOADED)
            _message.value = "Required documents uploaded! You can now select a college."
        }
    }

    private fun compressImage(bytes: ByteArray): ByteArray {
        val bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            ?: return if (bytes.size > 200_000) bytes.copyOfRange(0, 200_000) else bytes
        val stream = java.io.ByteArrayOutputStream()
        var quality = 50
        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, quality, stream)
        while (stream.size() > 200_000 && quality > 10) { stream.reset(); quality -= 10; bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, quality, stream) }
        return stream.toByteArray()
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

    fun requestCollege(collegeId: String) {
        Timber.d("requestCollege called")
        viewModelScope.launch {
            try {
                val student = studentRepository.getStudentByUserId(uid).getOrNull() ?: return@launch
                studentRepository.updateFields(student.id, mapOf("requestedCollegeId" to collegeId, "status" to "COLLEGE_REQUESTED"))
                Timber.d("requestCollege success")
                _message.value = "College request sent to your agent!"; loadStudentData()
            } catch (e: Exception) {
                Timber.e(e, "requestCollege failed")
                _message.value = e.message ?: "Failed"
            }
        }
    }

    fun acceptSuggestedCollege() {
        Timber.d("acceptSuggestedCollege called")
        viewModelScope.launch {
            try {
                val student = studentRepository.getStudentByUserId(uid).getOrNull() ?: return@launch
                val suggestedId = student.suggestedCollegeId.ifEmpty { return@launch }
                studentRepository.updateFields(student.id, mapOf("confirmedCollegeId" to suggestedId, "status" to "COLLEGE_CONFIRMED"))
                Timber.d("acceptSuggestedCollege success")
                _message.value = "College confirmed!"; loadStudentData()
            } catch (e: Exception) {
                Timber.e(e, "acceptSuggestedCollege failed")
                _message.value = e.message ?: "Failed"
            }
        }
    }

    fun withdraw() {
        Timber.d("withdraw called")
        viewModelScope.launch {
            try {
                val student = studentRepository.getStudentByUserId(uid).getOrNull() ?: return@launch
                studentRepository.updateFields(student.id, mapOf("status" to "WITHDRAWN", "requestedCollegeId" to "", "suggestedCollegeId" to "", "confirmedCollegeId" to ""))
                Timber.d("withdraw success")
                _message.value = "Application withdrawn"; loadStudentData()
            } catch (e: Exception) {
                Timber.e(e, "withdraw failed")
                _message.value = e.message ?: "Failed"
            }
        }
    }
}

sealed class RegisterState {
    data object Loading : RegisterState()
    data object VerificationSent : RegisterState()
    data object Verified : RegisterState()
    data class Error(val message: String) : RegisterState()
}
