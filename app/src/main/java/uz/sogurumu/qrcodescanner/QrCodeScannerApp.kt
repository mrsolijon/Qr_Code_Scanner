package uz.sogurumu.qrcodescanner

import android.app.Application
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import uz.sogurumu.qrcodescanner.di.appModule

class QrCodeScannerApp : Application() {
  override fun onCreate() {
    super.onCreate()

    FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)

    startKoin {
      androidContext(this@QrCodeScannerApp)
      modules(appModule)
    }
  }
}
