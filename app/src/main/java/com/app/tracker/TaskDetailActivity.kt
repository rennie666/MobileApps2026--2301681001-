package com.app.tracker

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.app.tracker.data.TaskRepository
import com.app.tracker.model.Task
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class TaskDetailActivity : AppCompatActivity() {

    private lateinit var repository: TaskRepository
    private var taskId: String? = null
    private var isEditMode = false

    // Views
    private lateinit var screenTitle: TextView
    private lateinit var editTitle: EditText
    private lateinit var editDesc: EditText
    private lateinit var tvDueDate: TextView
    private lateinit var btnPickDate: LinearLayout
    private lateinit var completionContainer: LinearLayout
    private lateinit var switchCompleted: SwitchCompat
    private lateinit var btnSave: Button
    private lateinit var btnDelete: Button
    private lateinit var bottomNav: BottomNavigationView

    // Selection states
    private var selectedCategory = "Work"
    private var selectedPriority = "Medium"
    private var selectedDate = ""

    private lateinit var categoryChips: Map<String, TextView>
    private lateinit var priorityChips: Map<String, TextView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_detail)

        repository = TaskRepository(this)
        taskId = intent.getStringExtra("TASK_ID")
        isEditMode = taskId != null

        // Bind Views
        screenTitle = findViewById(R.id.screen_title)
        editTitle = findViewById(R.id.edit_task_title)
        editDesc = findViewById(R.id.edit_task_desc)
        tvDueDate = findViewById(R.id.tv_due_date)
        btnPickDate = findViewById(R.id.btn_pick_date)
        completionContainer = findViewById(R.id.completion_container)
        switchCompleted = findViewById(R.id.switch_completed)
        btnSave = findViewById(R.id.btn_save_task)
        btnDelete = findViewById(R.id.btn_delete_task)
        bottomNav = findViewById(R.id.bottom_navigation)

        // Setup Category chips mapping
        categoryChips = mapOf(
            "Work" to findViewById(R.id.detail_chip_work),
            "Personal" to findViewById(R.id.detail_chip_personal),
            "Health" to findViewById(R.id.detail_chip_health),
            "Study" to findViewById(R.id.detail_chip_study)
        )

        for ((category, chip) in categoryChips) {
            chip.setOnClickListener { selectCategory(category) }
        }

        // Setup Priority chips mapping
        priorityChips = mapOf(
            "Low" to findViewById(R.id.priority_low),
            "Medium" to findViewById(R.id.priority_medium),
            "High" to findViewById(R.id.priority_high)
        )

        for ((priority, chip) in priorityChips) {
            chip.setOnClickListener { selectPriority(priority) }
        }

        // Date Picker Setup
        val calendar = Calendar.getInstance()
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        selectedDate = dateFormatter.format(calendar.time)
        tvDueDate.text = selectedDate

        btnPickDate.setOnClickListener {
            val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                selectedDate = dateFormatter.format(calendar.time)
                tvDueDate.text = selectedDate
            }
            DatePickerDialog(
                this,
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Save and Delete action listeners
        btnSave.setOnClickListener { saveTask() }
        btnDelete.setOnClickListener { deleteTask() }

        // Load task data if edit mode
        if (isEditMode) {
            loadTaskData()
        } else {
            // Set defaults for new mode
            selectCategory("Work")
            selectPriority("Medium")
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
                R.id.nav_detail -> true
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
        bottomNav.selectedItemId = R.id.nav_detail
        overridePendingTransition(0, 0)

        // Re-check mode in case we navigate to detail tab directly vs editing a task
        // If we came from bottom navigation selection, we should be in Create Task mode.
        val intentTaskId = intent.getStringExtra("TASK_ID")
        if (intentTaskId != taskId) {
            taskId = intentTaskId
            isEditMode = taskId != null
            if (isEditMode) {
                loadTaskData()
            } else {
                resetForm()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun loadTaskData() {
        val task = taskId?.let { repository.getTaskById(it) }
        if (task != null) {
            screenTitle.text = "Task Details"
            editTitle.setText(task.title)
            editDesc.setText(task.description)
            selectCategory(task.category)
            selectPriority(task.priority)
            selectedDate = task.dueDate
            tvDueDate.text = task.dueDate

            completionContainer.visibility = View.VISIBLE
            switchCompleted.isChecked = task.isCompleted

            btnDelete.visibility = View.VISIBLE
        } else {
            resetForm()
        }
    }

    private fun resetForm() {
        screenTitle.text = "Create New Task"
        editTitle.setText("")
        editDesc.setText("")
        selectCategory("Work")
        selectPriority("Medium")

        val calendar = Calendar.getInstance()
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        selectedDate = dateFormatter.format(calendar.time)
        tvDueDate.text = selectedDate

        completionContainer.visibility = View.GONE
        btnDelete.visibility = View.GONE
    }

    private fun selectCategory(category: String) {
        selectedCategory = category
        for ((catName, chip) in categoryChips) {
            if (catName.lowercase() == category.lowercase()) {
                chip.setBackgroundResource(R.drawable.bg_chip_selected)
                chip.setTextColor(Color.WHITE)
                chip.setTypeface(null, android.graphics.Typeface.BOLD)
            } else {
                chip.setBackgroundResource(R.drawable.bg_chip_unselected)
                chip.setTextColor(Color.parseColor("#121212"))
                chip.setTypeface(null, android.graphics.Typeface.NORMAL)
            }
        }
    }

    private fun selectPriority(priority: String) {
        selectedPriority = priority
        for ((priName, chip) in priorityChips) {
            if (priName.lowercase() == priority.lowercase()) {
                chip.setBackgroundResource(R.drawable.bg_chip_selected)
                chip.setTextColor(Color.WHITE)
                chip.setTypeface(null, android.graphics.Typeface.BOLD)
            } else {
                chip.setBackgroundResource(R.drawable.bg_chip_unselected)
                chip.setTextColor(Color.parseColor("#121212"))
                chip.setTypeface(null, android.graphics.Typeface.NORMAL)
            }
        }
    }

    private fun saveTask() {
        val title = editTitle.text.toString().trim()
        val desc = editDesc.text.toString().trim()

        if (title.isEmpty()) {
            Toast.makeText(this, "Title cannot be empty!", Toast.LENGTH_SHORT).show()
            return
        }

        val task = Task(
            id = taskId ?: UUID.randomUUID().toString(),
            title = title,
            description = desc,
            category = selectedCategory,
            priority = selectedPriority,
            isCompleted = if (isEditMode) switchCompleted.isChecked else false,
            dueDate = selectedDate
        )

        repository.saveTask(task)
        Toast.makeText(this, "Task saved successfully!", Toast.LENGTH_SHORT).show()

        // Clear intent extra so that the tab returns to Create mode next time
        intent.removeExtra("TASK_ID")

        // Return to MainActivity
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        }
        startActivity(intent)
        finish()
    }

    private fun deleteTask() {
        taskId?.let {
            repository.deleteTask(it)
            Toast.makeText(this, "Task deleted!", Toast.LENGTH_SHORT).show()
            intent.removeExtra("TASK_ID")
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            }
            startActivity(intent)
            finish()
        }
    }
}
