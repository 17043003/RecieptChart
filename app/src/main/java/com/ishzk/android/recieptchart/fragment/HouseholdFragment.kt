package com.ishzk.android.recieptchart.fragment

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.*
import com.ishzk.android.recieptchart.R
import com.ishzk.android.recieptchart.SharedPreference
import com.ishzk.android.recieptchart.databinding.FragmentHouseholdBinding
import com.ishzk.android.recieptchart.databinding.ItemEachDayCardBinding
import com.ishzk.android.recieptchart.databinding.ItemRecyclerviewBinding
import com.ishzk.android.recieptchart.model.Household
import com.ishzk.android.recieptchart.repository.FirestoreRepository
import com.ishzk.android.recieptchart.viewmodel.HouseholdViewModel
import com.ishzk.android.recieptchart.viewmodel.ItemsEachDay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HouseholdFragment: Fragment() {
    private var _binding: FragmentHouseholdBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<HouseholdViewModel>()

    private val listAdapter: ListItemAdapter by lazy { ListItemAdapter(viewModel) }
    private val cardListAdapter: ListCardItemAdapter by lazy { ListCardItemAdapter(viewModel) }

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
            adapter = cardListAdapter
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)

            val decoration = object : RecyclerView.ItemDecoration(){
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    super.getItemOffsets(outRect, view, parent, state)
                    val margin = 24
                    outRect.set(margin / 2, 0, margin / 2, margin)
                }
            }
            addItemDecoration(decoration)
        }

        // set initial date to select year month custom view.
        with(binding.selectYearMonth) {
            val today = Date()
            selectedYear.value = today.year + 1900
            selectedMonth.value = today.month + 1
            selectedDate.value = today
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // fetch items on recycler view.
        binding.selectYearMonth.selectedDate.observe(viewLifecycleOwner){
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.fetchMonthlyItems(it)
            }
        }

        viewModel.fetchedItems.observe(viewLifecycleOwner){
            viewLifecycleOwner.lifecycleScope.launch{
                listAdapter.submitList(it)
            }
        }

        viewModel.fetchedItemsEachDay.observe(viewLifecycleOwner){
            viewLifecycleOwner.lifecycleScope.launch {
                cardListAdapter.submitList(it)
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

class ListItemAdapter(val viewModel: HouseholdViewModel): ListAdapter<Household, ItemListViewHolder>(DIFF_UTIL_ITEM_CALLBACK){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemListViewHolder {
        val view = ItemRecyclerviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ItemListViewHolder(view, viewModel)
    }

    override fun onBindViewHolder(holder: ItemListViewHolder, position: Int) {
        holder.bind(getItem(position))
        holder.setOnClickListener(getItem(position).id)
        holder.setOnLongClickListener(getItem(position).id)
    }
}

class ItemListViewHolder(private val binding: ItemRecyclerviewBinding, private val viewModel: HouseholdViewModel):
    RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Household){
            binding.apply {
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

        fun setOnLongClickListener(id: String){
            binding.root.setOnLongClickListener {
                AlertDialog.Builder(binding.root.context)
                    .setTitle("削除確認")
                    .setMessage("削除してよろしいですか？")
                    .setPositiveButton("OK"){ _, _ ->
                        viewModel.deleteItem(id)
                    }
                    .setNeutralButton("キャンセル"){ _, _ ->
                    }.show()

                true
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


class ListCardItemAdapter(val viewModel: HouseholdViewModel): ListAdapter<ItemsEachDay, CardItemListViewHolder>(DIFF_CARD_ITEM_CALLBACK){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardItemListViewHolder {
        val view = ItemEachDayCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return CardItemListViewHolder(view, viewModel)
    }

    override fun onBindViewHolder(holder: CardItemListViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class CardItemListViewHolder(private val binding: ItemEachDayCardBinding, private val viewModel: HouseholdViewModel):
    RecyclerView.ViewHolder(binding.root) {
    private val listAdapter: ListItemAdapter by lazy { ListItemAdapter(viewModel) }

    init {
        binding.root.setOnClickListener {
            binding.expandableCard.toggle()
        }
    }
    fun bind(item: ItemsEachDay){
        binding.apply {
            val formatter = SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN)
            cardItemDate.text = formatter.format(item.day.toDate())
            totalEachDay.text = item.item.sumOf { it.cost }.toString()
            eachDayItemList.apply {
                adapter = listAdapter
                layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)

                val decorator = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
                addItemDecoration(decorator)
            }
            listAdapter.submitList(item.item)
        }
    }
}

val DIFF_CARD_ITEM_CALLBACK = object : DiffUtil.ItemCallback<ItemsEachDay>() {
    override fun areContentsTheSame(oldItem: ItemsEachDay, newItem: ItemsEachDay): Boolean {
        return oldItem == newItem
    }

    override fun areItemsTheSame(oldItem: ItemsEachDay, newItem: ItemsEachDay): Boolean {
        return oldItem.day == newItem.day
    }
}