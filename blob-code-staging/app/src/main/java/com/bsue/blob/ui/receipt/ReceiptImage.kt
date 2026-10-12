package com.bsue.blob.ui.receipt

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

// 영수증 그림을 갤러리에 저장하거나 공유하기
object ReceiptImage {

    private const val ALBUM = "블롭 출동"

    // 갤러리(사진 > 블롭 출동 앨범)에 PNG로 저장. 성공하면 true
    fun saveToGallery(context: Context, bitmap: Bitmap, fileName: String): Boolean = try {
        if (Build.VERSION.SDK_INT >= 29) {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "$fileName.png")
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/$ALBUM")
            }
            val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: error("저장 위치를 못 만들었어요")
            context.contentResolver.openOutputStream(uri)?.use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            true
        } else {
            // 안드로이드 9 이하 (전시폰은 해당 없음)
            @Suppress("DEPRECATION")
            val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), ALBUM).apply { mkdirs() }
            FileOutputStream(File(dir, "$fileName.png")).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            true
        }
    } catch (e: Exception) {
        false
    }

    // 공유 창 열기 (카톡·인스타 등)
    fun share(context: Context, bitmap: Bitmap, fileName: String) {
        val dir = File(context.cacheDir, "receipts").apply { mkdirs() }
        val file = File(dir, "$fileName.png")
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "탐정 블롭이랑 참은 것들 🧾 #블롭출동")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(send, "영수증 공유하기"))
    }
}
