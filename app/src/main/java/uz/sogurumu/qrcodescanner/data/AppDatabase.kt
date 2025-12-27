package uz.sogurumu.qrcodescanner.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ScanResult::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scanResultDao(): ScanResultDao
}
