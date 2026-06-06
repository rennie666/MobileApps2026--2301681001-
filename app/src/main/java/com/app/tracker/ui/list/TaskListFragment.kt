package com.app.tracker.ui.list

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.tracker.R
import com.app.tracker.di.ServiceLocator
import com.app.tracker.model.Task
import com.app.tracker.ui.TaskAdapter
import com.app.tracker.ui.viewmodel.TaskViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class TaskListFragment : Fragment() {

    private lateinit var viewModel: TaskViewModel
    private lateinit var adapter: TaskAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyStateView: View
    private lateinit var summaryText: TextView

    private var selectedCategory: String = "All"
    private lateinit var categoryChips: Map<String, TextView>
    private var cachedTasks: List<Task> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_task_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // MVVM Initialization via ServiceLocator
        val repository = ServiceLocator.provideTaskRepository(requireContext())
        val factory = TaskViewModel.TaskViewModelFactory(repository)
        viewModel = ViewModelProvider(requireActivity(), factory)[TaskViewModel::class.java]

        // Bind Views
        recyclerView = view.findViewById(R.id.tasks_recycler_view)
        emptyStateView = view.findViewById(R.id.empty_state_view)
        summaryText = view.findViewById(R.id.summary_text)

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = TaskAdapter(
            tasks = emptyList(),
            onItemClick = { task ->
                val action = TaskListFragmentDirections.actionTaskListFragmentToTaskDetailFragment(task.id.toString())
                findNavController().navigate(action)
            },
            onCompletedChanged = { task, _ ->
                viewModel.toggleTaskCompleted(task)
            }
        )
        recyclerView.adapter = adapter

        // Setup FAB
        view.findViewById<FloatingActionButton>(R.id.fab_add_task).setOnClickListener {
            val action = TaskListFragmentDirections.actionTaskListFragmentToTaskDetailFragment(null)
            findNavController().navigate(action)
        }

        // Setup Category Chips mapping
        categoryChips = mapOf(
            "All" to view.findViewById(R.id.chip_all),
            "Work" to view.findViewById(R.id.chip_work),
            "Personal" to view.findViewById(R.id.chip_personal),
            "Health" to view.findViewById(R.id.chip_health),
            "Study" to view.findViewById(R.id.chip_study)
        )

        // Category chip click listeners
        for ((category, chip) in categoryChips) {
            chip.setOnClickListener {
                selectCategory(category)
            }
        }

        // Observe Room LiveData from ViewModel
        viewModel.allTasks.observe(viewLifecycleOwner) { tasks ->
            cachedTasks = tasks
            filterAndDisplayTasks(tasks)
            updateSummary(tasks)
        }
    }

    private fun selectCategory(category: String) {
        selectedCategory = category
        val context = requireContext()
        val textColorSelected = androidx.core.content.ContextCompat.getColor(context, R.color.white)
        val textColorUnselected = androidx.core.content.ContextCompat.getColor(context, R.color.dark_accent)

        // Update Chips UI
        for ((catName, chip) in categoryChips) {
            if (catName == category) {
                chip.setBackgroundResource(R.drawable.bg_chip_selected)
                chip.setTextColor(textColorSelected)
                chip.setTypeface(null, android.graphics.Typeface.BOLD)
            } else {
                chip.setBackgroundResource(R.drawable.bg_chip_unselected)
                chip.setTextColor(textColorUnselected)
                chip.setTypeface(null, android.graphics.Typeface.NORMAL)
            }
        }
        filterAndDisplayTasks(cachedTasks)
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

    private fun updateSummary(tasks: List<Task>) {
        val pendingCount = tasks.count { !it.isCompleted }
        summaryText.text = resources.getQuantityString(R.plurals.summary_pending_tasks, pendingCount, pendingCount)
    }
}
