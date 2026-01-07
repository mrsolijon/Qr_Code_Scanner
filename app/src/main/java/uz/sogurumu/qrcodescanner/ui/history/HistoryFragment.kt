package uz.sogurumu.qrcodescanner.ui.history

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import org.koin.androidx.viewmodel.ext.android.viewModel
import uz.sogurumu.qrcodescanner.R
import uz.sogurumu.qrcodescanner.databinding.FragmentHistoryBinding

class HistoryFragment : Fragment() {
  private var _binding: FragmentHistoryBinding? = null
  private val binding
    get() = _binding!!

  private val historyViewModel: HistoryViewModel by viewModel()
  private lateinit var historyAdapter: HistoryAdapter

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?,
  ): View {
    _binding = FragmentHistoryBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    setupAdapter()
    setupClickListeners()
    observeData()
  }

  private fun setupAdapter() {
    historyAdapter =
        HistoryAdapter(
            onItemClick = { scanResult ->
              if (!historyAdapter.isSelectionMode) {
                historyAdapter.isSelectionMode = true
                historyAdapter.toggleSelection(scanResult.id)
                updateButtonsState()
              }
            },
            onDelete = { scanResult -> historyViewModel.delete(scanResult) },
        )

    historyAdapter.onItemClickSimple = { scanResult ->
      if (historyAdapter.isSelectionMode) {
        historyAdapter.toggleSelection(scanResult.id)
        updateButtonsState()
      } else {
        val data = scanResult.data
        try {
          var uri = Uri.parse(data)
          if (
              !data.startsWith("http://") &&
                  !data.startsWith("https://") &&
                  (data.contains("www.") || data.contains(".com") || data.contains(".uz"))
          ) {
            uri = Uri.parse("https://$data")
          }
          val intent = Intent(Intent.ACTION_VIEW, uri)
          startActivity(intent)
        } catch (e: Exception) {
          Toast.makeText(requireContext(), "Link hadn't opened or text", Toast.LENGTH_SHORT).show()
        }
      }
    }

    binding.recyclerView.apply {
      layoutManager = LinearLayoutManager(requireContext())
      adapter = historyAdapter
    }
  }

  private fun setupClickListeners() {
    binding.ivClose.setOnClickListener {
      if (historyAdapter.isSelectionMode) {
        historyAdapter.clearSelection()
        updateButtonsState()
      } else {
        findNavController().popBackStack()
      }
    }

    binding.btnSelectAll.setOnClickListener {
      historyAdapter.toggleSelectAll()

      updateButtonsState()
    }

    binding.btnDeleteSelected.setOnClickListener {
      val selectedIds = historyAdapter.getSelectedItems()
      if (selectedIds.isNotEmpty()) {
        historyViewModel.deleteSelected(selectedIds)
        historyAdapter.clearSelection()
        updateButtonsState()
      }
    }
  }

  private fun observeData() {
    historyViewModel.allScanResults.observe(viewLifecycleOwner) { list ->
      historyAdapter.submitList(list)

      if (list.isEmpty() && historyAdapter.isSelectionMode) {
        historyAdapter.clearSelection()
        updateButtonsState()
      }
    }
  }

  private fun updateButtonsState() {
    val isSelectionMode = historyAdapter.isSelectionMode
    val selectedCount = historyAdapter.getSelectedItems().size

    binding.btnDeleteSelected.isVisible = isSelectionMode
    binding.btnSelectAll.isVisible = isSelectionMode

    if (isSelectionMode) {
      binding.tvTitle.text = if (selectedCount == 0) "Select Items" else "$selectedCount Selected"
      binding.ivClose.setImageResource(R.drawable.ic_close)
    } else {
      binding.tvTitle.text = "History"
      binding.ivClose.setImageResource(R.drawable.ic_close)
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}
