package com.example.myeduapp.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

const val STUDENT360_NA = "N/A"

@Serializable
data class StudentDetailResponse(
    val success: Boolean,
    val data: StudentDetail? = null,
    val message: String? = null
)

@Serializable
data class StudentBranch(
    val id: Int? = null,
    val name: String? = null,
    val code: String? = null
)

@Serializable
data class StudentDetail(
    @Serializable(with = JsonFlexibleIdSerializer::class)
    val id: String? = null,
    @Serializable(with = JsonFlexibleIdSerializer::class)
    val user_id: String? = null,
    @Serializable(with = JsonFlexibleIdSerializer::class)
    val branch_id: String? = null,
    val admission_number: String? = null,
    val admission_date: String? = null,
    val roll_number: String? = null,
    val registration_number: String? = null,
    val grade: String? = null,
    val grade_label: String? = null,
    val section: String? = null,
    val academic_year: String? = null,
    @Serializable(with = JsonFlexibleIdSerializer::class)
    val academic_year_id: String? = null,
    val stream: String? = null,
    val elective_subjects: JsonElement? = null,
    val date_of_birth: String? = null,
    val gender: String? = null,
    val blood_group: String? = null,
    val religion: String? = null,
    val category: String? = null,
    val nationality: String? = null,
    val mother_tongue: String? = null,
    val current_address: String? = null,
    val permanent_address: String? = null,
    val city: String? = null,
    val state: String? = null,
    val country: String? = null,
    @Serializable(with = JsonFlexibleStringSerializer::class)
    val pincode: String? = null,
    val first_name: String? = null,
    val last_name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    @Serializable(with = JsonFlexibleBooleanSerializer::class)
    val is_active: Boolean? = null,
    val father_name: String? = null,
    val father_occupation: String? = null,
    val father_phone: String? = null,
    val father_email: String? = null,
    @Serializable(with = JsonFlexibleStringSerializer::class)
    val father_annual_income: String? = null,
    val mother_name: String? = null,
    val mother_occupation: String? = null,
    val mother_phone: String? = null,
    val mother_email: String? = null,
    @Serializable(with = JsonFlexibleStringSerializer::class)
    val mother_annual_income: String? = null,
    val guardian_name: String? = null,
    val guardian_relation: String? = null,
    val guardian_phone: String? = null,
    val emergency_contact_name: String? = null,
    val emergency_contact_phone: String? = null,
    val emergency_contact_relation: String? = null,
    val previous_school: String? = null,
    val previous_grade: String? = null,
    @Serializable(with = JsonFlexibleStringSerializer::class)
    val previous_percentage: String? = null,
    val transfer_certificate_number: String? = null,
    val medical_history: String? = null,
    val allergies: String? = null,
    val medications: String? = null,
    @Serializable(with = JsonFlexibleStringSerializer::class)
    val height_cm: String? = null,
    @Serializable(with = JsonFlexibleStringSerializer::class)
    val weight_kg: String? = null,
    val profile_picture: String? = null,
    val student_status: String? = null,
    val admission_status: String? = null,
    val remarks: String? = null,
    val hobbies_interests: JsonElement? = null,
    val achievements: JsonElement? = null,
    val sports_participation: JsonElement? = null,
    val branch: StudentBranch? = null,
    val created_at: String? = null,
    val updated_at: String? = null,
    @Transient val additionalFields: List<Student360Field> = emptyList()
) {
    val fullName: String
        get() = listOfNotNull(first_name, last_name)
            .joinToString(" ")
            .ifBlank { STUDENT360_NA }

    val classLabel: String
        get() = buildString {
            val gradeText = grade_label?.takeIf { it.isNotBlank() }
                ?: grade?.takeIf { it.isNotBlank() }?.let { "Grade $it" }
            val sectionText = section?.takeIf { it.isNotBlank() }
            when {
                gradeText != null && sectionText != null -> append("$gradeText • Section $sectionText")
                gradeText != null -> append(gradeText)
                sectionText != null -> append("Section $sectionText")
                else -> append(STUDENT360_NA)
            }
        }

    fun profileSections(): List<Student360Section> = listOf(
        Student360Section(
            title = "Academic",
            icon = "school",
            fields = listOf(
                field("Admission No.", admission_number),
                field("Admission Date", admission_date),
                field("Roll Number", roll_number),
                field("Registration No.", registration_number),
                field("Class", grade_label ?: grade),
                field("Section", section),
                field("Academic Year", academic_year),
                field("Stream", stream),
                field("Elective Subjects", elective_subjects.display()),
                field("Branch", branch?.name ?: branch?.code),
                field("Student Status", student_status),
                field("Admission Status", admission_status)
            )
        ),
        Student360Section(
            title = "Personal",
            icon = "person",
            fields = listOf(
                field("Full Name", fullName.takeUnless { it == STUDENT360_NA }),
                field("Date of Birth", date_of_birth),
                field("Gender", gender),
                field("Blood Group", blood_group),
                field("Religion", religion),
                field("Category", category),
                field("Nationality", nationality),
                field("Mother Tongue", mother_tongue)
            )
        ),
        Student360Section(
            title = "Contact",
            icon = "contact",
            fields = listOf(
                field("Email", email),
                field("Phone", phone),
                field("Account Active", is_active?.let { if (it) "Yes" else "No" }),
                field("Current Address", current_address),
                field("Permanent Address", permanent_address),
                field("City", city),
                field("State", state),
                field("Country", country),
                field("Pincode", pincode)
            )
        ),
        Student360Section(
            title = "Father",
            icon = "family",
            fields = listOf(
                field("Name", father_name),
                field("Phone", father_phone),
                field("Email", father_email),
                field("Occupation", father_occupation),
                field("Annual Income", father_annual_income)
            )
        ),
        Student360Section(
            title = "Mother",
            icon = "family",
            fields = listOf(
                field("Name", mother_name),
                field("Phone", mother_phone),
                field("Email", mother_email),
                field("Occupation", mother_occupation),
                field("Annual Income", mother_annual_income)
            )
        ),
        Student360Section(
            title = "Guardian",
            icon = "guardian",
            fields = listOf(
                field("Name", guardian_name),
                field("Relation", guardian_relation),
                field("Phone", guardian_phone)
            )
        ),
        Student360Section(
            title = "Emergency Contact",
            icon = "emergency",
            fields = listOf(
                field("Name", emergency_contact_name),
                field("Phone", emergency_contact_phone),
                field("Relation", emergency_contact_relation)
            )
        ),
        Student360Section(
            title = "Previous Education",
            icon = "history",
            fields = listOf(
                field("Previous School", previous_school),
                field("Previous Grade", previous_grade),
                field("Previous %", previous_percentage),
                field("TC Number", transfer_certificate_number)
            )
        ),
        Student360Section(
            title = "Medical",
            icon = "medical",
            fields = listOf(
                field("Medical History", medical_history),
                field("Allergies", allergies),
                field("Medications", medications),
                field("Height (cm)", height_cm),
                field("Weight (kg)", weight_kg)
            )
        ),
        Student360Section(
            title = "Activities & Notes",
            icon = "star",
            fields = listOf(
                field("Hobbies & Interests", hobbies_interests.display()),
                field("Achievements", achievements.display()),
                field("Sports", sports_participation.display()),
                field("Remarks", remarks),
                field("Profile Photo", profile_picture?.let { if (it.isBlank()) null else "Uploaded" })
            )
        )
    ).let { sections ->
        if (additionalFields.isEmpty()) sections
        else sections + Student360Section(
            title = "Additional Information",
            icon = "info",
            fields = additionalFields
        )
    }
}

