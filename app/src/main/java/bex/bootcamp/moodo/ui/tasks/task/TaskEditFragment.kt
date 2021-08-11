package bex.bootcamp.moodo.ui.tasks.task

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import bex.bootcamp.moodo.R
import bex.bootcamp.moodo.databinding.FragmentTaskBinding
import bex.bootcamp.moodo.headless.AlarmBroadcastReceiver
import bex.bootcamp.moodo.model.api.ExtraDescription
import bex.bootcamp.moodo.model.api.ExtraId
import bex.bootcamp.moodo.model.api.Task
import bex.bootcamp.moodo.shared.Logger
import bex.bootcamp.moodo.shared.injectLogger
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class TaskEditFragment(val task: Task?) : BottomSheetDialogFragment() {

    private val logger: Logger by injectLogger()

    private val viewModel: TaskViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentTaskBinding.inflate(inflater, container, false)

        task?.let {
            binding.descriptionText.setText(task.description)
            binding.addButton.text = getString(R.string.task_modify)
            binding.calendarView.updateDate(
                it.dueDate.year,
                it.dueDate.monthValue - 1,
                it.dueDate.dayOfMonth
            )
            binding.timePicker.hour = it.dueDate.hour
            binding.timePicker.minute = it.dueDate.minute
        }

        binding.addButton.setOnClickListener {
            val description = binding.descriptionText.text.toString()
            if (description.isNotEmpty()) {
                if (task != null) {
                    task.description = description
                    task.dueDate = LocalDateTime.of(
                        binding.calendarView.year,
                        binding.calendarView.month + 1,
                        binding.calendarView.dayOfMonth,
                        binding.timePicker.hour,
                        binding.timePicker.minute
                    )
                    viewModel.updateTask(task)
                    schedule(task)
                } else {
                    val newTask =
                        Task(null, description, viewModel.dueDate, viewModel.dueDate, null, 0, 0)
                    viewModel.addTask(newTask)
                    schedule(newTask)
                }
                dismiss()
            }
        }

        binding.calendarView.setOnDateChangedListener { _, year, monthOfYear, dayOfMonth ->
            viewModel.dueDate = LocalDateTime.of(
                LocalDate.of(year, monthOfYear + 1, dayOfMonth),
                viewModel.dueDate.toLocalTime()
            )
        }

        binding.timePicker.setIs24HourView(true)
        binding.timePicker.setOnTimeChangedListener { _, hour, minute ->
            viewModel.dueDate =
                LocalDateTime.of(viewModel.dueDate.toLocalDate(), LocalTime.of(hour, minute))
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val textInput = view.findViewById<TextView>(R.id.description_text)
        textInput.requestFocus()

        val inputMethodManager: InputMethodManager? =
            requireContext().getSystemService(InputMethodManager::class.java)
        inputMethodManager?.showSoftInput(textInput, InputMethodManager.SHOW_IMPLICIT)
    }


    private fun schedule(task: Task) {
        logger.debug { "schedule $task" }

        val alarmManager: AlarmManager = requireContext().getSystemService(AlarmManager::class.java)

        val intent = Intent(requireContext(), AlarmBroadcastReceiver::class.java).apply {
            putExtra(ExtraId, task.id)
            putExtra(ExtraDescription, task.description)
        }

        val timeInMillis = task.dueDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        logger.debug {
            """
            
            Current time     : ${LocalDateTime.now()}
            Task due at      : ${task.dueDate}
            Schedule alarm at: $timeInMillis
            
        """.prependIndent("     ")
        }

        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            timeInMillis,
            PendingIntent.getBroadcast(context, 0, intent, 0)
        )
    }
}