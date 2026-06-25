package com.dp.padhobihar.domain.model

data class Student(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val phone: String = "",
    val fatherName: String = "",
    val village: String = "",
    val district: String = "",
    val qualification: Qualification = Qualification.TWELFTH,
    val marks: Float = 0f,
    val courseInterest: String = "",
    val agentId: String = "",
    val agentCode: String = "",
    val requestedCollegeId: String = "",
    val suggestedCollegeId: String = "",
    val confirmedCollegeId: String = "",
    val status: ApplicationStatus = ApplicationStatus.REGISTERED,
    val documents: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
