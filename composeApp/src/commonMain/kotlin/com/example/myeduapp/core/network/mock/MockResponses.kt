package com.example.myeduapp.core.network.mock

/**
 * Mock API JSON payloads used when [com.example.myeduapp.core.network.ApiConfig.USE_MOCKS] is true.
 * Keep these in sync with files under MyEduApp/mocks/.
 */
object MockResponses {

    val login = """
    {
      "success": true,
      "message": "Login successful",
      "user": {
        "id": 1,
        "first_name": "Priya",
        "last_name": "Sharma",
        "email": "teacher@school.com",
        "phone": "9876543210",
        "role": "Teacher",
        "branch_id": 1,
        "school_id": 1,
        "company_id": 1,
        "avatar": null,
        "is_active": true,
        "permissions": ["dashboard.view", "attendance.manage", "students.view"]
      },
      "access_token": "mock-access-token-teacher-001",
      "token_type": "Bearer",
      "expires_in": "30 days"
    }
    """.trimIndent()

    val register = login

    val me = """
    {
      "success": true,
      "data": {
        "id": 1,
        "first_name": "Priya",
        "last_name": "Sharma",
        "email": "teacher@school.com",
        "phone": "9876543210",
        "role": "Teacher",
        "branch_id": 1,
        "school_id": 1,
        "company_id": 1,
        "avatar": null,
        "is_active": true,
        "permissions": ["dashboard.view", "attendance.manage", "students.view"]
      },
      "message": "OK"
    }
    """.trimIndent()

    val logout = """
    {
      "success": true,
      "message": "Logged out successfully"
    }
    """.trimIndent()

    val profile = """
    {
      "success": true,
      "message": "Profile updated",
      "user": {
        "id": 1,
        "first_name": "Priya",
        "last_name": "Sharma",
        "email": "teacher@school.com",
        "phone": "9876543210",
        "role": "Teacher",
        "branch_id": 1,
        "school_id": 1,
        "company_id": 1,
        "avatar": null,
        "is_active": true,
        "permissions": ["dashboard.view", "attendance.manage", "students.view"]
      },
      "access_token": "mock-access-token-teacher-001",
      "token_type": "Bearer",
      "expires_in": "30 days"
    }
    """.trimIndent()

    val changePassword = """
    {
      "success": true,
      "message": "Password changed successfully"
    }
    """.trimIndent()

    val dashboard = """
    {
      "success": true,
      "data": {
        "overview": {
          "total_students": 128,
          "total_teachers": 18,
          "total_branches": 2
        },
        "attendance": {
          "students": {
            "total_days": 20,
            "present_days": 17,
            "absent_days": 2,
            "late_days": 1,
            "leave_days": 0,
            "percentage": 85.0
          },
          "teachers": {
            "total_days": 20,
            "present_days": 19,
            "absent_days": 1,
            "late_days": 0,
            "leave_days": 0,
            "percentage": 95.0
          }
        },
        "fees": {
          "collected": 245000,
          "pending": 38500
        },
        "quick_stats": {
          "classes": 4,
          "students": 128,
          "assignments": 6
        },
        "financial": {
          "revenue_mtd": 245000,
          "expenses_mtd": 82000
        },
        "upcoming_events": [
          { "title": "Unit Test 2", "date": "2026-09-15", "type": "exam" },
          { "title": "Parent-Teacher Meeting", "date": "2026-09-20", "type": "meeting" },
          { "title": "Sports Day", "date": "2026-09-28", "type": "event" }
        ]
      }
    }
    """.trimIndent()

