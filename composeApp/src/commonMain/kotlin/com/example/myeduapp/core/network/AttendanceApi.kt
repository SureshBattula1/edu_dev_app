package com.example.myeduapp.core.network



import com.example.myeduapp.data.model.AttendanceNotifyReceiptsResponse

import com.example.myeduapp.data.model.AttendanceNotifyRequest

import com.example.myeduapp.data.model.AttendanceNotifyResponse

import com.example.myeduapp.data.model.BulkAttendanceRequest

import com.example.myeduapp.data.model.BulkAttendanceResponse

import com.example.myeduapp.data.model.ClassAttendanceStatusResponse

import com.example.myeduapp.data.model.LaravelClassAttendanceResponse

import com.example.myeduapp.data.model.PersonAttendanceResponse

import io.ktor.client.call.*

import io.ktor.client.request.*

import io.ktor.http.*

import kotlinx.serialization.encodeToString



class AttendanceApi {

    private val client = ApiClient.client



    suspend fun getStudentAttendance(token: String, userId: Int): PersonAttendanceResponse {

        return client.get("${ApiConfig.ATTENDANCE_STUDENT}/$userId") {

            header(HttpHeaders.Authorization, "Bearer $token")

        }.body()

    }



    suspend fun getTeacherAttendance(token: String, teacherId: Int): PersonAttendanceResponse {

        return client.get("${ApiConfig.ATTENDANCE_TEACHER}/$teacherId") {

            header(HttpHeaders.Authorization, "Bearer $token")

        }.body()

    }



    suspend fun getClassAttendance(

        token: String,

        grade: String,

        section: String,

        date: String

    ): LaravelClassAttendanceResponse {

        return client.get("${ApiConfig.ATTENDANCE_CLASS}/$grade/$section") {

            parameter("date", date)

            header(HttpHeaders.Authorization, "Bearer $token")

        }.body()

    }



    suspend fun getClassStatus(token: String, date: String): ClassAttendanceStatusResponse {

        return client.get(ApiConfig.ATTENDANCE_CLASS_STATUS) {

            parameter("date", date)

            header(HttpHeaders.Authorization, "Bearer $token")

        }.body()

    }



    suspend fun notifyStudents(token: String, request: AttendanceNotifyRequest): AttendanceNotifyResponse {

        return client.post(ApiConfig.ATTENDANCE_NOTIFY_STUDENTS) {

            header(HttpHeaders.Authorization, "Bearer $token")

            contentType(ContentType.Application.Json)

            setBody(ApiClient.jsonConfig.encodeToString(request))

        }.body()

    }



    suspend fun getNotifyReceipts(

        token: String,

        date: String,

        grade: String,

        section: String

    ): AttendanceNotifyReceiptsResponse {

        return client.get(ApiConfig.ATTENDANCE_NOTIFY_RECEIPTS) {

            parameter("date", date)

            parameter("grade", grade)

            parameter("section", section)

            header(HttpHeaders.Authorization, "Bearer $token")

        }.body()

    }



    suspend fun submitBulkAttendance(token: String, request: BulkAttendanceRequest): BulkAttendanceResponse {

        val body = ApiClient.jsonConfig.encodeToString(request)

        return client.post(ApiConfig.ATTENDANCE_BULK) {

            header(HttpHeaders.Authorization, "Bearer $token")

            contentType(ContentType.Application.Json)

            setBody(body)

        }.body()

    }

}

