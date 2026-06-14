package com.vinscanner.app.ui.list

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.vinscanner.app.data.VinRecord
import com.vinscanner.app.databinding.ItemVinBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VinListAdapter(
    private val onItemClick: (VinRecord, Int) -> Unit,
    private val onItemLongClick: (VinRecord, Int) -> Unit
) : RecyclerView.Adapter<VinListAdapter.VinViewHolder>() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)
    private val items = mutableListOf<VinRecord>()

    inner class VinViewHolder(private val binding: ItemVinBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) onItemClick(items[pos], pos)
            }
            binding.root.setOnLongClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) onItemLongClick(items[pos], pos)
                true
            }
        }

        fun bind(record: VinRecord, index: Int) {
            binding.tvIndex.text = String.format(Locale.US, "%02d", index + 1)
            binding.tvVin.text = record.vin
            binding.tvTime.text = dateFormat.format(Date(record.timestamp))
            binding.tvSource.text = record.source
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VinViewHolder {
        val binding = ItemVinBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VinViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VinViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newList: List<VinRecord>) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = items.size
            override fun getNewListSize() = newList.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                items[oldItemPosition].vin == newList[newItemPosition].vin
            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                items[oldItemPosition] == newList[newItemPosition]
        })
        items.clear()
        items.addAll(newList)
        diff.dispatchUpdatesTo(this)
    }

    fun getItemAt(position: Int): VinRecord = items[position]
}
