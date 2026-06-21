package rs.ac.bg.matf.attendance.history

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import rs.ac.bg.matf.BuildConfig
import rs.ac.bg.matf.R
import rs.ac.bg.matf.attendance.history.AttendanceHistoryApi
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class AttendanceHistoryActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_BEARER_TOKEN = "extra_bearer_token"
    }

    private val executor: ExecutorService = Executors.newSingleThreadExecutor()

    private lateinit var api: AttendanceHistoryApi
    private lateinit var backButton: Button
    private lateinit var toolbar: Toolbar
    private lateinit var progressBar: ProgressBar
    private lateinit var errorText: TextView
    private lateinit var semesterText: TextView
    private lateinit var emptyText: TextView
    private lateinit var historyContainer: LinearLayout
    private lateinit var token: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attendance_history)

        token = intent.getStringExtra(EXTRA_BEARER_TOKEN).orEmpty()
        if (token.isBlank()) {
            finish()
            return
        }

        api = AttendanceHistoryApi(BuildConfig.BACKEND_BASE_URL)
        bindViews()
        setupToolbar()
        wireUi()
        setupBackHandling()
        loadSummary()
    }

    override fun onDestroy() {
        executor.shutdownNow()
        super.onDestroy()
    }

    private fun bindViews() {
        backButton = findViewById(R.id.backButton)
        toolbar = findViewById(R.id.toolbar)
        progressBar = findViewById(R.id.progressBar)
        errorText = findViewById(R.id.errorText)
        semesterText = findViewById(R.id.semesterText)
        emptyText = findViewById(R.id.emptyText)
        historyContainer = findViewById(R.id.historyContainer)
    }

    private fun wireUi() {
        backButton.setOnClickListener {
            finish()
        }
    }

    private fun setupToolbar() {
        toolbar.title = getString(R.string.attendance_history_title)
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.text_primary))
        toolbar.overflowIcon = AppCompatResources.getDrawable(
            this,
            androidx.appcompat.R.drawable.abc_ic_menu_overflow_material,
        )?.mutate()?.also {
            DrawableCompat.setTint(it, Color.WHITE)
        }
        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupBackHandling() {
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    finish()
                }
            }
        )
    }

    private fun loadSummary() {
        progressBar.visibility = View.VISIBLE
        errorText.visibility = View.GONE
        emptyText.visibility = View.GONE
        historyContainer.removeAllViews()
        executor.execute {
            try {
                val response = api.attendanceHistory(token)
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    semesterText.text = response.currentSemester?.let {
                        getString(R.string.attendance_history_semester_prefix, it.name)
                    } ?: getString(R.string.attendance_history_semester_unavailable)
                    renderSummaries(response.summaries)
                }
            } catch (error: Exception) {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    errorText.visibility = View.VISIBLE
                    errorText.text = error.message ?: getString(R.string.attendance_history_error)
                }
            }
        }
    }

    private fun renderSummaries(summaries: List<AttendanceCourseSummary>) {
        historyContainer.removeAllViews()
        if (summaries.isEmpty()) {
            emptyText.visibility = View.VISIBLE
            emptyText.text = getString(R.string.attendance_history_empty)
            return
        }

        emptyText.visibility = View.GONE
        summaries.forEach { summary ->
            historyContainer.addView(buildCard(summary))
        }
    }

    private fun buildCard(summary: AttendanceCourseSummary): View {
        val card = CardView(this).apply {
            radius = resources.displayMetrics.density * 14
            cardElevation = resources.displayMetrics.density * 2
            useCompatPadding = true
            setCardBackgroundColor(ContextCompat.getColor(this@AttendanceHistoryActivity, android.R.color.white))
        }

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 24, 28, 24)
        }

        val title = TextView(this).apply {
            text = summary.courseName.ifBlank { getString(R.string.attendance_history_unknown_course) }
            textSize = 16f
            setTextColor(Color.BLACK)
            setTypeface(typeface, Typeface.BOLD)
        }
        content.addView(title)

        val total = maxOf(summary.totalLessonsWithRecordedAttendance, summary.attendedLessons)
        val percent = if (total > 0) {
            ((summary.attendedLessons.toDouble() / total.toDouble()) * 100.0).toInt()
        } else {
            0
        }

        val count = TextView(this).apply {
            text = getString(R.string.attendance_history_recorded_count, summary.totalLessonsWithRecordedAttendance)
            textSize = 14f
            setTextColor(Color.parseColor("#111111"))
            setPadding(0, 8, 0, 0)
        }
        content.addView(count)

        val attended = TextView(this).apply {
            text = getString(R.string.attendance_history_attended_count, summary.totalLessonsWithYourAttendance)
            textSize = 14f
            setTextColor(Color.parseColor("#555555"))
            setPadding(0, 4, 0, 0)
        }
        content.addView(attended)

        val note = TextView(this).apply {
            text = getString(R.string.attendance_history_progress, percent)
            textSize = 14f
            setTextColor(Color.parseColor("#555555"))
            setPadding(0, 4, 0, 0)
        }
        content.addView(note)

        val summaryBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = 100
            progress = percent
            isIndeterminate = false
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            )
            params.topMargin = 12
            layoutParams = params
        }
        content.addView(summaryBar)

        card.addView(content)
        val outer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            )
            params.bottomMargin = (resources.displayMetrics.density * 12).toInt()
            layoutParams = params
        }
        outer.addView(card)
        return outer
    }
}
