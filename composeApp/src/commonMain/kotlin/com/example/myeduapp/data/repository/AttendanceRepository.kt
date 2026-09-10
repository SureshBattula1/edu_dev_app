package com.example.myeduapp.data.repository



import com.example.myeduapp.core.datastore.SessionManager

import com.example.myeduapp.core.network.AttendanceApi

import com.example.myeduapp.data.model.Attendance

import com.example.myeduapp.data.model.AttendanceOverview

import com.example.myeduapp.data.model.BulkAttendanceItem

import com.example.myeduapp.data.model.BulkAttendanceRequest

import com.example.myeduapp.data.model.AttendanceNotifyClass

import com.example.myeduapp.data.model.AttendanceNotifyReceipts

import com.example.myeduapp.data.model.AttendanceNotifyRequest

import com.example.myeduapp.data.model.AttendanceNotifyResult

import com.example.myeduapp.data.model.ClassAttendanceResult

import com.example.myeduapp.data.model.ClassAttendanceStatus

import com.example.myeduapp.data.model.SchoolClass

import com.example.myeduapp.data.model.Student

import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.IO

import kotlinx.coroutines.withContext



class AttendanceRepository {

    private val api = AttendanceApi()



    suspend fun getStudentAttendance(userId: Int): Result<List<Attendance>> = withContext(Dispatchers.IO) {

        try {

            val token = SessionManager.token ?: return@withContext Result.failure(Exception("Not authenticated"))

            val response = api.getStudentAttendance(token, userId)

            if (!response.success) return@withContext Result.failure(Exception(response.message ?: "Failed to load attendance"))

            Result.success(response.data.map { it.toAttendance() })

        } catch (e: Exception) {

            Result.failure(e)

        }

    }



    suspend fun getStudentOverview(userId: Int): Result<AttendanceOverview> = withContext(Dispatchers.IO) {

        getStudentAttendance(userId).map { records -> summarize(records) }

    }



    suspend fun getTeacherAttendance(teacherId: Int): Result<List<Attendance>> = withContext(Dispatchers.IO) {

        try {

            val token = SessionManager.token ?: return@withContext Result.failure(Exception("Not authenticated"))

            val response = api.getTeacherAttendance(token, teacherId)

            if (!response.success) return@withContext Result.failure(Exception(response.message ?: "Failed to load attendance"))

            Result.success(response.data.map { it.toAttendance() })

        } catch (e: Exception) {

            Result.failure(e)

        }

    }



    suspend fun getTeacherOverview(teacherId: Int): Result<AttendanceOverview> = withContext(Dispatchers.IO) {

        getTeacherAttendance(teacherId).map { records -> summarize(records) }

    }



    suspend fun getClassAttendance(

        schoolClass: SchoolClass,

        date: String

    ): Result<ClassAttendanceResult> = withContext(Dispatchers.IO) {

        try {

            val token = SessionManager.token ?: return@withContext Result.failure(Exception("Not authenticated"))

            val response = api.getClassAttendance(token, schoolClass.displayGrade, schoolClass.section, date)

            if (!response.success) return@withContext Result.failure(Exception(response.message ?: "Failed to load class attendance"))

            val records = response.data.map { it.toAttendance() }

            Result.success(

                ClassAttendanceResult(

                    date = response.meta?.date?.take(10) ?: date,

                    grade = response.meta?.grade ?: schoolClass.displayGrade,

                    section = response.meta?.section ?: schoolClass.section,

                    attendance = records,

                    meta = response.meta?.copy(

                        total = response.meta.total.takeIf { it > 0 } ?: records.size

                    )

                )

            )

        } catch (e: Exception) {

            Result.failure(e)

        }

    }



    suspend fun getClassStatus(date: String): Result<List<ClassAttendanceStatus>> = withContext(Dispatchers.IO) {

        try {

            val token = SessionManager.token ?: return@withContext Result.failure(Exception("Not authenticated"))

            val response = api.getClassStatus(token, date)

            if (!response.success) {

                return@withContext Result.failure(Exception(response.message ?: "Failed to load class attendance"))

            }

            Result.success(response.data)

        } catch (e: Exception) {

            Result.failure(e)

        }

    }



    suspend fun notifyStudents(

        date: String,

        classes: List<AttendanceNotifyClass>

    ): Result<AttendanceNotifyResult> = withContext(Dispatchers.IO) {

        try {

            val token = SessionManager.token ?: return@withContext Result.failure(Exception("Not authenticated"))

            val response = api.notifyStudents(token, AttendanceNotifyRequest(date = date, classes = classes))

            val data = response.data

            if (!response.success || data == null) {

                return@withContext Result.failure(Exception(response.message ?: "Failed to send notifications"))

            }

            Result.success(data)

        } catch (e: Exception) {

            Result.failure(e)

        }

    }



