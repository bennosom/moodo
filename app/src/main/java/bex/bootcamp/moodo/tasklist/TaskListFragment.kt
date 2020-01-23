package bex.bootcamp.moodo.tasklist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import bex.bootcamp.moodo.R
import bex.bootcamp.moodo.databinding.FragmentTaskListBinding

class TaskListFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var binding: FragmentTaskListBinding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_task_list, container, false
            )

        val application = requireNotNull(this.activity).application

        val viewModelFactory = ListViewModelFactory(application)

        val viewModel =
            ViewModelProviders.of(this, viewModelFactory).get(TaskListViewModel::class.java)

        binding.viewModel = viewModel

        val adapter = TaskListAdapter()
        binding.mainList.adapter = adapter

        viewModel.tasks.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submit(it)
            }
        })

        binding.setLifecycleOwner(this)
        binding.mainList.layoutManager = LinearLayoutManager(context)
        binding.mainList.addItemDecoration(
            DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        )

        val swipeHandler = object : SwipeTaskCallback(binding.root.context) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDirection: Int) {
                //adapter.notifyDataSetChanged()
            }
        }

        ItemTouchHelper(swipeHandler).attachToRecyclerView(binding.mainList)

        return binding.root
    }
}
