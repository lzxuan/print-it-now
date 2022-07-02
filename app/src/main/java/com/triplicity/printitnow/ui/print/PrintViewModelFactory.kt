package com.triplicity.printitnow.ui.print

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class PrintViewModelFactory(private val uid: String, private val shopId: String)
    : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PrintViewModel::class.java)) {
            return PrintViewModel(uid, shopId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}