    val students = """
    {
      "success": true,
      "data": [
        {
          "id": 1,
          "user_id": 101,
          "first_name": "Rahul",
          "last_name": "Kumar",
          "admission_number": "ADM2024001",
          "roll_number": "1",
          "grade": "10",
          "section": "A",
          "avatar": null
        },
        {
          "id": 2,
          "user_id": 102,
          "first_name": "Ananya",
          "last_name": "Singh",
          "admission_number": "ADM2024002",
          "roll_number": "2",
          "grade": "10",
          "section": "A",
          "avatar": null
        },
        {
          "id": 3,
          "user_id": 103,
          "first_name": "Vikram",
          "last_name": "Patel",
          "admission_number": "ADM2024003",
          "roll_number": "3",
          "grade": "10",
          "section": "A",
          "avatar": null
        },
        {
          "id": 4,
          "user_id": 104,
          "first_name": "Meera",
          "last_name": "Nair",
          "admission_number": "ADM2024004",
          "roll_number": "4",
          "grade": "9",
          "section": "B",
          "avatar": null
        },
        {
          "id": 5,
          "user_id": 105,
          "first_name": "Arjun",
          "last_name": "Reddy",
          "admission_number": "ADM2024005",
          "roll_number": "5",
          "grade": "9",
          "section": "B",
          "avatar": null
        }
      ]
    }
    """.trimIndent()

    val classes = """
    {
      "success": true,
      "data": [
        {
          "id": 1,
          "name": "10",
          "section": "A",
          "subject": "Mathematics",
          "student_count": 32,
          "academic_year": "2025-26",
          "schedule": "Mon–Fri 09:00",
          "room": "Room 201"
        },
        {
          "id": 2,
          "name": "10",
          "section": "B",
          "subject": "Science",
          "student_count": 30,
          "academic_year": "2025-26",
          "schedule": "Mon–Fri 10:00",
          "room": "Room 202"
        },
        {
          "id": 3,
          "name": "9",
          "section": "A",
          "subject": "English",
          "student_count": 28,
          "academic_year": "2025-26",
          "schedule": "Tue–Sat 09:00",
          "room": "Room 105"
        },
        {
          "id": 4,
          "name": "9",
          "section": "B",
          "subject": "Mathematics",
          "student_count": 29,
          "academic_year": "2025-26",
          "schedule": "Mon–Fri 11:00",
          "room": "Room 106"
        }
      ]
    }
    """.trimIndent()

    val attendanceClass = """
    {
      "success": true,
      "date": "2026-09-08",
      "class_name": "10",
      "section": "A",
      "attendance": [
        {
          "id": 1,
          "student_id": 1,
          "student_name": "Rahul Kumar",
          "roll_number": "1",
          "date": "2026-09-08",
          "status": "Present",
          "remarks": null
        },
        {
          "id": 2,
          "student_id": 2,
          "student_name": "Ananya Singh",
          "roll_number": "2",
          "date": "2026-09-08",
          "status": "Present",
          "remarks": null
        },
        {
          "id": 3,
          "student_id": 3,
          "student_name": "Vikram Patel",
          "roll_number": "3",
          "date": "2026-09-08",
          "status": "Absent",
          "remarks": "Sick"
        },
        {
          "id": 4,
          "student_id": 4,
          "student_name": "Meera Nair",
          "roll_number": "4",
          "date": "2026-09-08",
          "status": "Late",
          "remarks": null
        },
        {
          "id": 5,
          "student_id": 5,
          "student_name": "Arjun Reddy",
          "roll_number": "5",
          "date": "2026-09-08",
          "status": "Present",
          "remarks": null
        }
      ]
    }
    """.trimIndent()

    val attendanceStudent = """
    {
      "success": true,
      "data": [
        { "id": 11, "student_id": 1, "date": "2026-09-08", "status": "Present", "remarks": null },
        { "id": 12, "student_id": 1, "date": "2026-09-07", "status": "Present", "remarks": null },
        { "id": 13, "student_id": 1, "date": "2026-09-06", "status": "Late", "remarks": "Bus delay" },
        { "id": 14, "student_id": 1, "date": "2026-09-05", "status": "Absent", "remarks": null },
        { "id": 15, "student_id": 1, "date": "2026-09-04", "status": "Present", "remarks": null }
      ]
    }
    """.trimIndent()

