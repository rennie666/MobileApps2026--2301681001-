package com.app.tracker.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.app.tracker.R
import com.app.tracker.data.Task

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var viewModel: TaskViewModel
    private lateinit var adapter: TaskAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Инициализираме ViewModel-а
        // 1. Инициализираме ViewModel-а по чист и правилен начин
        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application))[TaskViewModel::class.java]
        // 2. Намираме RecyclerView и му закачаме празен адаптер първоначално
        val rvTasks = view.findViewById<RecyclerView>(R.id.rvTasks)
        adapter = TaskAdapter(emptyList())
        rvTasks.adapter = adapter

        // 3. Наблюдаваме (Observe) данните от ViewModel-а
        viewModel.allTasks.observe(viewLifecycleOwner) { tasks ->
            // Ако базата данни е напълно празна, добавяме първите задачи за тест
            if (tasks.isEmpty()) {
                viewModel.insertTask(Task(title = "Website Design", status = "Ongoing", priority = "High", progress = 75))
                viewModel.insertTask(Task(title = "Landing Page", status = "Ongoing", priority = "High", progress = 40))
            } else {
                // Когато има данни в Room, ги подаваме на адаптера
                adapter = TaskAdapter(tasks)
                rvTasks.adapter = adapter
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Всеки път, когато потребителят се върне на този екран, четем наново от базата
        viewModel.refreshTasks()
    }
}