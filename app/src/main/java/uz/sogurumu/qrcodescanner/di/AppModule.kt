package uz.sogurumu.qrcodescanner.di

import androidx.room.Room
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import uz.sogurumu.qrcodescanner.data.AppDatabase
import uz.sogurumu.qrcodescanner.ui.history.HistoryViewModel

val appModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "scan_results_db"
        ).build()
    }

    single { get<AppDatabase>().scanResultDao() }

    viewModel { HistoryViewModel(get()) }
}
