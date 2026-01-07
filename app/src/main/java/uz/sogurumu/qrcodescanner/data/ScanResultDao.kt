package uz.sogurumu.qrcodescanner.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ScanResultDao {
  @Insert suspend fun insert(scanResult: ScanResult)

  @Query("SELECT * FROM scan_results ORDER BY timestamp DESC")
  fun getAll(): LiveData<List<ScanResult>>

  @Query("DELETE FROM scan_results WHERE id = :id") suspend fun delete(id: Int)

  @Query("DELETE FROM scan_results") suspend fun deleteAll()

  @Query("DELETE FROM scan_results WHERE id IN (:ids)") suspend fun deleteByIds(ids: List<Int>)
}
