package io.engst.moodo.ui.tasks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.coroutineScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
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
import kotlinx.coroutines.flow.collect
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class TaskListFragment : Fragment() {

    private val logger: Logger by injectLogger("view")

    private val viewModel: TaskListViewModel by sharedViewModel()
    private val navigationArguments: TaskListFragmentArgs by navArgs()
    lateinit var binding: FragmentTaskListBinding
    lateinit var listAdapter: ListItemAdapter

    private val clickListener = object : ListItemClickListener {
        override fun onClick(task: Task) {
            onTaskEditClicked(task.id)
        }
    }

    private val swipeListener = object : ListItemSwipeListener {
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

    private val dragListener = object : ListItemDragListener {
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

        listAdapter = ListItemAdapter(clickListener, swipeListener, dragListener).apply {
            stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        }

        binding.taskList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = listAdapter
        }

        binding.taskAddButton.setOnClickListener {
            logger.debug { "taskAddButton clicked" }
            onTaskEditClicked()
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
                    viewModel.scrollFirstTime()
                }
            }
        }

        lifecycle.coroutineScope.launchWhenResumed {
            viewModel.scrollFirstTime.collect {
                logger.debug { "scrollFirstTime" }

                var smooth = false

                // retrieve task index
                val taskId = navigationArguments.taskId.takeIf { it > -1L }
                val taskIndex = taskId?.let { taskId ->
                    listAdapter.getCurrentItems()
                        .map { it.id }
                        .indexOf(taskId.toString())
                        .takeIf { it > -1 }
                }

                // fallback to Today item
                val index = if (taskIndex == null) {
                    smooth = true
                    listAdapter.getCurrentItems()
                        .map { it.id }
                        .indexOf(Group.Today.name)
                        .takeIf { it > -1 }
                } else {
                    taskIndex
                }

                // actual scroll
                if (index != null) {
                    scrollTo(index, smooth)
                }

                // show dialog if task id provided
                if (taskId != null) {
                    onTaskEditClicked(taskId)
                }
            }
        }

        lifecycle.coroutineScope.launchWhenResumed {
            viewModel.scrollToday.collect {
                logger.debug { "scrollToday" }
                val index = listAdapter.getCurrentItems().map { it.id }.indexOf(Group.Today.name)
                if (index > -1) {
                    scrollTo(index, true)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.forceListUpdate()
    }

    private fun scrollTo(index: Int, smooth: Boolean) {
        logger.debug { "scrollTo: index=$index smooth=$smooth" }
        if (index > -1) {
            if (smooth) {
                binding.taskList.layoutManager?.startSmoothScroll(
                    object : LinearSmoothScroller(context) {
                        override fun getVerticalSnapPreference(): Int = SNAP_TO_START
                    }.apply {
                        targetPosition = index
                    }
                )
            } else {
                binding.taskList.layoutManager?.scrollToPosition(index)
            }
        }
    }

    private fun onTaskEditClicked(taskId: Long? = null) {
        logger.debug { "showTaskEditPopup #$taskId" }
        val navController =
            activity?.supportFragmentManager?.findFragmentById(R.id.nav_host_fragment)
                ?.findNavController()

        /**
         * Fix crash: java.lang.IllegalArgumentException: Navigation action/destination io.engst.moodo:id/action_taskListFragment_to_taskEditFragment cannot be found from the current destination Destination(io.engst.moodo:id/taskEditFragment) label=Edit task
         *
         * Occurs if very fast clicks happen on one list item (the second click leads to crash...)
         */
        navController?.navigateUp()

        navController?.navigate(
            TaskListFragmentDirections.actionTaskListFragmentToTaskEditFragment(taskId ?: -1L)
        )
    }
}