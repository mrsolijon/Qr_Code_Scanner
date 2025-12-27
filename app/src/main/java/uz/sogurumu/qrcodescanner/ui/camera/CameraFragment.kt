package uz.sogurumu.qrcodescanner.ui.camera

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.common.util.concurrent.ListenableFuture
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import org.koin.androidx.viewmodel.ext.android.viewModel
import uz.sogurumu.qrcodescanner.R
import uz.sogurumu.qrcodescanner.data.ScanResult
import uz.sogurumu.qrcodescanner.databinding.FragmentCameraBinding
import uz.sogurumu.qrcodescanner.ui.history.HistoryViewModel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

@androidx.camera.core.ExperimentalGetImage
class CameraFragment : Fragment() {
    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!
    private val historyViewModel: HistoryViewModel by viewModel()
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraExecutor: ExecutorService
    private var isScanning = AtomicBoolean(false)
    private var camera: Camera? = null
    private var animation: TranslateAnimation? = null
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) startCamera()
            else Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT)
                .show()
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        checkPermissions()
        setupClickListeners()

        binding.root.post {
            startScanAnimation()
        }
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun setupClickListeners() {
        binding.navHistory.setOnClickListener {
            findNavController().navigate(R.id.action_cameraFragment_to_historyFragment)
        }

        binding.btnFlash.setOnClickListener {
            val isSelected = binding.btnFlash.isSelected
            camera?.cameraControl?.enableTorch(!isSelected)
            binding.btnFlash.isSelected = !isSelected
        }

        binding.btnScanTrigger.setOnClickListener {
            isScanning.set(true)
        }
    }

    private fun startCamera() {
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider)
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(binding.previewView.surfaceProvider)
        }

        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
            if (isScanning.get()) {
                processImageProxy(imageProxy)
            } else {
                imageProxy.close()
            }
        }

        try {
            cameraProvider.unbindAll()
            camera = cameraProvider.bindToLifecycle(
                viewLifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageAnalysis
            )
        } catch (exc: Exception) {
            FirebaseCrashlytics.getInstance().recordException(exc)
        }
    }

    private fun processImageProxy(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: return
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        val scanner = BarcodeScanning.getClient()

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                if (barcodes.isNotEmpty() && isScanning.getAndSet(false)) {
                    val rawValue = barcodes[0].rawValue ?: ""
                    saveResultAndNavigate(rawValue)
                }
            }
            .addOnCompleteListener { imageProxy.close() }
    }

    private fun saveResultAndNavigate(rawValue: String) {
        historyViewModel.insert(ScanResult(data = rawValue, timestamp = System.currentTimeMillis()))
        val action = CameraFragmentDirections.actionCameraFragmentToScanResultFragment(rawValue)
        findNavController().navigate(action)
    }

    private fun startScanAnimation() {
        binding.ivScanFrame.post {
            val frameHeight = binding.ivScanFrame.height.toFloat()
            val lineHeight = binding.scanLine.height.toFloat()

            if (frameHeight <= 0) return@post

            animation = TranslateAnimation(
                0f, 0f,
                0f, frameHeight - lineHeight
            ).apply {
                duration = 2000
                repeatCount = Animation.INFINITE
                repeatMode = Animation.REVERSE
                interpolator = AccelerateDecelerateInterpolator()
            }

            binding.scanLine.startAnimation(animation)
        }
    }

    override fun onResume() {
        super.onResume()
        isScanning.set(false)
        binding.scanLine.post {
            startScanAnimation()
        }
    }

    override fun onPause() {
        super.onPause()
        binding.scanLine.clearAnimation()
    }
}