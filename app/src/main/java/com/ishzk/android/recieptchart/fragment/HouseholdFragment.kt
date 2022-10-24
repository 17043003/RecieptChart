package com.ishzk.android.recieptchart.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.*
import com.ishzk.android.recieptchart.R
import com.ishzk.android.recieptchart.SharedPreference
import com.ishzk.android.recieptchart.databinding.FragmentHouseholdBinding
import com.ishzk.android.recieptchart.databinding.ItemRecyclerviewBinding
import com.ishzk.android.recieptchart.model.Household
import com.ishzk.android.recieptchart.repository.FirestoreRepository
import com.ishzk.android.recieptchart.viewmodel.HouseholdViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HouseholdFragment: Fragment() {
    private var _binding: FragmentHouseholdBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<HouseholdViewModel>()

    private val listAdapter: ListItemAdapter by lazy { ListItemAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHouseholdBinding.inflate(layoutInflater, container, false)

        binding.lifecycleOwner = this
        binding.viewModel = this.viewModel

        binding.newHouseholdButton.setOnClickListener { view ->
            view.findNavController().
            navigate(HouseholdFragmentDirections.actionHouseholdFragmentToNewHouseholdFragment())
        }

        viewModel._userID = SharedPreference(requireActivity())
            .getValue(getString(R.string.preference_file_key), getString(R.string.user_id))

        // set using repository to fetch data.
        viewModel._repository = FirestoreRepository()

        // init recycler view.
        binding.recyclerView.apply {
            adapter = listAdapter
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)

            val decorator = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            addItemDecoration(decorator)
        }

        // set initial date to select year month custom view.
        with(binding.selectYearMonth) {
            val today = Date()
            selectedYear.value = today.year + 1900
            selectedMonth.value = today.month + 1
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // fetch items on recycler view.
        binding.selectYearMonth.selectedMonth.observe(viewLifecycleOwner){
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.isFetching.value = true

                viewModel.fetchMonthlyItems(binding.selectYearMonth.selectedDate).collect{
                    Log.d(TAG, "Fetch date: ${binding.selectYearMonth.selectedDate}")
                    listAdapter.submitList(it)
                }

                viewModel.isFetching.value = false
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
    }

    companion object {
        const val TAG = "HouseholdFragment"
    }
}

class ListItemAdapter: ListAdapter<Household, ItemListViewHolder>(DIFF_UTIL_ITEM_CALLBACK){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemListViewHolder {
        val view = ItemRecyclerviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ItemListViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemListViewHolder, position: Int) {
        holder.bind(getItem(position))
        holder.setOnClickListener(getItem(position).id)
    }
}

class ItemListViewHolder(private val binding: ItemRecyclerviewBinding):
    RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Household){
            binding.apply {
                val formatter = SimpleDateFormat("yyyy/MM/dd")
                itemDateText.text = formatter.format(item.date.toDate())
                itemCostText.text = item.cost.toString()
                itemKindText.text = item.kind
                itemDescriptionText.text = item.description
            }
        }
        fun setOnClickListener(id: String){
            binding.root.setOnClickListener {
                it.findNavController().
                navigate(HouseholdFragmentDirections.actionHouseholdFragmentToNewHouseholdFragment(id))
            }
        }
}

val DIFF_UTIL_ITEM_CALLBACK = object : DiffUtil.ItemCallback<Household>() {
    override fun areContentsTheSame(oldItem: Household, newItem: Household): Boolean {
        return oldItem == newItem
    }

    override fun areItemsTheSame(oldItem: Household, newItem: Household): Boolean {
        return oldItem.id == newItem.id
    }
}