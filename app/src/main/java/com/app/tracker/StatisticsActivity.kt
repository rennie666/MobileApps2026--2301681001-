package com.app.tracker

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.app.tracker.data.TaskRepository
import com.app.tracker.model.Task
import com.app.tracker.ui.TaskProgressRingView
import com.google.android.material.bottomnavigation.BottomNavigationView

class StatisticsActivity : AppCompatActivity() {

    private lateinit var repository: TaskRepository

    // Views
    private lateinit var progressRingView: TaskProgressRingView
    private lateinit var tvTotal: TextView
    private lateinit var tvCompleted: TextView
    private lateinit var tvPending: TextView

    // Work Views
    private lateinit var tvWorkRatio: TextView
    private lateinit var pbWork: ProgressBar

    // Personal Views
    private lateinit var tvPersonalRatio: TextView
    private lateinit var pbPersonal: ProgressBar

    // Health Views
    private lateinit var tvHealthRatio: TextView
    private lateinit var pbHealth: ProgressBar

    // Study Views
    private lateinit var tvStudyRatio: TextView
    private lateinit var pbStudy: ProgressBar

    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        repository = TaskRepository(this)

        // Bind Views
        progressRingView = findViewById(R.id.progress_ring_view)
        tvTotal = findViewById(R.id.tv_total_tasks)
        tvCompleted = findViewById(R.id.tv_completed_tasks)
        tvPending = findViewById(R.id.tv_pending_tasks)

        tvWorkRatio = findViewById(R.id.tv_work_ratio)
        pbWork = findViewById(R.id.pb_work)

        tvPersonalRatio = findViewById(R.id.tv_personal_ratio)
        pbPersonal = findViewById(R.id.pb_personal)

        tvHealthRatio = findViewById(R.id.tv_health_ratio)
        pbHealth = findViewById(R.id.pb_health)

        tvStudyRatio = findViewById(R.id.tv_study_ratio)
        pbStudy = findViewById(R.id.pb_study)

        bottomNav = findViewById(R.id.bottom_navigation)

        // Apply visual styling tints to progress bars dynamically
        val tintColor = ColorStateList.valueOf(Color.parseColor("#121212")) // dark_accent
        val bgTintColor = ColorStateList.valueOf(Color.parseColor("#EAECEF"))

        val progressBars = listOf(pbWork, pbPersonal, pbHealth, pbStudy)
        for (pb in progressBars) {
            pb.progressTintList = tintColor
            pb.progressBackgroundTintList = bgTintColor
        }

        // Bottom Navigation setup
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    }
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_detail -> {
                    val intent = Intent(this, TaskDetailActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    }
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_statistics -> true
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        bottomNav.selectedItemId = R.id.nav_statistics
        overridePendingTransition(0, 0)
        calculateAndDisplayStats()
    }

    private fun calculateAndDisplayStats() {
        val tasks = repository.getTasks()
        val totalCount = tasks.size
        val completedCount = tasks.count { it.isCompleted }
        val pendingCount = totalCount - completedCount

        // Set text views
        tvTotal.text = "Total Tasks: $totalCount"
        tvCompleted.text = "Completed: $completedCount"
        tvPending.text = "Pending: $pendingCount"

        // Set progress ring
        val overallRate = if (totalCount > 0) {
            (completedCount.toFloat() / totalCount.toFloat()) * 100f
        } else {
            0f
        }
        progressRingView.setProgress(overallRate)

        // Update category stats
        updateCategoryStats(tasks, "Work", tvWorkRatio, pbWork)
        updateCategoryStats(tasks, "Personal", tvPersonalRatio, pbPersonal)
        updateCategoryStats(tasks, "Health", tvHealthRatio, pbHealth)
        updateCategoryStats(tasks, "Study", tvStudyRatio, pbStudy)
    }

    private fun updateCategoryStats(
        tasks: List<Task>,
        categoryName: String,
        ratioText: TextView,
        progressBar: ProgressBar
    ) {
        val catTasks = tasks.filter { it.category.lowercase() == categoryName.lowercase() }
        val catTotal = catTasks.size
        val catCompleted = catTasks.count { it.isCompleted }

        ratioText.text = "$catCompleted / $catTotal"

        val percentage = if (catTotal > 0) {
            (catCompleted * 100) / catTotal
        } else {
            0
        }
        progressBar.progress = percentage
    }
}
