package com.example.myeduapp.data.model

import com.example.myeduapp.core.network.ApiClient
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object StudentDetailParser {
    private val knownKeys = setOf(
        "id", "user_id", "branch_id", "admission_number", "admission_date", "roll_number",
        "registration_number", "grade", "grade_label", "section", "academic_year", "academic_year_id",
        "stream", "elective_subjects", "date_of_birth", "gender", "blood_group", "religion",
        "category", "nationality", "mother_tongue", "current_address", "permanent_address",
        "city", "state", "country", "pincode", "first_name", "last_name", "email", "phone",
        "is_active", "father_name", "father_occupation", "father_phone", "father_email",
        "father_annual_income", "mother_name", "mother_occupation", "mother_phone",
        "mother_email", "mother_annual_income", "guardian_name", "guardian_relation",
        "guardian_phone", "emergency_contact_name", "emergency_contact_phone",
        "emergency_contact_relation", "previous_school", "previous_grade", "previous_percentage",
        "transfer_certificate_number", "medical_history", "allergies", "medications",
        "height_cm", "weight_kg", "profile_picture", "student_status", "admission_status",
        "remarks", "hobbies_interests", "achievements", "sports_participation", "branch",
        "created_at", "updated_at", "deleted_at", "parent_id", "documents", "school_id",
        "current_grade", "current_section", "current_grade_label", "current_academic_year",
        "current_academic_year_id", "account_is_active", "account_status_label",
        "branch_id_val", "branch_name", "branch_code", "extra_curricular_activities",
        "cultural_activities", "language_preferences", "vaccination_records", "sibling_details"
    )

    fun parse(element: JsonElement): StudentDetail {
        val strict = runCatching {
            ApiClient.jsonConfig.decodeFromJsonElement(StudentDetail.serializer(), element)
        }.getOrNull()

        val loose = parseLoose(element.jsonObject)
        val additional = additionalFields(element.jsonObject)

        return when {
            strict != null -> strict.copy(
                grade = strict.grade ?: loose.grade,
                grade_label = strict.grade_label ?: loose.grade_label,
                section = strict.section ?: loose.section,
                academic_year = strict.academic_year ?: loose.academic_year,
                additionalFields = additional
            )
            else -> loose.copy(additionalFields = additional)
        }
    }

    private fun parseLoose(obj: JsonObject): StudentDetail {
        fun s(vararg keys: String): String? =
            keys.firstNotNullOfOrNull { key -> obj[key]?.let(::jsonToDisplayString) }

        fun element(vararg keys: String): JsonElement? =
            keys.firstNotNullOfOrNull { key -> obj[key]?.takeUnless { it is JsonNull } }

        fun branch(): StudentBranch? {
            val raw = obj["branch"] ?: return null
            if (raw is JsonObject) {
                return StudentBranch(
                    id = raw["id"]?.let(::jsonToDisplayString)?.toIntOrNull(),
                    name = raw["name"]?.let(::jsonToDisplayString),
                    code = raw["code"]?.let(::jsonToDisplayString)
                )
            }
            return null
        }

        return StudentDetail(
            id = s("id"),
            user_id = s("user_id"),
            branch_id = s("branch_id"),
            admission_number = s("admission_number"),
            admission_date = s("admission_date"),
            roll_number = s("roll_number"),
            registration_number = s("registration_number"),
            grade = s("grade", "current_grade"),
            grade_label = s("grade_label", "current_grade_label"),
            section = s("section", "current_section"),
            academic_year = s("academic_year", "current_academic_year"),
            academic_year_id = s("academic_year_id", "current_academic_year_id"),
            stream = s("stream"),
            elective_subjects = element("elective_subjects"),
            date_of_birth = s("date_of_birth"),
            gender = s("gender"),
            blood_group = s("blood_group"),
            religion = s("religion"),
            category = s("category"),
            nationality = s("nationality"),
            mother_tongue = s("mother_tongue"),
            current_address = s("current_address"),
            permanent_address = s("permanent_address"),
            city = s("city"),
            state = s("state"),
            country = s("country"),
            pincode = s("pincode"),
            first_name = s("first_name"),
            last_name = s("last_name"),
            email = s("email"),
            phone = s("phone"),
            is_active = obj["is_active"]?.let(::jsonToBoolean)
                ?: obj["account_is_active"]?.let(::jsonToBoolean),
            father_name = s("father_name"),
            father_occupation = s("father_occupation"),
            father_phone = s("father_phone"),
            father_email = s("father_email"),
            father_annual_income = s("father_annual_income"),
            mother_name = s("mother_name"),
            mother_occupation = s("mother_occupation"),
            mother_phone = s("mother_phone"),
            mother_email = s("mother_email"),
            mother_annual_income = s("mother_annual_income"),
            guardian_name = s("guardian_name"),
            guardian_relation = s("guardian_relation"),
            guardian_phone = s("guardian_phone"),
            emergency_contact_name = s("emergency_contact_name"),
            emergency_contact_phone = s("emergency_contact_phone"),
            emergency_contact_relation = s("emergency_contact_relation"),
            previous_school = s("previous_school"),
            previous_grade = s("previous_grade"),
            previous_percentage = s("previous_percentage"),
            transfer_certificate_number = s("transfer_certificate_number"),
            medical_history = s("medical_history"),
            allergies = s("allergies"),
            medications = s("medications"),
            height_cm = s("height_cm"),
            weight_kg = s("weight_kg"),
            profile_picture = s("profile_picture"),
            student_status = s("student_status"),
            admission_status = s("admission_status"),
            remarks = s("remarks"),
            hobbies_interests = element("hobbies_interests"),
            achievements = element("achievements"),
            sports_participation = element("sports_participation"),
            branch = branch(),
            created_at = s("created_at"),
            updated_at = s("updated_at")
        )
    }

    private fun additionalFields(obj: JsonObject): List<Student360Field> =
        obj.entries
            .filter { (key, value) ->
                key !in knownKeys && value !is JsonNull && jsonToDisplayString(value) != null
            }
            .sortedBy { it.key }
            .map { (key, value) ->
                Student360Field(formatLabel(key), jsonToDisplayString(value) ?: STUDENT360_NA)
            }

    private fun formatLabel(key: String): String =
        key.split('_').joinToString(" ") { part ->
            part.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }

    private fun jsonToDisplayString(element: JsonElement): String? {
        return when (element) {
            is JsonNull -> null
            is JsonPrimitive -> element.content.takeIf { it.isNotBlank() && !it.equals("null", true) }
            is JsonObject -> element.entries.joinToString(", ") { (k, v) ->
                "$k: ${jsonToDisplayString(v) ?: STUDENT360_NA}"
            }.takeIf { it.isNotBlank() }
            is JsonArray -> element.joinToString(", ") { item ->
                jsonToDisplayString(item) ?: ""
            }.takeIf { it.isNotBlank() }
            else -> runCatching {
                element.toString().trim().removePrefix("\"").removeSuffix("\"")
            }.getOrNull()?.takeIf { it.isNotBlank() }
        }
    }

    private fun jsonToBoolean(element: JsonElement): Boolean? {
        if (element is JsonNull) return null
        if (element is JsonPrimitive) {
            return when (element.content.lowercase()) {
                "1", "true", "yes" -> true
                "0", "false", "no" -> false
                else -> element.content.toBooleanStrictOrNull()
            }
        }
        return null
    }
}
