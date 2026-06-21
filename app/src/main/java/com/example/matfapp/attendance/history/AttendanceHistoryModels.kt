package rs.ac.bg.matf.attendance.history

data class SemesterInfo(
    val id: Long,
    val name: String,
    val startDate: String,
    val endDate: String,
)

data class AttendanceCourseSummary(
    val courseName: String,
    val courseCode: String?,
    val attendedLessons: Int,
    val totalLessonsWithRecordedAttendance: Int,
    val totalLessonsWithYourAttendance: Int,
)

data class AttendanceHistoryResponse(
    val currentSemester: SemesterInfo?,
    val summaries: List<AttendanceCourseSummary>,
)