    val attendanceOverview = """
    {
      "success": true,
      "data": {
        "total_days": 20,
        "present_days": 16,
        "absent_days": 2,
        "late_days": 2,
        "leave_days": 0,
        "percentage": 80.0
      }
    }
    """.trimIndent()

    val attendanceSubmit = """
    {
      "success": true,
      "data": "Attendance submitted",
      "message": "Attendance saved successfully"
    }
    """.trimIndent()

    val assignments = """
    {
      "success": true,
      "data": [
        {
          "id": 1,
          "title": "Quadratic Equations Worksheet",
          "description": "Solve problems 1–20 from chapter 4",
          "class_name": "10",
          "section": "A",
          "subject": "Mathematics",
          "due_date": "2026-09-12",
          "submission_count": 18,
          "status": "Active"
        },
        {
          "id": 2,
          "title": "Lab Report – Acids & Bases",
          "description": "Write a full lab report with observations",
          "class_name": "10",
          "section": "B",
          "subject": "Science",
          "due_date": "2026-09-14",
          "submission_count": 12,
          "status": "Active"
        },
        {
          "id": 3,
          "title": "Essay – Climate Change",
          "description": "800-word essay with sources",
          "class_name": "9",
          "section": "A",
          "subject": "English",
          "due_date": "2026-09-18",
          "submission_count": 8,
          "status": "Active"
        }
      ]
    }
    """.trimIndent()

    val upcomingExams = """
    {
      "success": true,
      "data": [
        {
          "id": 1,
          "name": "Unit Test 2 – Mathematics",
          "date": "2026-09-15",
          "time": "09:00",
          "type": "Unit Test",
          "subject": "Mathematics"
        },
        {
          "id": 2,
          "name": "Unit Test 2 – Science",
          "date": "2026-09-16",
          "time": "09:00",
          "type": "Unit Test",
          "subject": "Science"
        },
        {
          "id": 3,
          "name": "Half Yearly – English",
          "date": "2026-10-05",
          "time": "10:00",
          "type": "Half Yearly",
          "subject": "English"
        }
      ]
    }
    """.trimIndent()

    val studentResults = """
    {
      "success": true,
      "data": [
        {
          "id": 1,
          "subject": "Mathematics",
          "marks": 88.0,
          "total_marks": 100.0,
          "grade": "A",
          "remarks": "Excellent",
          "date": "2026-08-20"
        },
        {
          "id": 2,
          "subject": "Science",
          "marks": 76.0,
          "total_marks": 100.0,
          "grade": "B+",
          "remarks": null,
          "date": "2026-08-21"
        },
        {
          "id": 3,
          "subject": "English",
          "marks": 91.0,
          "total_marks": 100.0,
          "grade": "A+",
          "remarks": "Outstanding writing",
          "date": "2026-08-22"
        }
      ]
    }
    """.trimIndent()

    val feeDues = """
    {
      "success": true,
      "data": [
        {
          "id": 1,
          "fee_type": "Tuition Fee – Sep 2026",
          "amount": 4500.0,
          "due_date": "2026-09-10",
          "status": "Pending"
        },
        {
          "id": 2,
          "fee_type": "Transport Fee – Sep 2026",
          "amount": 1200.0,
          "due_date": "2026-09-10",
          "status": "Pending"
        },
        {
          "id": 3,
          "fee_type": "Lab Fee",
          "amount": 800.0,
          "due_date": "2026-09-15",
          "status": "Partially Paid"
        }
      ]
    }
    """.trimIndent()

    val feePayments = """
    {
      "success": true,
      "data": [
        {
          "id": 1,
          "amount_paid": 4500.0,
          "payment_date": "2026-08-05",
          "payment_method": "UPI",
          "receipt_number": "RCP-2026-0801"
        },
        {
          "id": 2,
          "amount_paid": 1200.0,
          "payment_date": "2026-08-05",
          "payment_method": "Cash",
          "receipt_number": "RCP-2026-0802"
        }
      ]
    }
    """.trimIndent()

