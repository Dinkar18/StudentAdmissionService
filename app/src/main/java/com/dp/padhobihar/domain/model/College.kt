package com.dp.padhobihar.domain.model

data class College(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val district: String = "",
    val type: String = "",
    val university: String = "",
    val courses: List<Course> = emptyList(),
    val creditCardAccepted: Boolean = true,
    val websiteUrl: String = "",
    val brochureData: String = "",  // base64 encoded PDF
    val approvedByAdmin: Boolean = false,
    val addedBy: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class Course(
    val name: String = "",
    val duration: String = "",
    val fees: Long = 0,
    val seatsAvailable: Int = 0,
    val eligibility: String = ""
)
