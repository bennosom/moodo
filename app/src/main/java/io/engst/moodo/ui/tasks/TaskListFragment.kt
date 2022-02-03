package io.engst.moodo.ui.tasks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.coroutineScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import io.engst.moodo.R
import io.engst.moodo.databinding.FragmentTaskListBinding
import io.engst.moodo.model.types.DateShift
import io.engst.moodo.model.types.Task
import io.engst.moodo.shared.Logger
import io.engst.moodo.shared.injectLogger
import io.engst.moodo.ui.LifecycleEventLogger
import io.engst.moodo.ui.tasks.edit.TaskEditFragment
import kotlinx.coroutines.flow.collect
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class TaskListFragment : Fragment() {

    private val logger: Logger by injectLogger("view")

    private val viewModel: TaskListViewModel by sharedViewModel()
    lateinit var binding: FragmentTaskListBinding
    lateinit var listAdapter: TaskListAdapter

    private val clickListener = object : TaskItemClickListener {
        override fun onClick(task: Task) {
            showTaskEditPopup(task)
        }
    }

    private val swipeListener = object : TaskItemSwipeListener {
        override fun onDone(task: Task) {
            logger.debug { "onDone #${task.id}" }
            viewModel.done(task)
        }

        override fun onShift(task: Task, shiftBy: DateShift) {
            logger.debug { "onShift #${task.id} shiftBy=$shiftBy" }
            if (task.isDone) {
                viewModel.undone(task)
            } else {
                viewModel.shiftBy(task, shiftBy)
            }
        }

        override fun onRemoved(task: Task) {
            logger.debug { "onDelete #${task.id}" }
            activity?.findViewById<View>(R.id.activity_root_layout)?.let { view ->
                Snackbar
                    .make(view, "Task deleted", Snackbar.LENGTH_LONG)
                    .setAction("Undo") {
                        viewModel.undoDelete(task)
                    }
                    .show()
            }
            viewModel.delete(task)
        }
    }

    private val dragListener = object : TaskItemDragListener {
        override fun canDrag(dragTask: Task): Boolean {
            //logger.debug { "canDrag #${dragTask.id}" }
            return true
        }

        override fun onDragStart(dragTask: Task) {
            //logger.debug { "onDragStart #${dragTask.id}" }
        }

        override fun canDrop(dragTask: Task, dropTask: Task): Boolean {
            //logger.debug { "canDrop #${dragTask.id} <-> #${dropTask.id}" }
            return true
        }

        override fun onDrop(dragTask: Task, dropTask: Task) {
            //logger.debug { "onDrop #${dragTask.id} <-> #${dropTask.id}" }
            viewModel.updateOrder(listAdapter.getCurrentItems()
                .filterIsInstance<ListItem.TaskItem>()
                .map { it.id.toLong() })
        }
    }

    init {
        lifecycle.addObserver(LifecycleEventLogger("TaskListFragment"))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTaskListBinding.inflate(inflater, container, false)

        listAdapter = TaskListAdapter(clickListener, swipeListener, dragListener).apply {
            stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        }

        binding.taskList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = listAdapter
        }

        binding.taskAddButton.setOnClickListener {
            logger.debug { "taskAddButton clicked" }

            /*
            findNavController().navigate(R.id.action_taskListFragment_to_taskEditFragment)
            crashes with Navigation destination io.engst.moodo:id/taskFragment referenced from action io.engst.moodo:id/action_taskListFragment_to_taskEditFragment cannot be found from the current destination Destination(io.engst.moodo:id/tasklistFragment)

            TODO: Check this:
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.content, fragment)
                .addToBackStack(null)
                .commit();
            */
            showTaskEditPopup()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycle.coroutineScope.launchWhenResumed {
            viewModel.tasks.collect { list ->
                logger.debug { "tasks=${list.map { it.id }}" }
                listAdapter.submitList(list)

                if (list.isNotEmpty()) {
                    viewModel.scrollTodayFirstTimeOnly()
                }
            }
        }

        lifecycle.coroutineScope.launchWhenResumed {
            viewModel.scrollToday.collect {
                scrollToToday()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.forceListUpdate()
    }

    private fun scrollToToday() {
        val todayIndex = listAdapter.getCurrentItems().map { it.id }.indexOf(Group.Today.name)
        if (todayIndex > -1) {
            binding.taskList.layoutManager?.startSmoothScroll(
                object : LinearSmoothScroller(context) {
                    override fun getVerticalSnapPreference(): Int = SNAP_TO_START
                }.apply {
                    targetPosition = todayIndex
                }
            )
        }
    }

    private fun showTaskEditPopup(task: Task? = null) {
        logger.debug { "showTaskEditPopup task=$task" }
        TaskEditFragment.show(requireActivity().supportFragmentManager, task)
    }
}