    val leaves = """
    {
      "success": true,
      "data": [
        {
          "id": 1,
          "start_date": "2026-09-10",
          "end_date": "2026-09-11",
          "leave_type": "Sick",
          "reason": "Fever",
          "status": "Pending",
          "applied_on": "2026-09-08"
        },
        {
          "id": 2,
          "start_date": "2026-08-20",
          "end_date": "2026-08-20",
          "leave_type": "Casual",
          "reason": "Family function",
          "status": "Approved",
          "applied_on": "2026-08-18"
        }
      ]
    }
    """.trimIndent()

    val applyLeave = """
    {
      "success": true,
      "data": true,
      "message": "Leave applied successfully"
    }
    """.trimIndent()

    val notifications = """
    {
      "success": true,
      "data": [
        {
          "id": "n1",
          "title": "Attendance reminder",
          "message": "Please mark attendance for Class 10-A today.",
          "type": "attendance",
          "date": "2026-09-08",
          "is_read": false
        },
        {
          "id": "n2",
          "title": "New assignment due",
          "message": "Quadratic Equations Worksheet is due on 12 Sep.",
          "type": "assignment",
          "date": "2026-09-07",
          "is_read": true
        },
        {
          "id": "n3",
          "title": "PTM scheduled",
          "message": "Parent-Teacher Meeting on 20 Sep at 10:00 AM.",
          "type": "event",
          "date": "2026-09-06",
          "is_read": false
        }
      ]
    }
    """.trimIndent()

    val announcements = """
    {
      "success": true,
      "data": [
        {
          "id": 1,
          "title": "Half-yearly exam timetable released",
          "content": "Please check the notice board and portal for the full schedule.",
          "author": "Principal",
          "published_at": "2026-09-05T09:00:00.000000Z"
        },
        {
          "id": 2,
          "title": "Independence Day celebration photos",
          "content": "Photos from the school celebration are now available in the gallery.",
          "author": "Admin",
          "published_at": "2026-08-16T14:30:00.000000Z"
        }
      ]
    }
    """.trimIndent()

    val holidays = """
    {
      "success": true,
      "data": [
        {
          "id": 1,
          "name": "Ganesh Chaturthi",
          "date": "2026-09-14",
          "end_date": null,
          "description": "Public holiday"
        },
        {
          "id": 2,
          "name": "Gandhi Jayanti",
          "date": "2026-10-02",
          "end_date": null,
          "description": "National holiday"
        },
        {
          "id": 3,
          "name": "Diwali Break",
          "date": "2026-11-08",
          "end_date": "2026-11-12",
          "description": "School closed for Diwali"
        }
      ]
    }
    """.trimIndent()

    val timetable = """
    {
      "success": true,
      "data": [
        {
          "id": 1,
          "day": "Monday",
          "start_time": "09:00",
          "end_time": "09:45",
          "subject": "Mathematics",
          "teacher": "Priya Sharma",
          "room": "Room 201"
        },
        {
          "id": 2,
          "day": "Monday",
          "start_time": "09:45",
          "end_time": "10:30",
          "subject": "Science",
          "teacher": "Ravi Mehta",
          "room": "Lab 1"
        },
        {
          "id": 3,
          "day": "Tuesday",
          "start_time": "09:00",
          "end_time": "09:45",
          "subject": "English",
          "teacher": "Sneha Iyer",
          "room": "Room 201"
        },
        {
          "id": 4,
          "day": "Wednesday",
          "start_time": "11:00",
          "end_time": "11:45",
          "subject": "Mathematics",
          "teacher": "Priya Sharma",
          "room": "Room 201"
        },
        {
          "id": 5,
          "day": "Friday",
          "start_time": "10:00",
          "end_time": "10:45",
          "subject": "Computer Science",
          "teacher": "Amit Joshi",
          "room": "Lab 2"
        }
      ]
    }
    """.trimIndent()

    val notFound = """
    {
      "success": false,
      "message": "Mock route not found"
    }
    """.trimIndent()
}
