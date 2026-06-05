package com.app.tracker.ui.detail

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.app.tracker.R
import com.app.tracker.di.ServiceLocator
import com.app.tracker.model.Task
import android.content.Intent
import com.app.tracker.ui.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TaskDetailFragment : Fragment() {

    private lateinit var viewModel: TaskViewModel
    private var taskId: String? = null
    private var isEditMode = false
    private var currentTask: Task? = null

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
    private lateinit var btnShare: Button

    // Selection states
    private var selectedCategory = "Work"
    private var selectedPriority = "Medium"
    private var selectedDate = ""

    private lateinit var categoryChips: Map<String, TextView>
    private lateinit var priorityChips: Map<String, TextView>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_task_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // MVVM Initialization
        val repository = ServiceLocator.provideTaskRepository(requireContext())
        val factory = TaskViewModel.TaskViewModelFactory(repository)
        viewModel = ViewModelProvider(requireActivity(), factory)[TaskViewModel::class.java]

        // Fetch Safe Args
        arguments?.let {
            val args = TaskDetailFragmentArgs.fromBundle(it)
            taskId = args.taskId
        }
        isEditMode = taskId != null

        // Bind Views
        screenTitle = view.findViewById(R.id.screen_title)
        editTitle = view.findViewById(R.id.edit_task_title)
        editDesc = view.findViewById(R.id.edit_task_desc)
        tvDueDate = view.findViewById(R.id.tv_due_date)
        btnPickDate = view.findViewById(R.id.btn_pick_date)
        completionContainer = view.findViewById(R.id.completion_container)
        switchCompleted = view.findViewById(R.id.switch_completed)
        btnSave = view.findViewById(R.id.btn_save_task)
        btnDelete = view.findViewById(R.id.btn_delete_task)
        btnShare = view.findViewById(R.id.btn_share_task)

        // Setup Category chips mapping
        categoryChips = mapOf(
            "Work" to view.findViewById(R.id.detail_chip_work),
            "Personal" to view.findViewById(R.id.detail_chip_personal),
            "Health" to view.findViewById(R.id.detail_chip_health),
            "Study" to view.findViewById(R.id.detail_chip_study)
        )

        for ((category, chip) in categoryChips) {
            chip.setOnClickListener { selectCategory(category) }
        }

        // Setup Priority chips mapping
        priorityChips = mapOf(
            "Low" to view.findViewById(R.id.priority_low),
            "Medium" to view.findViewById(R.id.priority_medium),
            "High" to view.findViewById(R.id.priority_high)
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
                requireContext(),
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Save, Share and Delete action listeners
        btnSave.setOnClickListener { saveTask() }
        btnDelete.setOnClickListener { deleteTask() }
        btnShare.setOnClickListener { shareTask() }

        // Load task data if edit mode
        if (isEditMode) {
            taskId?.toIntOrNull()?.let { id ->
                viewModel.getTaskById(id) { task ->
                    currentTask = task
                    loadTaskData(task)
                }
            }
        } else {
            selectCategory("Work")
            selectPriority("Medium")
        }
    }

    private fun loadTaskData(task: Task?) {
        if (task != null) {
            screenTitle.text = getString(R.string.title_edit_task)
            editTitle.setText(task.title)
            editDesc.setText(task.description)
            selectCategory(task.category)
            selectPriority(task.priority)
            selectedDate = task.dueDate
            tvDueDate.text = task.dueDate

            completionContainer.visibility = View.VISIBLE
            switchCompleted.isChecked = task.isCompleted

            btnDelete.visibility = View.VISIBLE
            btnShare.visibility = View.VISIBLE
        }
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
            Toast.makeText(requireContext(), R.string.toast_title_empty, Toast.LENGTH_SHORT).show()
            return
        }

        if (isEditMode) {
            val task = currentTask?.copy(
                title = title,
                description = desc,
                category = selectedCategory,
                priority = selectedPriority,
                isCompleted = switchCompleted.isChecked,
                dueDate = selectedDate
            )
            task?.let {
                viewModel.updateTask(it)
                Toast.makeText(requireContext(), R.string.toast_task_updated, Toast.LENGTH_SHORT).show()
            }
        } else {
            val task = Task(
                id = 0, // autogenerate ID
                title = title,
                description = desc,
                category = selectedCategory,
                priority = selectedPriority,
                isCompleted = false,
                dueDate = selectedDate
            )
            viewModel.insertTask(task)
            Toast.makeText(requireContext(), R.string.toast_task_created, Toast.LENGTH_SHORT).show()
        }

        findNavController().navigateUp()
    }

    private fun deleteTask() {
        currentTask?.let {
            viewModel.deleteTask(it)
            Toast.makeText(requireContext(), R.string.toast_task_deleted, Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }

    private fun shareTask() {
        currentTask?.let { task ->
            val status = if (task.isCompleted) getString(R.string.status_completed) else getString(R.string.status_pending)
            val shareText = getString(
                R.string.share_task_template,
                task.title,
                task.description,
                task.category,
                task.priority,
                task.dueDate,
                status
            )

            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, shareText)
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, getString(R.string.share_task_via))
            startActivity(shareIntent)
        }
    }
}
