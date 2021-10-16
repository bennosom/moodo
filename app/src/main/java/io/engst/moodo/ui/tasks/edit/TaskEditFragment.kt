package io.engst.moodo.ui.tasks.edit

import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.text.format.DateFormat.is24HourFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import io.engst.moodo.R
import io.engst.moodo.databinding.FragmentTaskEditBinding
import io.engst.moodo.model.types.DateSuggestion
import io.engst.moodo.model.types.Task
import io.engst.moodo.model.types.TimeSuggestion
import io.engst.moodo.model.types.textId
import io.engst.moodo.shared.Logger
import io.engst.moodo.shared.injectLogger
import io.engst.moodo.shared.prettyFormat
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class TaskEditFragment(val task: Task?) : BottomSheetDialogFragment() {

    private val logger: Logger by injectLogger("view")

    private val viewModel: TaskEditViewModel by viewModel()

    private lateinit var binding: FragmentTaskEditBinding

    private var saveOnDismiss: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)

        if (task != null) {
            viewModel.taskText = task.description
            viewModel.taskDate = task.dueDate?.toLocalDate()
            viewModel.taskTime = task.dueDate?.toLocalTime()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTaskEditBinding.inflate(inflater, container, false)

        updateChips()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        task?.let {
            binding.removeButton.text = getString(R.string.task_button_delete)
            binding.removeButton.setTextColor(Color.RED)
        } ?: Unit.let {
            binding.removeButton.text = getString(R.string.task_button_cancel)
            binding.removeButton.setTextColor(Color.BLACK)
        }
        binding.descriptionText.editText?.setText(viewModel.taskText)
        binding.descriptionText.editText?.doOnTextChanged { text, _, _, _ ->
            viewModel.taskText = text.toString()
        }

        binding.removeButton.setOnClickListener {
            saveOnDismiss = false
            dismiss()
        }

        binding.descriptionText.editText?.requestFocus()

        requireContext().getSystemService(InputMethodManager::class.java)?.run {
            showSoftInput(
                binding.descriptionText.editText,
                InputMethodManager.SHOW_IMPLICIT
            )
        }
    }

    override fun onResume() {
        saveOnDismiss = true
        super.onResume()
    }

    override fun onDismiss(dialog: DialogInterface) {
        if (saveOnDismiss) {
            if (viewModel.taskText != null && viewModel.taskText!!.isNotBlank()) {
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
        val dueDate = if (viewModel.taskDate != null) {
            val dueTime = viewModel.taskTime ?: LocalTime.of(9, 0)
            LocalDateTime.of(viewModel.taskDate, dueTime)
        } else {
            null
        }

        if (task != null) {
            logger.info { "save modified task #${task.id}" }

            val updatedTask = task.copy(
                description = viewModel.taskText ?: "",
                dueDate = dueDate
            )

            viewModel.updateTask(updatedTask)
        } else {
            logger.info { "save new task" }

            val newTask = Task(
                description = viewModel.taskText ?: "",
                createdDate = LocalDateTime.now(),
                dueDate = dueDate
            )

            viewModel.addTask(newTask)
        }
    }

    private fun deleteTask() {
        if (task != null) {
            logger.info { "delete task #${task.id}" }
            viewModel.deleteTask(task)
        }
    }

    private fun updateChips() {
        binding.root.findViewById<ChipGroup>(R.id.due_date_chips).run {
            removeAllViews()

            viewModel.taskDate?.let { dueDate ->
                val dateChip = layoutInflater.inflate(
                    R.layout.task_edit_duedate_chip,
                    null,
                    false
                ) as Chip
                addView(dateChip.apply {
                    id = DateSuggestion.Custom.ordinal
                    text = dueDate.prettyFormat
                    isCheckable = false
                    setOnCloseIconClickListener {
                        viewModel.taskDate = null
                        viewModel.taskTime = null
                        updateChips()
                    }
                    setOnClickListener { showDatePicker(viewModel.taskDate ?: LocalDate.now()) }
                })

                // add time suggestions only if date was set
                viewModel.taskTime?.let { dueTime ->
                    val timeChip = layoutInflater.inflate(
                        R.layout.task_edit_duedate_chip,
                        null,
                        false
                    ) as Chip
                    addView(timeChip.apply {
                        id = TimeSuggestion.Custom.ordinal
                        text = dueTime.prettyFormat
                        isCheckable = false
                        setOnCloseIconClickListener {
                            viewModel.taskTime = null
                            viewModel.taskDate = null
                            updateChips()
                        }
                        setOnClickListener { showTimePicker(viewModel.taskTime ?: LocalTime.now()) }
                    })
                } ?: Unit.let {
                    TimeSuggestion.values().forEach { suggestion ->
                        val timeChip = layoutInflater.inflate(
                            R.layout.task_edit_duedate_suggestion_chip,
                            null,
                            false
                        ) as Chip
                        addView(timeChip.apply {
                            id = suggestion.ordinal
                            text = getString(suggestion.textId)
                            isCloseIconVisible = false
                            isSelected = suggestion == TimeSuggestion.Morning
                            isCheckable = false
                            if (suggestion == TimeSuggestion.Custom) {
                                setOnClickListener {
                                    showTimePicker(viewModel.taskTime ?: LocalTime.now())
                                    updateChips()
                                }
                            } else {
                                setOnClickListener {
                                    viewModel.taskTime = getSuggestedTime(suggestion)
                                    updateChips()
                                }
                            }
                        })
                    }
                }
            } ?: Unit.let {
                DateSuggestion.values().forEach { suggestion ->
                    val dateChip = layoutInflater.inflate(
                        R.layout.task_edit_duedate_suggestion_chip,
                        null,
                        false
                    ) as Chip
                    addView(dateChip.apply {
                        id = suggestion.ordinal
                        text = getString(suggestion.textId)
                        isCloseIconVisible = false
                        isCheckable = false
                        if (suggestion == DateSuggestion.Custom) {
                            setOnClickListener {
                                showDatePicker(viewModel.taskDate ?: LocalDate.now())
                            }
                        } else {
                            setOnClickListener {
                                viewModel.taskDate = getSuggestedDate(suggestion)
                                updateChips()
                            }
                        }
                    })
                }
            }
        }
    }

    private fun getSuggestedDate(suggestion: DateSuggestion): LocalDate {
        return when (suggestion) {
            DateSuggestion.Tomorrow -> LocalDate.now().plusDays(1)
            DateSuggestion.In2Days -> LocalDate.now().plusDays(2)
            DateSuggestion.NextMonday -> LocalDate.now().plusWeeks(1).with(DayOfWeek.MONDAY)
            else -> throw IllegalArgumentException("invalid date")
        }
    }

    private fun getSuggestedTime(suggestion: TimeSuggestion): LocalTime {
        return when (suggestion) {
            TimeSuggestion.Morning -> LocalTime.of(9, 0)
            TimeSuggestion.Midday -> LocalTime.of(12, 0)
            TimeSuggestion.Afternoon -> LocalTime.of(17, 0)
            else -> throw IllegalArgumentException("invalid time")
        }
    }

    private fun showDatePicker(dueDate: LocalDate) {
        val millis = LocalDateTime.of(
            dueDate,
            LocalTime.of(0, 0)
        ).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val picker = MaterialDatePicker.Builder
            .datePicker()
            .setSelection(millis)
            .setTitleText(R.string.due_date_picker_title)
            .build()
        picker.addOnPositiveButtonClickListener { selection ->
            val updatedDate =
                Instant.ofEpochMilli(selection).atZone(ZoneId.systemDefault()).toLocalDate()
            viewModel.taskDate = updatedDate
            updateChips()
        }
        picker.addOnNegativeButtonClickListener {
            // call back code
        }
        picker.addOnCancelListener {
            // call back code
        }
        picker.addOnDismissListener {
            // call back code
        }
        picker.show(parentFragmentManager, "date-picker")
    }

    private fun showTimePicker(dueTime: LocalTime) {
        val clockFormat =
            if (is24HourFormat(context)) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(clockFormat)
            .setHour(dueTime.hour)
            .setMinute(dueTime.minute)
            .setTitleText(R.string.due_date_picker_title)
            .build()
        picker.addOnPositiveButtonClickListener {
            val updatedTime = LocalTime.of(picker.hour, picker.minute)
            viewModel.taskTime = updatedTime
            updateChips()
        }
        picker.addOnNegativeButtonClickListener {
            // call back code
        }
        picker.addOnCancelListener {
            // call back code
        }
        picker.addOnDismissListener {
            // call back code
        }
        picker.show(parentFragmentManager, "time-picker")
    }
}