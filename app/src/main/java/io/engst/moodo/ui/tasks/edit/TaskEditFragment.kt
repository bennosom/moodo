package io.engst.moodo.ui.tasks.edit

import android.app.Dialog
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.format.DateFormat.is24HourFormat
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.core.content.getSystemService
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
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
import io.engst.moodo.ui.LifecycleEventLogger
import io.engst.moodo.ui.prettyFormat
import io.engst.moodo.ui.tasks.TaskListGroupHelper
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


inline fun View.afterMeasured(crossinline block: () -> Unit) {
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            if (measuredWidth > 0 && measuredHeight > 0) {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                block()
            }
        }
    })
}

class TaskEditFragment private constructor() : BottomSheetDialogFragment() {

    private val logger: Logger by injectLogger(TaskEditFragment::class.simpleName)
    private lateinit var binding: FragmentTaskEditBinding
    private val viewModel: TaskEditViewModel by viewModel()
    private var task: Task? = null

    companion object {
        private var instance: TaskEditFragment? = null

        fun show(fragmentManager: FragmentManager, task: Task?) {
            instance?.let {
                it.dismiss()
                instance = null
            }
            instance = TaskEditFragment().apply {
                this.task = task
                show(fragmentManager, TaskEditFragment::class.simpleName)
            }
        }
    }

