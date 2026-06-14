package com.vinscanner.app.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * VIN记录仓库：提供读取/写入/删除VIN记录的持久化能力。
 * 基于 SharedPreferences + Gson JSON 序列化。
 */
class VinRepository(private val sharedPreferences: SharedPreferences) {

    private val gson = Gson()
    private val listType = object : TypeToken<List<VinRecord>>() {}.type

    constructor(context: Context) : this(
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )

    fun getAll(): List<VinRecord> {
        val raw = sharedPreferences.getString(KEY_RECORDS, null) ?: return emptyList()
        return runCatching { gson.fromJson<List<VinRecord>>(raw, listType) }.getOrElse { emptyList() }
    }

    fun add(record: VinRecord): Boolean {
        val current = getAll().toMutableList()
        if (current.any { it.vin == record.vin }) return false
        current.add(0, record)
        save(current)
        return true
    }

    fun removeAt(index: Int): Boolean {
        val current = getAll().toMutableList()
        if (index < 0 || index >= current.size) return false
        current.removeAt(index)
        save(current)
        return true
    }

    fun removeByVin(vin: String): Boolean {
        val current = getAll().toMutableList()
        val removed = current.removeAll { it.vin == vin }
        if (removed) save(current)
        return removed
    }

    fun clear() {
        sharedPreferences.edit().remove(KEY_RECORDS).apply()
    }

    fun count(): Int = getAll().size

    fun contains(vin: String): Boolean = getAll().any { it.vin == vin }

    private fun save(list: List<VinRecord>) {
        sharedPreferences.edit().putString(KEY_RECORDS, gson.toJson(list)).apply()
    }

    companion object {
        private const val PREFS_NAME = "vin_scanner_prefs"
        private const val KEY_RECORDS = "vin_records"
    }
}
