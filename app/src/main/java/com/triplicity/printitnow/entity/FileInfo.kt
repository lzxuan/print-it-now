package com.triplicity.printitnow.entity

import android.net.Uri

class FileInfo(var uri: Uri? = null, var name: String = "") {
    object FILE {
        const val FILE_SIZE_LIMIT = 10 * 1024 * 1024
        val FILE_TYPES_ALLOWED = arrayOf("application/pdf", "image/*")
    }
}
