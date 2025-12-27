package uz.sogurumu.qrcodescanner.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import androidx.core.graphics.set
import androidx.core.graphics.createBitmap

object QrCodeHelper {
    fun generateQRCode(text: String, width: Int = 512, height: Int = 512): Bitmap? {
        val writer = QRCodeWriter()
        return try {
            val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, width, height)
            val w = bitMatrix.width
            val h = bitMatrix.height
            val bitmap = createBitmap(w, h)
            for (x in 0 until w) {
                for (y in 0 until h) {
                    bitmap[x, y] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
                }
            }
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}