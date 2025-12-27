package uz.sogurumu.qrcodescanner.ui.scan

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import android.widget.Toast
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import uz.sogurumu.qrcodescanner.databinding.FragmentScanResultBinding

class ScanResultFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentScanResultBinding? = null
    private val binding get() = _binding!!
    private val args: ScanResultFragmentArgs by navArgs()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScanResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val scanResult = args.scanResult
        binding.scanResultTextView.text = scanResult
        if (URLUtil.isValidUrl(scanResult)) {
            binding.openButton.visibility = View.VISIBLE
            binding.openButton.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(scanResult))
                startActivity(intent)
            }
        }
        binding.copyButton.setOnClickListener {
            val clipboard =
                requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Scan Result", scanResult)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(requireContext(), "Copied to clipboard", Toast.LENGTH_SHORT).show()
        }

        binding.closeButton.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
