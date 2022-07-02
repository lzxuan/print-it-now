package com.triplicity.printitnow.ui.print

import android.content.ClipData
import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.triplicity.printitnow.entity.Card
import com.triplicity.printitnow.entity.FileInfo
import com.triplicity.printitnow.entity.FileInfo.FILE.FILE_SIZE_LIMIT
import com.triplicity.printitnow.entity.PrintConfig
import com.triplicity.printitnow.utils.FileUtils.getFileNameAndSize
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class PrintViewModel(private val uid: String, private val shopId: String) : ViewModel() {

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private val storage = Firebase.storage.reference
    private val functions = Firebase.functions

    val files = ArrayList<FileInfo>()
    var pages = 0
    val printConfig = PrintConfig()
    private var dateTime = ""

    private val _fileCount = MutableLiveData(0)
    val fileCount: LiveData<Int> = _fileCount

    private val _loaded = MutableLiveData(true)
    val loaded: LiveData<Boolean> = _loaded

    var priced = false

    private val _paid = MutableLiveData(false)
    val paid: LiveData<Boolean> = _paid

    var price = 0.0
    val card = Card()

    fun addFile(context: Context, uri: Uri) {
        uiScope.launch {
            _loaded.value = false
            loadFile(context, uri)
            _loaded.value = true
        }
    }

    fun addFiles(context: Context, clipData: ClipData) {
        uiScope.launch {
            _loaded.value = false
            loadFiles(context, clipData)
            _loaded.value = true
        }
    }

    private suspend fun loadFile(context: Context, uri: Uri) {
        withContext(Dispatchers.IO) {
            if (uri.scheme.equals("content")) {
                getFileNameAndSize(context, uri)?.let {
                    val name = it.first
                    val size = it.second
                    val type = context.contentResolver.getType(uri).toString()
                    val page = countPages(context, uri, type)

                    if (name != null && size <= FILE_SIZE_LIMIT && page > 0) {
                        files.add(FileInfo(uri, name))
                        pages += page
                        _fileCount.postValue(files.size)
                    }
                }
            }
        }
    }

    private suspend fun loadFiles(context: Context, clipData: ClipData) {
        withContext(Dispatchers.IO) {
            for (i in 0 until clipData.itemCount) {
                loadFile(context, clipData.getItemAt(i).uri)
            }
        }
    }

    private fun countPages(context: Context, uri: Uri, type: String): Int {
        var numberOfPages = 0

        if (type == "application/pdf") {
            val input = context.contentResolver.openInputStream(uri)

            if (input != null) {
                val pdf = PDDocument.load(input)
                numberOfPages = pdf.numberOfPages
                pdf.close()
            }
        } else {
            numberOfPages++
        }

        return numberOfPages
    }

    fun requestPrice() {
        uiScope.launch {
            priced = false
            _loaded.value = false
            getPrice()
            _loaded.value = true
            priced = true
        }
    }

    private suspend fun getPrice() {
        val config = hashMapOf(
            "paperSize" to printConfig.paperSize,
            "color" to printConfig.color,
            "twoSided" to printConfig.twoSided,
            "copies" to printConfig.copies
        )
        val data = hashMapOf(
            "pages" to pages,
            "printConfig" to config,
            "shop" to shopId
        )
        withContext(Dispatchers.IO) {
            price = com.google.android.gms.tasks.Tasks.await(
                functions
                    .getHttpsCallable("pricePrint")
                    .call(data)
            ).data.toString().toDouble()
        }
    }

    fun pay(context: Context) {
        uiScope.launch {
            _loaded.value = false
            uploadFiles(context)
            createPrint()
            _loaded.value = true
            _paid.value = true
        }
    }

    private suspend fun uploadFiles(context: Context) {
        val sdf = SimpleDateFormat("yyyyMMddHHmmss", Locale("ms","MY","MY"))
        dateTime = sdf.format(Date())
        val printsRef = storage.child("prints/$shopId/$uid/$dateTime")

        withContext(Dispatchers.IO) {
            files.forEach {
                com.google.android.gms.tasks.Tasks.await(
                    printsRef.child(it.name).putStream(context.contentResolver.openInputStream(it.uri!!)!!)
                )
            }
        }
    }

    private suspend fun createPrint() {
        val config = hashMapOf(
            "paperSize" to printConfig.paperSize,
            "color" to printConfig.color,
            "twoSided" to printConfig.twoSided,
            "copies" to printConfig.copies
        )
        val filesList = ArrayList<String>(files.size)
        files.forEach {
            filesList.add(it.name)
        }
        val data = hashMapOf(
            "folder" to dateTime,
            "files" to filesList,
            "printConfig" to config,
            "pages" to pages,
            "user" to uid,
            "shop" to shopId,
            "payMethod" to "Card"
        )
        withContext(Dispatchers.IO) {
            com.google.android.gms.tasks.Tasks.await(
                functions
                    .getHttpsCallable("createPrint")
                    .call(data)
            )
        }
        clearFiles()
        clearPayment()
    }

    fun clearFiles() {
        files.clear()
        pages = 0
        price = 0.0
        _fileCount.postValue(0)
    }

    private fun clearPayment() {
        card.name = ""
        card.cardNumber = ""
        card.expiration = ""
        card.cvv = ""
        _paid.postValue(false)
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}