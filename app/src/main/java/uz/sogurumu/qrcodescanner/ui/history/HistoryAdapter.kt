package uz.sogurumu.qrcodescanner.ui.history

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uz.sogurumu.qrcodescanner.R
import uz.sogurumu.qrcodescanner.data.ScanResult
import uz.sogurumu.qrcodescanner.databinding.ItemScanResultBinding
import uz.sogurumu.qrcodescanner.utils.QrCodeHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter(
    private val onItemClick: (ScanResult) -> Unit,
    private val onDelete: (ScanResult) -> Unit
) :
    ListAdapter<ScanResult, HistoryAdapter.ViewHolder>(ScanResultDiffCallback()) {

    var onItemClickSimple: ((ScanResult) -> Unit)? = null
    private val selectedItems = mutableSetOf<Int>()
    var isSelectionMode = false

    fun toggleSelection(id: Int) {
        if (selectedItems.contains(id)) {
            selectedItems.remove(id)
        } else {
            selectedItems.add(id)
        }
        if (selectedItems.isEmpty()) {
            isSelectionMode = false
        }
        notifyDataSetChanged()
    }

    fun toggleSelectAll() {
        if (selectedItems.size == currentList.size) {
            clearSelection()
        } else {
            selectedItems.clear()
            currentList.forEach { scanResult ->
                selectedItems.add(scanResult.id)
            }
            isSelectionMode = true
            notifyDataSetChanged()
        }
    }

    fun getSelectedItems(): List<Int> {
        return selectedItems.toList()
    }

    fun clearSelection() {
        selectedItems.clear()
        isSelectionMode = false
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemScanResultBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val scanResult = getItem(position)
        val isSelected = selectedItems.contains(scanResult.id)

        holder.bind(scanResult)
        val context = holder.itemView.context

        holder.binding.cardBackground.setBackgroundColor(
            if (isSelected) ContextCompat.getColor(context, R.color.item_selected_bg)
            else Color.TRANSPARENT
        )

        if (isSelected) {
            holder.binding.iconQr.visibility = View.INVISIBLE
            holder.binding.ivCheck.visibility = View.VISIBLE
            holder.cancelQrJob()
        } else {
            holder.binding.iconQr.visibility = View.VISIBLE
            holder.binding.ivCheck.visibility = View.GONE
            holder.loadQrCode(scanResult.data)
        }

        if (isSelectionMode) {
            holder.binding.deleteButton.visibility = View.GONE
            holder.binding.deleteButton.isEnabled = false
        } else {
            holder.binding.deleteButton.visibility = View.VISIBLE
            holder.binding.deleteButton.isEnabled = true
        }

        holder.itemView.setOnLongClickListener {
            onItemClick(scanResult)
            true
        }

        holder.itemView.setOnClickListener {
            onItemClickSimple?.invoke(scanResult)
        }

        holder.binding.deleteButton.setOnClickListener {
            onDelete(scanResult)
        }
    }

    inner class ViewHolder(val binding: ItemScanResultBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private var qrJob: Job? = null
        fun bind(scanResult: ScanResult) {
            binding.dataTextView.text = scanResult.data
            binding.timestampTextView.text =
                SimpleDateFormat(
                    "yyyy-MM-dd HH:mm",
                    Locale.getDefault()
                ).format(Date(scanResult.timestamp))
        }

        fun loadQrCode(data: String) {
            qrJob?.cancel()
            binding.iconQr.setImageResource(R.drawable.ic_qr_code)
            qrJob = CoroutineScope(Dispatchers.Default).launch {
                val bitmap = QrCodeHelper.generateQRCode(data, 200, 200)
                withContext(Dispatchers.Main) {
                    if (bitmap != null) {
                        binding.iconQr.setImageBitmap(bitmap)
                    }
                }
            }
        }

        fun cancelQrJob() {
            qrJob?.cancel()
        }
    }
}

class ScanResultDiffCallback : DiffUtil.ItemCallback<ScanResult>() {
    override fun areItemsTheSame(oldItem: ScanResult, newItem: ScanResult): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ScanResult, newItem: ScanResult): Boolean {
        return oldItem == newItem
    }
}