    init {
        lifecycle.addObserver(LifecycleEventLogger("TaskEditFragment"))
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return (super.onCreateDialog(savedInstanceState) as BottomSheetDialog).apply {
            setOnShowListener {
                findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)?.let { frameLayout ->
                    BottomSheetBehavior.from(frameLayout).state = BottomSheetBehavior.STATE_EXPANDED
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.originalTask = task
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTaskEditBinding.inflate(inflater, container, false)

        updateDateChips()
        initActions()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.originalTask?.let {
            binding.textDone.isVisible = it.isDone
            binding.textDone.text = getString(R.string.task_done_at, it.doneDate?.prettyFormat)
        }

        binding.descriptionText.editText?.setText(viewModel.description)
        binding.descriptionText.editText?.doOnTextChanged { text, _, _, _ ->
            viewModel.description = text.toString()
        }
        binding.descriptionText.editText?.requestFocus()
        binding.descriptionText.editText?.setOnTouchListener { editTextView, event ->
            if (editTextView.hasFocus()) {
                editTextView.parent.requestDisallowInterceptTouchEvent(true)
                when (event.actionMasked) {
                    MotionEvent.ACTION_SCROLL -> {
                        editTextView.parent.requestDisallowInterceptTouchEvent(false)
                    }
                }
            }
            false
        }

        requireContext().getSystemService(InputMethodManager::class.java)?.run {
            showSoftInput(
                binding.descriptionText.editText,
                InputMethodManager.SHOW_IMPLICIT
            )
        }

        logger.debug { "bexx1 ${view.height} ${view.measuredHeight}" }
        requireActivity().findViewById<View>(android.R.id.content).rootView.afterMeasured {
            logger.debug { "bexx2 ${view.height} ${view.measuredHeight}" }
        }
    }

    private fun initActions() {
        if (viewModel.originalTask == null) {
            binding.buttonDelete.apply {
                text = getString(R.string.task_button_cancel)
                setTextColor(context.getColor(R.color.textOnSurface))
                setOnClickListener {
                    dismiss()
                }
            }

            binding.buttonDone.isVisible = false

            binding.buttonSave.apply {
                text = getString(R.string.task_button_add)
                setOnClickListener {
                    viewModel.addNewTask()
                    viewModel.description = ""
                    viewModel.dueDate = null
                    viewModel.dueTime = null
                    binding.descriptionText.editText?.setText(viewModel.description)
                    updateDateChips()
                    context.getSystemService<Vibrator>()
                        ?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
                }
            }
        } else {
            binding.buttonDelete.setOnClickListener {
                viewModel.deleteTask()
                dismiss()
            }

            binding.buttonDone.setOnClickListener {
                viewModel.setTaskDone()
                dismiss()
            }

            binding.buttonSave.setOnClickListener {
                viewModel.saveChanges()
                dismiss()
            }
        }
    }

    private fun updateDateChips() {
        val helper = TaskListGroupHelper(LocalDateTime.now(viewModel.clock), viewModel.locale)

        binding.root.findViewById<ChipGroup>(R.id.due_date_chips).run {
            removeAllViews()

            viewModel.dueDate?.let { dueDate ->
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
                        viewModel.dueDate = null
                        viewModel.dueTime = null
                        updateDateChips()
                    }
                    setOnClickListener { showDatePicker(viewModel.dueDate ?: LocalDate.now()) }
                })

                // add time suggestions only if date was set
                viewModel.dueTime?.let { dueTime ->
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
                            viewModel.dueTime = null
                            viewModel.dueDate = null
                            updateDateChips()
                        }
                        setOnClickListener { showTimePicker(viewModel.dueTime ?: LocalTime.now()) }
                    })
                } ?: Unit.let {
                    val formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

                    TimeSuggestion.values().forEach { suggestion ->
                        val timeChip = layoutInflater.inflate(
                            R.layout.task_edit_duedate_suggestion_chip,
                            null,
                            false
                        ) as Chip
                        addView(timeChip.apply {
                            id = suggestion.ordinal
                            text = when (suggestion) {
                                TimeSuggestion.Custom -> getString(R.string.due_time_custom)
                                else -> formatter.format(getSuggestedTime(suggestion))
                            }
                            isCloseIconVisible = false
                            isSelected = suggestion == TimeSuggestion.Morning
                            isCheckable = false
                            if (suggestion == TimeSuggestion.Custom) {
                                setOnClickListener {
                                    showTimePicker(viewModel.dueTime ?: LocalTime.now())
                                    updateDateChips()
                                }
                            } else {
                                setOnClickListener {
                                    viewModel.dueTime = getSuggestedTime(suggestion)
                                    updateDateChips()
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
                                showDatePicker(viewModel.dueDate ?: LocalDate.now())
                            }
                        } else {
                            setOnClickListener {
                                viewModel.dueDate = getSuggestedDate(suggestion, helper)

                                // don't ask for time for dates not being today or tomorrow
                                if (viewModel.dueDate == helper.startOfNextWeek
                                    || viewModel.dueDate == helper.later
                                ) {
                                    viewModel.dueTime = getSuggestedTime(TimeSuggestion.Morning)
                                }

                                updateDateChips()
                            }
                        }
                    })
                }
            }
        }
    }

    private fun getSuggestedDate(
        suggestion: DateSuggestion,
        helper: TaskListGroupHelper
    ): LocalDate {
        return when (suggestion) {
            DateSuggestion.Today -> helper.today
            DateSuggestion.Tomorrow -> helper.tomorrow
            DateSuggestion.NextWeek -> helper.startOfNextWeek
            DateSuggestion.Later -> helper.later
            else -> throw IllegalArgumentException("invalid date")
        }
    }

    private fun getSuggestedTime(suggestion: TimeSuggestion): LocalTime {
        return when (suggestion) {
            TimeSuggestion.Morning -> LocalTime.of(9, 0)
            TimeSuggestion.Midday -> LocalTime.of(12, 0)
            TimeSuggestion.Afternoon -> LocalTime.of(15, 0)
            TimeSuggestion.Evening -> LocalTime.of(18, 0)
            else -> throw IllegalArgumentException("invalid time")
        }
    }

    private fun showDatePicker(dueDate: LocalDate) {
        val millis = LocalDateTime.of(dueDate, LocalTime.of(0, 0))
            .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val picker = MaterialDatePicker.Builder.datePicker()
            .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
            .setSelection(millis)
            .setTitleText(R.string.due_date_picker_title)
            .setCalendarConstraints(
                CalendarConstraints.Builder()
                    .setValidator(DateValidatorPointForward.now())
                    .build()
            )
            .build()
        picker.addOnPositiveButtonClickListener { selection ->
            viewModel.dueDate =
                Instant.ofEpochMilli(selection).atZone(ZoneId.systemDefault()).toLocalDate()
            updateDateChips()
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
            .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
            .setHour(dueTime.hour)
            .setMinute(dueTime.minute)
            .setTitleText(R.string.due_date_picker_title)
            .build()
        picker.addOnPositiveButtonClickListener {
            viewModel.dueTime = LocalTime.of(picker.hour, picker.minute)
            updateDateChips()
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