    suspend fun getNotifyReceipts(

        date: String,

        grade: String,

        section: String

    ): Result<AttendanceNotifyReceipts> = withContext(Dispatchers.IO) {

        try {

            val token = SessionManager.token ?: return@withContext Result.failure(Exception("Not authenticated"))

            val response = api.getNotifyReceipts(token, date, grade, section)

            val data = response.data

            if (!response.success || data == null) {

                return@withContext Result.failure(Exception(response.message ?: "Failed to load notify status"))

            }

            Result.success(data)

        } catch (e: Exception) {

            Result.failure(e)

        }

    }



    suspend fun submitStudentAttendance(

        schoolClass: SchoolClass,

        date: String,

        students: List<Student>,

        statusByUserId: Map<String, String>,

        remarksByUserId: Map<String, String> = emptyMap(),

        isUpdate: Boolean = false

    ): Result<String> = withContext(Dispatchers.IO) {

        val branchId = SessionManager.user?.branch_id

            ?: return@withContext Result.failure(Exception("Branch not assigned to your account"))



        val grade = schoolClass.displayGrade

        val section = schoolClass.section

        if (grade.isBlank() || section.isBlank()) {

            return@withContext Result.failure(Exception("Class and section are required"))

        }



        val uniqueStudents = students

            .filter { it.attendanceUserId.isNotBlank() && it.attendanceUserId != "0" }

            .distinctBy { it.attendanceUserId }



        val items = uniqueStudents.map { student ->

            val userId = student.attendanceUserId

            BulkAttendanceItem(

                id = userId,

                status = statusByUserId[userId] ?: "Present",

                grade_level = grade,

                section = section,

                remarks = remarksByUserId[userId]?.takeIf { it.isNotBlank() }

            )

        }



        if (items.isEmpty()) {

            return@withContext Result.failure(Exception("No valid students to mark attendance for"))

        }



        if (items.size != uniqueStudents.size) {

            return@withContext Result.failure(

                Exception("Could not prepare attendance for all students (${items.size}/${uniqueStudents.size})")

            )

        }



        submitBulk("student", date, branchId, items, isUpdate).map { message ->

            if (items.size < students.size) {

                "$message (${items.size} of ${students.size} students — check duplicate user IDs)"

            } else {

                message

            }

        }

    }



    suspend fun submitTeacherSelfAttendance(

        date: String,

        status: String,

        remarks: String? = null

    ): Result<String> = withContext(Dispatchers.IO) {

        val user = SessionManager.user ?: return@withContext Result.failure(Exception("Not authenticated"))

        val branchId = user.branch_id ?: return@withContext Result.failure(Exception("Branch not assigned"))

        submitBulk(

            type = "teacher",

            date = date,

            branchId = branchId,

            items = listOf(BulkAttendanceItem(id = user.id.toString(), status = status, remarks = remarks))

        )

    }



    private suspend fun submitBulk(

        type: String,

        date: String,

        branchId: Int,

        items: List<BulkAttendanceItem>,

        isUpdate: Boolean = false

    ): Result<String> = withContext(Dispatchers.IO) {

        try {

            val token = SessionManager.token ?: return@withContext Result.failure(Exception("Not authenticated"))

            val response = api.submitBulkAttendance(

                token,

                BulkAttendanceRequest(type = type, date = date, branch_id = branchId, attendance = items)

            )

            if (response.success) {

                val marked = response.data?.marked ?: items.size

                if (marked < items.size) {

                    return@withContext Result.failure(

                        Exception("Only $marked of ${items.size} records were saved. Please try again or contact admin.")

                    )

                }

                val defaultMessage = if (isUpdate) {

                    "Attendance updated for $marked student(s)"

                } else {

                    "Attendance saved for $marked student(s)"

                }

                val message = response.message?.takeIf { it.isNotBlank() } ?: defaultMessage

                Result.success(message)

            } else {

                Result.failure(Exception(response.message ?: "Failed to submit attendance"))

            }

        } catch (e: Exception) {

            Result.failure(e)

        }

    }



    private fun summarize(records: List<Attendance>): AttendanceOverview {

        val total = records.size

        val present = records.count { it.status == "Present" }

        val absent = records.count { it.status == "Absent" }

        val late = records.count { it.status == "Late" }

        val leaves = records.count { it.status in listOf("Sick Leave", "Leave") }

        return AttendanceOverview(

            total_days = total,

            present_days = present,

            absent_days = absent,

            late_days = late,

            leave_days = leaves,

            percentage = if (total > 0) (present.toFloat() / total) * 100f else 0f

        )

    }

}

