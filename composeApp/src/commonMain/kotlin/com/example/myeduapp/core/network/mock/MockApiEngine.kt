package com.example.myeduapp.core.network.mock

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf

/**
 * In-memory mock HTTP engine that serves [MockResponses] for all MyEduApp API routes.
 * Enabled when [com.example.myeduapp.core.network.ApiConfig.USE_MOCKS] is true.
 */
object MockApiEngine {

    fun create(): MockEngine = MockEngine { request ->
        val path = request.url.encodedPath
            .removePrefix("/")
            .removePrefix("api/")
            .trimEnd('/')
        val method = request.method

        val body = resolve(method, path)
        respond(
            content = body,
            status = HttpStatusCode.OK,
            headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        )
    }

    private fun resolve(method: HttpMethod, path: String): String = when {
        method == HttpMethod.Post && path == "login" -> MockResponses.login
        method == HttpMethod.Post && path == "register" -> MockResponses.register
        method == HttpMethod.Get && path == "me" -> MockResponses.me
        method == HttpMethod.Post && path == "logout" -> MockResponses.logout
        method == HttpMethod.Put && path == "profile" -> MockResponses.profile
        method == HttpMethod.Put && path == "change-password" -> MockResponses.changePassword

        method == HttpMethod.Get && path == "dashboard" -> MockResponses.dashboard
        method == HttpMethod.Get && path == "dashboard/upcoming-exams" -> MockResponses.upcomingExams
        method == HttpMethod.Get && path == "dashboard/student-results" -> MockResponses.studentResults

        method == HttpMethod.Get && path == "students" -> MockResponses.students
        method == HttpMethod.Get && path == "classes" -> MockResponses.classes
        method == HttpMethod.Get && path == "assignments" -> MockResponses.assignments

        method == HttpMethod.Get && path.matches(Regex("""attendance/student/\d+/overview""")) ->
            MockResponses.attendanceOverview
        method == HttpMethod.Get && path.matches(Regex("""attendance/student/\d+""")) ->
            MockResponses.attendanceStudent
        method == HttpMethod.Get && path.matches(Regex("""attendance/class/\d+""")) ->
            MockResponses.attendanceClass
        method == HttpMethod.Post && path == "attendance/submit" -> MockResponses.attendanceSubmit

        method == HttpMethod.Get && path.matches(Regex("""fee-dues/student/\d+""")) -> MockResponses.feeDues
        method == HttpMethod.Get && path.matches(Regex("""students/\d+/fees""")) -> MockResponses.feePayments

        method == HttpMethod.Get && path.matches(Regex("""leaves/student/\d+""")) -> MockResponses.leaves
        method == HttpMethod.Post && path == "leaves" -> MockResponses.applyLeave

        method == HttpMethod.Get && path == "communications/notifications" -> MockResponses.notifications
        method == HttpMethod.Get && path == "communications/announcements" -> MockResponses.announcements
        method == HttpMethod.Get && path == "holidays/upcoming" -> MockResponses.holidays

        method == HttpMethod.Get && path.matches(Regex("""timetables/class/.+/.+""")) -> MockResponses.timetable

        else -> MockResponses.notFound
    }
}
