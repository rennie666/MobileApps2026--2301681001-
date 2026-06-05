package com.app.tracker.ui.statistics

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.app.tracker.R
import com.app.tracker.di.ServiceLocator
import com.app.tracker.model.Task
import com.app.tracker.ui.TaskProgressRingView
import com.app.tracker.ui.viewmodel.TaskViewModel

class StatisticsFragment : Fragment() {

    private lateinit var viewModel: TaskViewModel

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_statistics, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // MVVM Initialization using shared activity ViewModel
        val repository = ServiceLocator.provideTaskRepository(requireContext())
        val factory = TaskViewModel.TaskViewModelFactory(repository)
        viewModel = ViewModelProvider(requireActivity(), factory)[TaskViewModel::class.java]

        // Bind Views
        progressRingView = view.findViewById(R.id.progress_ring_view)
        tvTotal = view.findViewById(R.id.tv_total_tasks)
        tvCompleted = view.findViewById(R.id.tv_completed_tasks)
        tvPending = view.findViewById(R.id.tv_pending_tasks)

        tvWorkRatio = view.findViewById(R.id.tv_work_ratio)
        pbWork = view.findViewById(R.id.pb_work)

        tvPersonalRatio = view.findViewById(R.id.tv_personal_ratio)
        pbPersonal = view.findViewById(R.id.pb_personal)

        tvHealthRatio = view.findViewById(R.id.tv_health_ratio)
        pbHealth = view.findViewById(R.id.pb_health)

        tvStudyRatio = view.findViewById(R.id.tv_study_ratio)
        pbStudy = view.findViewById(R.id.pb_study)

        // Apply visual styling tints
        val tintColor = ColorStateList.valueOf(Color.parseColor("#121212")) // dark_accent
        val bgTintColor = ColorStateList.valueOf(Color.parseColor("#EAECEF"))

        val progressBars = listOf(pbWork, pbPersonal, pbHealth, pbStudy)
        for (pb in progressBars) {
            pb.progressTintList = tintColor
            pb.progressBackgroundTintList = bgTintColor
        }

        // Observe ViewModel LiveData
        viewModel.allTasks.observe(viewLifecycleOwner) { tasks ->
            calculateAndDisplayStats(tasks)
        }
    }

    private fun calculateAndDisplayStats(tasks: List<Task>) {
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
