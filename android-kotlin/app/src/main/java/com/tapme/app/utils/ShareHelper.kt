package com.tapme.app.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.io.File
import java.io.FileOutputStream
import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.content.ContextCompat
import com.tapme.app.R
import java.util.Hashtable

/**
 * Makes sharing/inviting extremely easy
 * - Deep links that auto-open app
 * - QR code sharing
 * - One-tap share to popular apps
 * - Pre-filled user code
 */
class ShareHelper(private val context: Context) {

    companion object {
        // Deep link format: tapme://invite?code=@ABC123
        private const val DEEP_LINK_BASE = "tapme://invite?code="
        
        // Web fallback (if app not installed)
        private const val WEB_FALLBACK_BASE = "https://tapme.app/invite?code="
        
        // Play Store link (update when published)
        private const val PLAY_STORE_LINK = "https://play.google.com/store/apps/details?id=com.tapme.app"
    }

    /**
     * Generate share message with deep link
     */
    fun generateShareMessage(userCode: String): String {
        val deepLink = "$DEEP_LINK_BASE$userCode"
        val webLink = "$WEB_FALLBACK_BASE$userCode"
        
        return """
            💝 Let's stay connected on Tap Me!
            
            My user code: $userCode
            
            Tap this link to add me:
            $webLink
            
            Or download Tap Me:
            $PLAY_STORE_LINK
            
            It's a simple way to let each other know we're thinking of each other ❤️
        """.trimIndent()
    }

    /**
     * Share via Android share sheet (all apps)
     */
    fun shareViaSystem(userCode: String) {
        val shareMessage = generateShareMessage(userCode)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareMessage)
            putExtra(Intent.EXTRA_SUBJECT, "Let's connect on Tap Me! 💝")
        }
        
        try {
            context.startActivity(Intent.createChooser(shareIntent, "Share your Tap Me code"))
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to share", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Share via WhatsApp (one-tap)
     */
    fun shareViaWhatsApp(userCode: String) {
        val shareMessage = generateShareMessage(userCode)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            setPackage("com.whatsapp")
            putExtra(Intent.EXTRA_TEXT, shareMessage)
        }
        
        try {
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
                shareViaSystem(userCode) // Fallback to system share
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to share via WhatsApp", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Share via SMS (one-tap)
     */
    fun shareViaSMS(userCode: String) {
        val shareMessage = generateShareMessage(userCode)
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:")
            putExtra("sms_body", shareMessage)
        }
        
        try {
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "SMS not available", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to send SMS", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Generate QR code for user code
     */
    fun generateQRCode(userCode: String, size: Int = 512): Bitmap? {
        return try {
            val deepLink = "$DEEP_LINK_BASE$userCode"
            val hints = Hashtable<EncodeHintType, Any>().apply {
                put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H)
                put(EncodeHintType.CHARACTER_SET, "UTF-8")
                put(EncodeHintType.MARGIN, 1)
            }
            
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(deepLink, BarcodeFormat.QR_CODE, size, size, hints)
            
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
            for (x in 0 until size) {
                for (y in 0 until size) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            
            bitmap
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Share QR code image
     */
    fun shareQRCode(userCode: String) {
        val qrBitmap = generateQRCode(userCode) ?: run {
            Toast.makeText(context, "Unable to generate QR code", Toast.LENGTH_SHORT).show()
            return
        }
        
        try {
            // Save QR code to cache
            val cacheDir = context.cacheDir
            val qrFile = File(cacheDir, "qr_code_$userCode.png")
            FileOutputStream(qrFile).use { out ->
                qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            
            // Share via FileProvider
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                qrFile
            )
            
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, "Scan this QR code to add me on Tap Me! My code: $userCode")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            context.startActivity(Intent.createChooser(shareIntent, "Share QR Code"))
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to share QR code", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Copy deep link to clipboard
     */
    fun copyDeepLink(userCode: String) {
        val deepLink = "$DEEP_LINK_BASE$userCode"
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("Tap Me Invite", deepLink)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Invite link copied!", Toast.LENGTH_SHORT).show()
    }

    /**
     * Parse user code from deep link
     */
    fun parseUserCodeFromDeepLink(uri: Uri): String? {
        return uri.getQueryParameter("code")
    }
}
