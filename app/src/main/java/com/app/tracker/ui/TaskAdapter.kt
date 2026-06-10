package com.app.tracker.ui

<<<<<<< Updated upstream
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
            val colorRes = when (priority.lowercase()) {
                "high" -> R.color.priority_high
                "medium" -> R.color.priority_medium
                else -> R.color.priority_low
            }
            val context = priorityDot.context
            priorityDot.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, colorRes))
        }

        private fun setupCategoryBadge(category: String) {
            val (bgColorRes, textColorRes) = when (category.lowercase()) {
                "work" -> Pair(R.color.work_bg, R.color.work_text)
                "personal" -> Pair(R.color.personal_bg, R.color.personal_text)
                "health" -> Pair(R.color.health_bg, R.color.health_text)
                "study" -> Pair(R.color.study_bg, R.color.study_text)
                else -> Pair(R.color.bg_light, R.color.dark_accent)
            }

            val context = categoryBadge.context
            categoryBadge.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, bgColorRes))
            categoryBadge.setTextColor(ContextCompat.getColor(context, textColorRes))
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
=======
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.tracker.R
import com.app.tracker.data.Task

class TaskAdapter(private val taskList: List<Task>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTaskTitle)
        val tvStatus: TextView = itemView.findViewById(R.id.tvTaskStatus)
        val progressBar: ProgressBar = itemView.findViewById(R.id.taskProgressBar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val task = taskList[position]
        if (holder is TaskViewHolder) {
            holder.tvTitle.text = task.title
            holder.tvStatus.text = task.status
            holder.progressBar.progress = task.progress
        }
    }

    override fun getItemCount(): Int = taskList.size
}
>>>>>>> Stashed changes
