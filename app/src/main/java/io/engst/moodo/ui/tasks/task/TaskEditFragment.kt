package io.engst.moodo.ui.tasks.task

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.view.ContextThemeWrapper
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentTaskEditBinding.inflate(inflater, container, false)

        task?.let {
            viewModel.description = task.description
            viewModel.dueDate = task.dueDate
        }

        binding.descriptionText.setText(viewModel.description)
        binding.addButton.text = getString(R.string.task_modify)
        binding.calendarView.updateDate(
            viewModel.dueDate.year,
            viewModel.dueDate.monthValue - 1,
            viewModel.dueDate.dayOfMonth
        )
        binding.timePicker.hour = viewModel.dueDate.hour
        binding.timePicker.minute = viewModel.dueDate.minute

        binding.descriptionText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.description = s.toString()
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        binding.addButton.setOnClickListener {
            saveTask()
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

        return binding.root
    }


    override fun onDismiss(dialog: DialogInterface) {
        saveTask()
        super.onDismiss(dialog)
    }

    private fun saveTask() {
        if (task != null) {
            val updatedTask = task.copy(
                description = viewModel.description,
                dueDate = viewModel.dueDate
            )
            viewModel.updateTask(updatedTask)
            schedule(updatedTask)
        } else {
            val newTask =
                Task(null, viewModel.description, viewModel.dueDate, viewModel.dueDate, null, 0, 0)
            viewModel.addTask(newTask)
            schedule(newTask)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val chipGroup = view.findViewById<ChipGroup>(R.id.due_date_chips)
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