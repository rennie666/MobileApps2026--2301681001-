package com.app.tracker

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.tracker.data.TaskRepository
import com.app.tracker.model.Task
import com.app.tracker.ui.TaskAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var repository: TaskRepository
    private lateinit var adapter: TaskAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyStateView: View
    private lateinit var summaryText: TextView
    private lateinit var bottomNav: BottomNavigationView

    private var selectedCategory: String = "All"
    private lateinit var categoryChips: Map<String, TextView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        repository = TaskRepository(this)

        // Bind Views
        recyclerView = findViewById(R.id.tasks_recycler_view)
        emptyStateView = findViewById(R.id.empty_state_view)
        summaryText = findViewById(R.id.summary_text)
        bottomNav = findViewById(R.id.bottom_navigation)

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TaskAdapter(
            tasks = emptyList(),
            onItemClick = { task ->
                val intent = Intent(this, TaskDetailActivity::class.java).apply {
                    putExtra("TASK_ID", task.id)
                }
                startActivity(intent)
            },
            onCompletedChanged = { task, isCompleted ->
                task.isCompleted = isCompleted
                repository.saveTask(task)
                updateSummary()
            }
        )
        recyclerView.adapter = adapter

        // Setup FAB
        findViewById<FloatingActionButton>(R.id.fab_add_task).setOnClickListener {
            val intent = Intent(this, TaskDetailActivity::class.java)
            startActivity(intent)
        }

        // Setup Category Chips mapping
        categoryChips = mapOf(
            "All" to findViewById(R.id.chip_all),
            "Work" to findViewById(R.id.chip_work),
            "Personal" to findViewById(R.id.chip_personal),
            "Health" to findViewById(R.id.chip_health),
            "Study" to findViewById(R.id.chip_study)
        )

        // Category chip click listeners
        for ((category, chip) in categoryChips) {
            chip.setOnClickListener {
                selectCategory(category)
            }
        }

        // Bottom Navigation setup
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_detail -> {
                    val intent = Intent(this, TaskDetailActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    }
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_statistics -> {
                    val intent = Intent(this, StatisticsActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    }
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    true
                }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Ensure home navigation item is selected when returning to home screen
        bottomNav.selectedItemId = R.id.nav_home
        overridePendingTransition(0, 0)
        loadTasks()
    }

    private fun loadTasks() {
        val tasks = repository.getTasks()
        filterAndDisplayTasks(tasks)
        updateSummary()
    }

    private fun selectCategory(category: String) {
        selectedCategory = category
        // Update Chips UI
        for ((catName, chip) in categoryChips) {
            if (catName == category) {
                chip.setBackgroundResource(R.drawable.bg_chip_selected)
                chip.setTextColor(Color.WHITE)
                chip.setTypeface(null, android.graphics.Typeface.BOLD)
            } else {
                chip.setBackgroundResource(R.drawable.bg_chip_unselected)
                chip.setTextColor(Color.parseColor("#121212")) // dark_accent
                chip.setTypeface(null, android.graphics.Typeface.NORMAL)
            }
        }
        loadTasks()
    }

    private fun filterAndDisplayTasks(allTasks: List<Task>) {
        val filtered = if (selectedCategory == "All") {
            allTasks
        } else {
            allTasks.filter { it.category.lowercase() == selectedCategory.lowercase() }
        }

        adapter.updateTasks(filtered)

        if (filtered.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyStateView.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyStateView.visibility = View.GONE
        }
    }

    private fun updateSummary() {
        val tasks = repository.getTasks()
        val pendingCount = tasks.count { !it.isCompleted }
        summaryText.text = if (pendingCount == 1) {
            "You have 1 pending task for today."
        } else {
            "You have $pendingCount pending tasks for today."
        }
    }
}