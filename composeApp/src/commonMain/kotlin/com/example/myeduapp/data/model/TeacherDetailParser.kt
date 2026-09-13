package com.example.myeduapp.data.model

import com.example.myeduapp.core.network.ApiClient
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

object TeacherDetailParser {
    private val knownKeys = setOf(
        "id", "user_id", "branch_id", "employee_id", "designation", "department",
        "category_type", "teacher_status", "contract_type", "work_shift", "gender",
        "blood_group", "religion", "marital_status", "father_name", "husband_name",
        "mother_name", "national_id", "aadhaar_number", "pan_number", "class_teacher_of_grade",
        "class_teacher_of_section", "qualification", "experience", "specialization",
        "subjects", "joining_date", "basic_salary", "salary", "bank_name", "account_title",
        "bank_account_number", "ifsc_code", "resume", "id_proof", "documents", "attachments",
        "files", "certificates", "is_active", "user", "branch", "first_name", "last_name",
        "name", "email", "phone", "alternate_phone", "emergency_contact", "avatar",
        "profile_picture", "address", "current_address", "permanent_address", "city",
        "state", "pincode", "dob", "date_of_birth", "created_at", "updated_at", "deleted_at"
    )

    fun parseResponse(body: String): TeacherDetailResponse {
        return try {
            val element = ApiClient.jsonConfig.parseToJsonElement(body)
            if (element is JsonObject) {
                val success = element["success"]?.let(::jsonToBoolean)
                    ?: element["status"]?.let(::jsonToBoolean)
                    ?: true
                val message = element["message"]?.let(::jsonToDisplayString)
                val dataObj = element["data"] ?: element["teacher"] ?: element
                val teacher = if (dataObj !is JsonNull) parseTeacher(dataObj) else null
                TeacherDetailResponse(success = success, data = teacher, message = message)
            } else {
                TeacherDetailResponse(success = false, data = null, message = "Invalid response")
            }
        } catch (e: Exception) {
            TeacherDetailResponse(success = false, data = null, message = e.message ?: "Failed to parse teacher details")
        }
    }

    fun parseTeacher(element: JsonElement): Teacher {
        val obj = (element as? JsonObject) ?: return Teacher()
        val strict = runCatching {
            ApiClient.jsonConfig.decodeFromJsonElement(Teacher.serializer(), element)
        }.getOrNull()

        val loose = parseLoose(obj)
        val additional = additionalFields(obj)

        val base = strict ?: loose
        val mergedUser = mergeUser(base.user, obj)
        val mergedAttachments = extractAttachments(obj, base.documents + base.attachments)

        return base.copy(
            user = mergedUser,
            documents = mergedAttachments,
            additionalFields = additional
        )
    }

