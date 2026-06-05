package com.app.tracker.ui

import android.graphics.Color
import android.graphics.Paint
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.app.tracker.R
import com.app.tracker.model.Task

class TaskAdapter(
    private var tasks: List<Task>,
    private val onItemClick: (Task) -> Unit,
    private val onCompletedChanged: (Task, Boolean) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    fun updateTasks(newTasks: List<Task>) {
        this.tasks = newTasks
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.task_item, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.bind(task, onItemClick, onCompletedChanged)
    }

    override fun getItemCount(): Int = tasks.size

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val checkbox: CheckBox = itemView.findViewById(R.id.task_checkbox)
        private val titleText: TextView = itemView.findViewById(R.id.task_title)
        private val descText: TextView = itemView.findViewById(R.id.task_desc)
        private val categoryBadge: TextView = itemView.findViewById(R.id.task_category_badge)
        private val priorityDot: View = itemView.findViewById(R.id.priority_dot)
        private val dueDateText: TextView = itemView.findViewById(R.id.task_due_date)

        fun bind(
            task: Task,
            onItemClick: (Task) -> Unit,
            onCompletedChanged: (Task, Boolean) -> Unit
        ) {
            titleText.text = task.title
            descText.text = task.description
            categoryBadge.text = task.category
            dueDateText.text = formatDate(task.dueDate)

            // Setup task click to open detail
            itemView.setOnClickListener { onItemClick(task) }

            // Setup checkbox click
            checkbox.isChecked = task.isCompleted
            checkbox.setOnClickListener {
                val isChecked = (it as CheckBox).isChecked
                onCompletedChanged(task, isChecked)
                applyCompletedStyle(isChecked)
            }

            applyCompletedStyle(task.isCompleted)
            setupPriorityDot(task.priority)
            setupCategoryBadge(task.category)
        }

        private fun applyCompletedStyle(isCompleted: Boolean) {
            if (isCompleted) {
                titleText.paintFlags = titleText.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                titleText.alpha = 0.45f
                descText.alpha = 0.35f
                itemView.alpha = 0.85f
            } else {
                titleText.paintFlags = titleText.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                titleText.alpha = 1.0f
                descText.alpha = 0.6f
                itemView.alpha = 1.0f
            }
        }

        private fun setupPriorityDot(priority: String) {
            val colorStr = when (priority.lowercase()) {
                "high" -> "#FFFF3B30" // Red
                "medium" -> "#FFFF9500" // Orange
                else -> "#FF34C759" // Green for low/default
            }
            priorityDot.backgroundTintList = ColorStateList.valueOf(Color.parseColor(colorStr))
        }

        private fun setupCategoryBadge(category: String) {
            val (bgColorStr, textColorStr) = when (category.lowercase()) {
                "work" -> Pair("#E2D4F0", "#5B21B6") // Soft purple background, deep purple text
                "personal" -> Pair("#D7EBE5", "#0F766E") // Mint background, deep teal text
                "health" -> Pair("#FFEBEB", "#B91C1C") // Soft red background, deep red text
                "study" -> Pair("#FEF3C7", "#B45309") // Soft amber background, deep amber text
                else -> Pair("#E2E8F0", "#334155") // Gray
            }

            categoryBadge.backgroundTintList = ColorStateList.valueOf(Color.parseColor(bgColorStr))
            categoryBadge.setTextColor(Color.parseColor(textColorStr))
        }

        private fun formatDate(dateStr: String): String {
            return try {
                val parser = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                val formatter = java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault())
                val date = parser.parse(dateStr)
                if (date != null) formatter.format(date) else dateStr
            } catch (e: java.lang.Exception) {
                dateStr
            }
        }
    }
}
