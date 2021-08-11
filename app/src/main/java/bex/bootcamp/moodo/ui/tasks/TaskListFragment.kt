package bex.bootcamp.moodo.ui.tasks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import bex.bootcamp.moodo.databinding.FragmentTaskListBinding
import bex.bootcamp.moodo.model.api.DateShift
import bex.bootcamp.moodo.model.api.Task
import bex.bootcamp.moodo.shared.Logger
import bex.bootcamp.moodo.shared.injectLogger
import bex.bootcamp.moodo.ui.tasks.task.TaskEditFragment
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.time.LocalDateTime
import java.time.LocalTime

class TaskListFragment : Fragment() {
    private val logger: Logger by injectLogger()

    private val viewModel: TaskListViewModel by viewModel()

    private var taskListAdapter: TaskListAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.tasks.observe(viewLifecycleOwner) { updateTaskList(it) }
    }

    private fun updateTaskList(tasks: List<Task>) {
        logger.debug { "updateTaskList $tasks" }
        val group = DateGroup()

        val doneList = mutableListOf<ListItem>()
        val todoList = mutableListOf<ListItem>()
        val groups = mutableSetOf<ListItem>()

        tasks.forEach { task ->
            if (task.doneDate == null) {
                val groupDate = group.getDateGroupForDate(task.dueDate.toLocalDate())
                groups.add(HeaderListItem(-1, LocalDateTime.of(groupDate, LocalTime.MIN)))
                todoList.add(TodoListItem(task.id!!, task))
            } else {
                doneList.add(TodoListItem(task.id!!, task))
            }
        }

        doneList.sortBy {
            (it as TodoListItem).task.doneDate
        }

        todoList.addAll(groups)
        todoList.sortBy {
            when (it) {
                is TodoListItem -> it.task.dueDate
                is HeaderListItem -> it.date
            }
        }

        val items = doneList + todoList
        logger.debug { "updateTaskList $items" }
        taskListAdapter?.submitList(items)
    }

    private fun showTaskEditPopup(task: Task? = null) {
        val sheet = TaskEditFragment(task)
        sheet.show(requireActivity().supportFragmentManager, "foo")
    }

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
        }

        binding.taskAddButton.setOnClickListener {
            showTaskEditPopup()
        }

        val swipeHandler = object : SwipeTaskCallback(requireContext()) {
            override fun onDone(position: Int) {
                val taskViewHolder =
                    binding.taskList.findViewHolderForAdapterPosition(position) as TaskListAdapter.TaskViewHolder
                val task = taskViewHolder.task
                viewModel.resolve(task!!)
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
                Snackbar
                    .make(binding.root, "Task deleted", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show()
                viewModel.delete(task!!)
            }
        }

        ItemTouchHelper(swipeHandler).attachToRecyclerView(binding.taskList)

        return binding.root
    }
}
