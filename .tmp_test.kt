import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.example.myeduapp.data.model.BulkAttendanceItem
import com.example.myeduapp.data.model.BulkAttendanceRequest

fun main() {
    val req = BulkAttendanceRequest(
        type = \"student\",
        date = \"2026-09-09\",
        branch_id = 1,
        attendance = listOf(
            BulkAttendanceItem(1, \"Present\", \"10\", \"A\"),
            BulkAttendanceItem(2, \"Absent\", \"10\", \"A\"),
            BulkAttendanceItem(3, \"Late\", \"10\", \"A\")
        )
    )
    println(Json.encodeToString(req))
}
