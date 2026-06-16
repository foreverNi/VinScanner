package com.vinscanner.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class VinRepositoryTest {

    private lateinit var repository: VinRepository
    private lateinit var backingMap: MutableMap<String, String>

    @Before fun setUp() {
        backingMap = mutableMapOf()
        val inMemPrefs = InMemoryPrefs(backingMap)
        repository = VinRepository(inMemPrefs)
    }

    @Test fun `empty repository returns empty list`() {
        assertEquals(0, repository.count())
        assertTrue(repository.getAll().isEmpty())
    }

    @Test fun `add and retrieve records`() {
        val record = VinRecord("LSGPC54U3KD123456", 1000L, "scan")
        assertTrue(repository.add(record))
        assertEquals(1, repository.count())
        assertEquals("LSGPC54U3KD123456", repository.getAll()[0].vin)
    }

    @Test fun `duplicate vin returns false and is not saved`() {
        val r1 = VinRecord("LSGPC54U3KD123456", 1000L, "scan")
        val r2 = VinRecord("LSGPC54U3KD123456", 2000L, "manual")
        assertTrue(repository.add(r1))
        assertFalse(repository.add(r2))
        assertEquals(1, repository.count())
    }

    @Test fun `contains returns true for existing vin`() {
        repository.add(VinRecord("LSGPC54U3KD123456", 1000L, "scan"))
        assertTrue(repository.contains("LSGPC54U3KD123456"))
        assertFalse(repository.contains("XXXPC54U3KD123000"))
    }

    @Test fun `removeAt deletes record`() {
        repository.add(VinRecord("LSGPC54U3KD123456", 1000L, "scan"))
        repository.add(VinRecord("LSGPC54U3KD123457", 2000L, "manual"))
        assertEquals(2, repository.count())
        assertTrue(repository.removeAt(0))
        assertEquals(1, repository.count())
    }

    @Test fun `clear removes everything`() {
        repository.add(VinRecord("LSGPC54U3KD123456", 1000L, "scan"))
        repository.clear()
        assertEquals(0, repository.count())
    }

    @Test fun `removeByVin removes specific record`() {
        repository.add(VinRecord("LSGPC54U3KD123456", 1000L, "scan"))
        repository.add(VinRecord("LSGPC54U3KD123457", 2000L, "manual"))
        assertTrue(repository.removeByVin("LSGPC54U3KD123456"))
        assertEquals(1, repository.count())
        assertFalse(repository.contains("LSGPC54U3KD123456"))
    }

    @Test fun `updateVinAt changes vin and keeps metadata`() {
        repository.add(VinRecord("LSGPC54U3KD123456", 1000L, "scan"))

        assertTrue(repository.updateVinAt(0, "LSGPC54U3KD654321"))

        val record = repository.getAll()[0]
        assertEquals("LSGPC54U3KD654321", record.vin)
        assertEquals(1000L, record.timestamp)
        assertEquals("scan", record.source)
    }

    @Test fun `updateVinAt rejects duplicate vin`() {
        repository.add(VinRecord("LSGPC54U3KD123456", 1000L, "scan"))
        repository.add(VinRecord("LSGPC54U3KD123457", 2000L, "manual"))

        assertFalse(repository.updateVinAt(0, "LSGPC54U3KD123456"))
        assertEquals("LSGPC54U3KD123457", repository.getAll()[0].vin)
    }

    @Test fun `updateVinAt rejects invalid index`() {
        repository.add(VinRecord("LSGPC54U3KD123456", 1000L, "scan"))

        assertFalse(repository.updateVinAt(1, "LSGPC54U3KD654321"))
        assertEquals("LSGPC54U3KD123456", repository.getAll()[0].vin)
    }
}

internal class InMemoryPrefs(private val map: MutableMap<String, String>) : android.content.SharedPreferences {

    override fun getAll(): MutableMap<String, *> = HashMap(map)
    override fun getString(key: String, defValue: String?): String? = map[key] ?: defValue
    override fun getStringSet(key: String, defValues: MutableSet<String>?): MutableSet<String>? = null
    override fun getInt(key: String, defValue: Int): Int = map[key]?.toIntOrNull() ?: defValue
    override fun getLong(key: String, defValue: Long): Long = map[key]?.toLongOrNull() ?: defValue
    override fun getFloat(key: String, defValue: Float): Float = map[key]?.toFloatOrNull() ?: defValue
    override fun getBoolean(key: String, defValue: Boolean): Boolean = map[key]?.toBoolean() ?: defValue
    override fun contains(key: String): Boolean = map.containsKey(key)
    override fun edit(): android.content.SharedPreferences.Editor = EditorImpl()
    override fun registerOnSharedPreferenceChangeListener(listener: android.content.SharedPreferences.OnSharedPreferenceChangeListener?) {}
    override fun unregisterOnSharedPreferenceChangeListener(listener: android.content.SharedPreferences.OnSharedPreferenceChangeListener?) {}

    inner class EditorImpl : android.content.SharedPreferences.Editor {
        private val edits = mutableMapOf<String, String?>()
        override fun putString(key: String, value: String?): android.content.SharedPreferences.Editor {
            edits[key] = value; return this
        }
        override fun putStringSet(key: String, values: MutableSet<String>?): android.content.SharedPreferences.Editor = this
        override fun putInt(key: String, value: Int): android.content.SharedPreferences.Editor {
            edits[key] = value.toString(); return this
        }
        override fun putLong(key: String, value: Long): android.content.SharedPreferences.Editor {
            edits[key] = value.toString(); return this
        }
        override fun putFloat(key: String, value: Float): android.content.SharedPreferences.Editor {
            edits[key] = value.toString(); return this
        }
        override fun putBoolean(key: String, value: Boolean): android.content.SharedPreferences.Editor {
            edits[key] = value.toString(); return this
        }
        override fun remove(key: String): android.content.SharedPreferences.Editor {
            edits[key] = null; return this
        }
        override fun clear(): android.content.SharedPreferences.Editor {
            map.clear(); return this
        }
        override fun commit(): Boolean {
            edits.forEach { (k, v) -> if (v == null) map.remove(k) else map[k] = v }
            return true
        }
        override fun apply() { commit() }
    }
}
