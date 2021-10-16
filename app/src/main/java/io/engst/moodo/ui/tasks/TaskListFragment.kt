package io.engst.moodo.ui.tasks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.coroutineScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import io.engst.moodo.R
import io.engst.moodo.databinding.FragmentTaskListBinding
import io.engst.moodo.model.types.DateShift
import io.engst.moodo.model.types.Task
import io.engst.moodo.shared.Logger
import io.engst.moodo.shared.injectLogger
import io.engst.moodo.ui.tasks.edit.TaskEditFragment
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class TaskListFragment : Fragment() {
    private val logger: Logger by injectLogger("view")

    private val viewModel: TaskListViewModel by viewModel()

    private var taskListAdapter: TaskListAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentTaskListBinding.inflate(inflater, container, false)

        taskListAdapter = TaskListAdapter {
            showTaskEditPopup(it)
        }

        binding.taskList.apply {
            adapter = taskListAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }

        binding.taskAddButton.setOnClickListener {
            showTaskEditPopup()
            // findNavController().navigate(R.id.action_taskListFragment_to_taskEditFragment) // crash: Navigation destination io.engst.moodo:id/taskFragment referenced from action io.engst.moodo:id/action_taskListFragment_to_taskEditFragment cannot be found from the current destination Destination(io.engst.moodo:id/tasklistFragment)
        }

        val swipeHandler = object : SwipeTaskCallback(requireContext()) {
            override fun onDone(position: Int) {
                val taskViewHolder =
                    binding.taskList.findViewHolderForAdapterPosition(position) as TaskListAdapter.TaskViewHolder
                val task = taskViewHolder.task
                viewModel.setDone(task!!)
            }

            override fun onShift(position: Int, shiftBy: DateShift) {
                val taskViewHolder =
                    binding.taskList.findViewHolderForAdapterPosition(position) as TaskListAdapter.TaskViewHolder
                val task = taskViewHolder.task
                viewModel.shift(task!!, shiftBy)
                binding.taskList.adapter!!.notifyDataSetChanged()
            }

            override fun onDelete(position: Int) {
                val taskViewHolder =
                    binding.taskList.findViewHolderForAdapterPosition(position) as TaskListAdapter.TaskViewHolder
                val task = taskViewHolder.task

                activity?.findViewById<View>(R.id.activity_root_layout)?.let { view ->
                    Snackbar
                        .make(view, "Task deleted", Snackbar.LENGTH_LONG)
                        .setAction("Undo") {
                            viewModel.undoDelete(task!!)
                        }
                        .show()
                }

                viewModel.delete(task!!)
            }
        }

        ItemTouchHelper(swipeHandler).attachToRecyclerView(binding.taskList)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycle.coroutineScope.launch {
            viewModel.tasks.collect { list ->
                taskListAdapter?.submitList(list) {
                    logger.debug { "list items changed: ${list.map { it.id }}" }
                }
            }
        }
    }

    private fun showTaskEditPopup(task: Task? = null) {
        val sheet = TaskEditFragment(task)
        sheet.show(requireActivity().supportFragmentManager, "taskEdit")
    }
}