    private fun mergeUser(user: TeacherUserBrief?, obj: JsonObject): TeacherUserBrief {
        fun s(vararg keys: String): String? =
            keys.firstNotNullOfOrNull { key -> obj[key]?.let(::jsonToDisplayString) }

        val userObj = obj["user"] as? JsonObject

        return TeacherUserBrief(
            id = user?.id ?: userObj?.get("id")?.let(::jsonToDisplayString) ?: s("user_id"),
            first_name = user?.first_name ?: userObj?.get("first_name")?.let(::jsonToDisplayString) ?: s("first_name"),
            last_name = user?.last_name ?: userObj?.get("last_name")?.let(::jsonToDisplayString) ?: s("last_name"),
            name = user?.name ?: userObj?.get("name")?.let(::jsonToDisplayString) ?: s("name"),
            email = user?.email ?: userObj?.get("email")?.let(::jsonToDisplayString) ?: s("email"),
            phone = user?.phone ?: userObj?.get("phone")?.let(::jsonToDisplayString) ?: s("phone"),
            alternate_phone = user?.alternate_phone ?: userObj?.get("alternate_phone")?.let(::jsonToDisplayString) ?: s("alternate_phone"),
            emergency_contact = user?.emergency_contact ?: userObj?.get("emergency_contact")?.let(::jsonToDisplayString) ?: s("emergency_contact"),
            avatar = user?.avatar ?: userObj?.get("avatar")?.let(::jsonToDisplayString) ?: userObj?.get("profile_picture")?.let(::jsonToDisplayString) ?: s("avatar", "profile_picture"),
            address = user?.address ?: userObj?.get("address")?.let(::jsonToDisplayString) ?: s("address"),
            current_address = user?.current_address ?: userObj?.get("current_address")?.let(::jsonToDisplayString) ?: s("current_address"),
            permanent_address = user?.permanent_address ?: userObj?.get("permanent_address")?.let(::jsonToDisplayString) ?: s("permanent_address"),
            city = user?.city ?: userObj?.get("city")?.let(::jsonToDisplayString) ?: s("city"),
            state = user?.state ?: userObj?.get("state")?.let(::jsonToDisplayString) ?: s("state"),
            pincode = user?.pincode ?: userObj?.get("pincode")?.let(::jsonToDisplayString) ?: s("pincode"),
            dob = user?.dob ?: userObj?.get("dob")?.let(::jsonToDisplayString) ?: s("dob", "date_of_birth"),
            date_of_birth = user?.date_of_birth ?: userObj?.get("date_of_birth")?.let(::jsonToDisplayString) ?: s("date_of_birth"),
            gender = user?.gender ?: userObj?.get("gender")?.let(::jsonToDisplayString) ?: s("gender"),
            blood_group = user?.blood_group ?: userObj?.get("blood_group")?.let(::jsonToDisplayString) ?: s("blood_group"),
            religion = user?.religion ?: userObj?.get("religion")?.let(::jsonToDisplayString) ?: s("religion"),
            marital_status = user?.marital_status ?: userObj?.get("marital_status")?.let(::jsonToDisplayString) ?: s("marital_status"),
            father_name = user?.father_name ?: userObj?.get("father_name")?.let(::jsonToDisplayString) ?: s("father_name"),
            husband_name = user?.husband_name ?: userObj?.get("husband_name")?.let(::jsonToDisplayString) ?: s("husband_name"),
            mother_name = user?.mother_name ?: userObj?.get("mother_name")?.let(::jsonToDisplayString) ?: s("mother_name"),
            qualification = user?.qualification ?: userObj?.get("qualification")?.let(::jsonToDisplayString) ?: s("qualification"),
            experience = user?.experience ?: userObj?.get("experience")?.let(::jsonToDisplayString) ?: s("experience"),
            joining_date = user?.joining_date ?: userObj?.get("joining_date")?.let(::jsonToDisplayString) ?: s("joining_date"),
            department = user?.department ?: userObj?.get("department")?.let(::jsonToDisplayString) ?: s("department"),
            is_active = user?.is_active ?: userObj?.get("is_active")?.let(::jsonToBoolean) ?: obj["is_active"]?.let(::jsonToBoolean)
        )
    }

