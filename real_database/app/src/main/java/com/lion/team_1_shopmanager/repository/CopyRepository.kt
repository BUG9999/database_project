package com.lion.database_project.repository

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object CopyRepository {
    // 외부 저장소의 이미지를 내부 저장소로 복사하는 메서드
    fun copyImageToInternalStorage(uri: Uri, context: Context): Uri? {
        try {
            // 외부 저장소의 파일에 접근
            val contentResolver: ContentResolver = context.contentResolver
            // 주어진 URI에 해당하는 이미지 파일을 입력 스트림으로 열고 URI를 통해 파일을 열 수 없으면 null을 반환합니다.
            val inputStream: InputStream = contentResolver.openInputStream(uri) ?: return null
            val bitmap: Bitmap = BitmapFactory.decodeStream(inputStream)
            // 내부 저장소에 이미지를 저장할 파일을 생성합니다.
            // 파일명은 "copied_image_" 뒤에 현재 시간을 붙여 고유하게 생성합니다.
            val file = File(context.filesDir, "copied_image_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            // 비트맵을 JPEG 형식으로 압축하여 출력 스트림에 저장
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            // 저장된 파일의 URI를 반환합니다. 이를 통해 내부 저장소에 저장된 파일을 참조할 수 있습니다.
            return Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}