package io.engst.moodo.ui.tasks.task

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.engst.moodo.R
import io.engst.moodo.databinding.FragmentTaskEditBinding
import io.engst.moodo.headless.AlarmBroadcastReceiver
import io.engst.moodo.model.api.DateShift
import io.engst.moodo.model.api.ExtraDescription
import io.engst.moodo.model.api.ExtraId
import io.engst.moodo.model.api.Task
import io.engst.moodo.shared.Logger
import io.engst.moodo.shared.injectLogger
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class TaskEditFragment(val task: Task?) : BottomSheetDialogFragment() {

    private val logger: Logger by injectLogger()

    private val viewModel: TaskViewModel by viewModel()

    private lateinit var binding: FragmentTaskEditBinding

    private var saveOnDismiss: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)

        if (task != null) {
            viewModel.description = task.description
            viewModel.dueDate = task.dueDate
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTaskEditBinding.inflate(inflater, container, false)

        val chipGroup = binding.root.findViewById<ChipGroup>(R.id.due_date_chips)
        DateShift.values().forEach {
            val chip = Chip(
                ContextThemeWrapper(
                    requireContext(),
                    R.style.Widget_MaterialComponents_Chip_Choice
                )
            ).apply {
                text = it.name
            }
            chipGroup.addView(chip)
        }
        val chip = Chip(
            ContextThemeWrapper(
                requireContext(),
                R.style.Widget_MaterialComponents_Chip_Choice
            )
        ).apply {
            text = "specify exact date"
            setOnClickListener {
                MaterialAlertDialogBuilder(context)
                    // Add customization options here
                    .setTitle("TODO: select date and time by picker")
                    .show()
            }
        }
        chipGroup.addView(chip)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        task?.let {
            binding.removeButton.text = "Delete"
            binding.removeButton.setTextColor(Color.RED)
        } ?: Unit.let {
            binding.removeButton.text = "Cancel"
            binding.removeButton.setTextColor(Color.BLACK)
        }
        binding.descriptionText.editText?.setText(viewModel.description)
        binding.calendarView.updateDate(
            viewModel.dueDate.year,
            viewModel.dueDate.monthValue - 1,
            viewModel.dueDate.dayOfMonth
        )
        binding.timePicker.hour = viewModel.dueDate.hour
        binding.timePicker.minute = viewModel.dueDate.minute

        binding.descriptionText.editText?.doOnTextChanged { text, _, _, _ ->
            viewModel.description = text.toString()
        }

        binding.removeButton.setOnClickListener {
            saveOnDismiss = false
            dismiss()
        }

        binding.calendarView.setOnDateChangedListener { _, year, monthOfYear, dayOfMonth ->
            val date = LocalDateTime.of(
                LocalDate.of(year, monthOfYear + 1, dayOfMonth),
                viewModel.dueDate.toLocalTime()
            )
            viewModel.dueDate = date
        }

        binding.timePicker.setIs24HourView(true)
        binding.timePicker.setOnTimeChangedListener { _, hour, minute ->
            val date = LocalDateTime.of(
                viewModel.dueDate.toLocalDate(),
                LocalTime.of(hour, minute)
            )
            viewModel.dueDate = date
        }

        binding.descriptionText.editText?.requestFocus()

        val inputMethodManager: InputMethodManager? =
            requireContext().getSystemService(InputMethodManager::class.java)
        inputMethodManager?.showSoftInput(
            binding.descriptionText.editText,
            InputMethodManager.SHOW_IMPLICIT
        )
    }

    override fun onResume() {
        saveOnDismiss = true
        super.onResume()
    }

    override fun onDismiss(dialog: DialogInterface) {
        if (saveOnDismiss) {
            if (viewModel.description.isNotBlank()) {
                saveTask()
            } else {
                deleteTask()
            }
        } else {
            deleteTask()
        }
        super.onDismiss(dialog)
    }

    private fun saveTask() {
        val task = if (task != null) {
            val updatedTask = task.copy(
                description = viewModel.description,
                dueDate = viewModel.dueDate
            )
            viewModel.updateTask(updatedTask)
            // TODO: unschedule former task
            updatedTask
        } else {
            val newTask = Task(
                description = viewModel.description,
                createdDate = LocalDateTime.now(),
                dueDate = viewModel.dueDate
            )
            viewModel.addTask(newTask)
            newTask
        }
        schedule(task)
    }

    private fun deleteTask() {
        if (task != null) {
            viewModel.deleteTask(task)
            // TODO: unschedule deleted task
        }
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