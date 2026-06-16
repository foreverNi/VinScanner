package com.vinscanner.app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vinscanner.app.data.VinRecord
import com.vinscanner.app.data.VinRepository

class VinViewModel(private val repository: VinRepository) : ViewModel() {

    private val _records = MutableLiveData<List<VinRecord>>()
    val records: LiveData<List<VinRecord>> get() = _records

    init {
        refresh()
    }

    fun refresh() {
        _records.value = repository.getAll()
    }

    fun add(record: VinRecord): Boolean {
        val added = repository.add(record)
        if (added) refresh()
        return added
    }

    fun removeAt(index: Int): Boolean {
        val removed = repository.removeAt(index)
        if (removed) refresh()
        return removed
    }

    fun removeByVin(vin: String): Boolean {
        val removed = repository.removeByVin(vin)
        if (removed) refresh()
        return removed
    }

    fun updateVinAt(index: Int, newVin: String): Boolean {
        val updated = repository.updateVinAt(index, newVin)
        if (updated) refresh()
        return updated
    }

    fun clearAll() {
        repository.clear()
        refresh()
    }

    fun count(): Int = records.value?.size ?: repository.count()

    fun contains(vin: String): Boolean = repository.contains(vin)

    fun containsExceptIndex(vin: String, excludedIndex: Int): Boolean =
        repository.containsExceptIndex(vin, excludedIndex)
}
