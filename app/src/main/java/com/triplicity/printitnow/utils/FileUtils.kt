package com.triplicity.printitnow.utils

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import com.triplicity.printitnow.entity.FileInfo.FILE.FILE_SIZE_LIMIT
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception

object FileUtils {

    private fun isLocal(url: String?): Boolean {
        return url != null && !url.startsWith("http://") && !url.startsWith("https://")
    }

    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    private fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }

    private fun isGoogleDriveUri(uri: Uri): Boolean {
        return "com.google.android.apps.docs.storage" == uri.authority
    }

    private fun getDataColumn(
        context: Context, uri: Uri, selection: String?, selectionArgs: Array<String>?
    ): String? {
        val dataColumn = "_data"
        val projection = arrayOf(dataColumn)

        context.contentResolver.query(uri, projection, selection, selectionArgs, null).use {
            if (it != null && it.moveToFirst()) {
                return it.getString(it.getColumnIndexOrThrow(dataColumn))
            }
        }

        return null
    }

    fun getPath(context: Context, uri: Uri): String? {
        val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            val docId = DocumentsContract.getDocumentId(uri)

            if (isExternalStorageDocument(uri)) {
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                if ("primary".equals(type, true)) {
                    return context.getExternalFilesDir(null).toString() + "/" + split[1]
                }
                // TODO handle non-primary volumes

            } else if (isDownloadsDocument(uri)) {
                val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), docId.toLong()
                )
                try {
                    val path = getDataColumn(context, contentUri, null, null)
                    if (path != null) {
                        return path
                    }
                } catch (e: Exception) {}
                return copyFileAndGetPath(context, uri)

            } else if (isMediaDocument(uri)) {
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                when (type) {
                    "image" -> { contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI }
                    "video" -> { contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI }
                    "audio" -> { contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI }
                }
                return contentUri?.let { getDataColumn(context, it, "_id=?", arrayOf(split[1])) }

            } else if (isGoogleDriveUri(uri)) {
                return copyFileAndGetPath(context, uri)
            }

        } else if ("content".equals(uri.scheme, true)) {
            return if (isGooglePhotosUri(uri)) uri.lastPathSegment
                else getDataColumn(context, uri, null, null)

        } else if ("file".equals(uri.scheme, true)) {
            return uri.path
        }

        return null
    }

    fun getFile(context: Context, uri: Uri): File? {
        val path = getPath(context, uri)
        return if (path != null && isLocal(path)) File(path) else null
    }

    fun getFile(path: String): File? {
        return if (isLocal(path)) File(path) else null
    }

    fun getFileNameAndSize(context: Context, uri: Uri): Pair<String?, Long>? {
        val projection = arrayOf(
            OpenableColumns.DISPLAY_NAME,
            OpenableColumns.SIZE
        )

        context.contentResolver.query(uri, projection, null, null, null).use {
            if (it != null && it.moveToFirst()) {
                val name = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                val size = it.getLong(it.getColumnIndex(OpenableColumns.SIZE))

                return Pair(name, size)
            }
        }

        return null
    }

    private fun copyFileAndGetPath(context: Context, uri: Uri): String? {
        val fileNameAndSize = getFileNameAndSize(context, uri)

        fileNameAndSize?.let {
            if (it.second < FILE_SIZE_LIMIT && it.first!= null) {
                val inputStream = context.contentResolver.openInputStream(uri)

                if (inputStream != null) {
                    val file = File(context.cacheDir, it.first!!)
                    val bis = BufferedInputStream(inputStream)
                    val bos = BufferedOutputStream(FileOutputStream(file, false))

                    val maxBufferSize = 1 * 1024 * 1024
                    val bytesAvailable = inputStream.available()
                    val bufferSize = bytesAvailable.coerceAtMost(maxBufferSize)
                    val buffer = ByteArray(bufferSize)

                    while (bis.read(buffer) != -1) {
                        bos.write(buffer)
                    }

                    bos.flush()
                    bos.close()
                    bis.close()

                    return file.path
                }
            }
        }

        return null
    }
}