    private fun parseLoose(obj: JsonObject): Teacher {
        fun s(vararg keys: String): String? =
            keys.firstNotNullOfOrNull { key -> obj[key]?.let(::jsonToDisplayString) }

        val branchObj = obj["branch"] as? JsonObject
        val branch = if (branchObj != null) {
            TeacherBranchBrief(
                id = branchObj["id"]?.let(::jsonToDisplayString),
                name = branchObj["name"]?.let(::jsonToDisplayString),
                code = branchObj["code"]?.let(::jsonToDisplayString)
            )
        } else {
            TeacherBranchBrief(
                id = s("branch_id"),
                name = s("branch_name"),
                code = s("branch_code")
            )
        }

        return Teacher(
            id = s("id") ?: "0",
            user_id = s("user_id"),
            branch_id = s("branch_id"),
            employee_id = s("employee_id"),
            designation = s("designation"),
            department = s("department"),
            category_type = s("category_type"),
            teacher_status = s("teacher_status"),
            contract_type = s("contract_type"),
            work_shift = s("work_shift"),
            gender = s("gender"),
            blood_group = s("blood_group"),
            religion = s("religion"),
            marital_status = s("marital_status"),
            father_name = s("father_name"),
            husband_name = s("husband_name"),
            mother_name = s("mother_name"),
            national_id = s("national_id"),
            aadhaar_number = s("aadhaar_number"),
            pan_number = s("pan_number"),
            class_teacher_of_grade = s("class_teacher_of_grade"),
            class_teacher_of_section = s("class_teacher_of_section"),
            qualification = s("qualification"),
            experience = s("experience"),
            specialization = s("specialization"),
            subjects = s("subjects"),
            joining_date = s("joining_date"),
            basic_salary = s("basic_salary", "salary"),
            bank_name = s("bank_name"),
            account_title = s("account_title"),
            bank_account_number = s("bank_account_number"),
            ifsc_code = s("ifsc_code"),
            resume = s("resume"),
            id_proof = s("id_proof"),
            is_active = obj["is_active"]?.let(::jsonToBoolean),
            branch = branch
        )
    }

    private fun extractAttachments(obj: JsonObject, existing: List<TeacherAttachment>): List<TeacherAttachment> {
        val list = existing.toMutableList()
        val docArrayKeys = listOf("documents", "attachments", "files", "certificates")
        for (key in docArrayKeys) {
            val arr = obj[key] as? JsonArray ?: continue
            for (item in arr) {
                if (item is JsonObject) {
                    val path = item["file_path"]?.let(::jsonToDisplayString)
                        ?: item["url"]?.let(::jsonToDisplayString)
                        ?: item["path"]?.let(::jsonToDisplayString)
                    val title = item["title"]?.let(::jsonToDisplayString)
                        ?: item["name"]?.let(::jsonToDisplayString)
                        ?: item["original_name"]?.let(::jsonToDisplayString)
                    val docType = item["document_type"]?.let(::jsonToDisplayString)
                        ?: item["type"]?.let(::jsonToDisplayString)

                    if (path != null && list.none { it.file_path == path || it.url == path }) {
                        list.add(
                            TeacherAttachment(
                                id = item["id"]?.let(::jsonToDisplayString),
                                title = title,
                                file_path = path,
                                url = item["url"]?.let(::jsonToDisplayString),
                                document_type = docType,
                                file_size = item["file_size"]?.let(::jsonToDisplayString)
                            )
                        )
                    }
                } else if (item is JsonPrimitive) {
                    val path = item.content.takeIf { it.isNotBlank() }
                    if (path != null && list.none { it.file_path == path || it.url == path }) {
                        list.add(TeacherAttachment(file_path = path))
                    }
                }
            }
        }
        return list
    }

    private fun additionalFields(obj: JsonObject): List<TeacherDetailExtraField> {
        val userObj = obj["user"] as? JsonObject
        val combinedKeys = mutableMapOf<String, JsonElement>()
        obj.entries.forEach { (k, v) -> if (k !in knownKeys) combinedKeys[k] = v }
        userObj?.entries?.forEach { (k, v) -> if (k !in knownKeys) combinedKeys["user_$k"] = v }

        return combinedKeys.entries
            .filter { (_, value) ->
                value !is JsonNull && jsonToDisplayString(value) != null
            }
            .sortedBy { it.key }
            .map { (key, value) ->
                TeacherDetailExtraField(formatLabel(key), jsonToDisplayString(value) ?: "N/A")
            }
    }

    private fun formatLabel(key: String): String =
        key.removePrefix("user_").split('_').joinToString(" ") { part ->
            part.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }

    private fun jsonToDisplayString(element: JsonElement): String? {
        return when (element) {
            is JsonNull -> null
            is JsonPrimitive -> element.content.takeIf { it.isNotBlank() && !it.equals("null", true) }
            is JsonObject -> element.entries.joinToString(", ") { (k, v) ->
                "$k: ${jsonToDisplayString(v) ?: "N/A"}"
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