data class Student360Section(
    val title: String,
    val icon: String,
    val fields: List<Student360Field>
)

data class Student360Field(
    val label: String,
    val value: String
)

private fun field(label: String, raw: String?): Student360Field =
    Student360Field(label, raw.displayOrNa())

private fun String?.displayOrNa(): String =
    this?.trim()?.takeIf { it.isNotBlank() && !it.equals("null", ignoreCase = true) } ?: STUDENT360_NA

private fun JsonElement?.display(): String? {
    if (this == null) return null
    return try {
        if (this is JsonPrimitive) {
            return this.content.takeIf { it.isNotBlank() }
        }
        this.jsonArray.joinToString(", ") { element ->
            runCatching { element.jsonPrimitive.content }.getOrElse { element.toString() }
        }.ifBlank { null }
    } catch (_: Exception) {
        this.toString().takeIf { it.isNotBlank() }
    }
}

fun Student.mergeWithDetail(detail: StudentDetail?): Student {
    if (detail == null) return this
    return Student(
        id = detail.id ?: id,
        user_id = detail.user_id ?: user_id,
        first_name = detail.first_name ?: first_name,
        last_name = detail.last_name ?: last_name,
        admission_number = detail.admission_number ?: admission_number,
        roll_number = detail.roll_number ?: roll_number,
        grade = detail.grade ?: grade ?: current_grade,
        section = detail.section ?: section ?: current_section,
        avatar = detail.profile_picture ?: avatar
    )
}

fun String.toApiIntId(): Int? = trim().toIntOrNull()

fun resolveStudentUserId(student: Student, detail: StudentDetail? = null): Int? =
    (detail?.user_id ?: student.user_id).toApiIntId()
        ?: student.attendanceUserId.toApiIntId()

fun StudentDetail.toStudent(): Student = Student(
    id = id ?: "0",
    user_id = user_id ?: "0",
    first_name = first_name ?: "",
    last_name = last_name ?: "",
    admission_number = admission_number ?: STUDENT360_NA,
    roll_number = roll_number,
    grade = grade,
    section = section,
    grade_label = grade_label,
    email = email,
    phone = phone,
    gender = gender,
    student_status = student_status,
    avatar = profile_picture
)

fun User.toStudentSeed(): Student = Student(
    id = user_type_id?.toString() ?: id.toString(),
    user_id = id.toString(),
    first_name = first_name,
    last_name = last_name,
    admission_number = employee_id ?: STUDENT360_NA,
    email = email,
    phone = phone,
    gender = gender,
    avatar = avatar
)

fun Student.toDetailFallback(): StudentDetail = StudentDetail(
    id = id,
    user_id = user_id,
    first_name = first_name,
    last_name = last_name,
    admission_number = admission_number,
    roll_number = roll_number,
    grade = displayGrade,
    section = displaySection,
    grade_label = grade_label,
    email = email,
    phone = phone,
    student_status = student_status,
    profile_picture = avatar
)
