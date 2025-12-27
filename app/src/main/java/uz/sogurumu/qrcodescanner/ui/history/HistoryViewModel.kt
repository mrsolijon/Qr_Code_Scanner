package uz.sogurumu.qrcodescanner.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.launch
import uz.sogurumu.qrcodescanner.data.ScanResult
import uz.sogurumu.qrcodescanner.data.ScanResultDao

class HistoryViewModel(private val scanResultDao: ScanResultDao) : ViewModel() {

    val allScanResults = scanResultDao.getAll()

    fun insert(scanResult: ScanResult) = viewModelScope.launch {
        try {
            scanResultDao.insert(scanResult)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    fun delete(scanResult: ScanResult) = viewModelScope.launch {
        try {
            scanResultDao.delete(scanResult.id)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }
    fun deleteSelected(selectedIds: List<Int>) = viewModelScope.launch {
        try {
            scanResultDao.deleteByIds(selectedIds)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }
}
