package com.vinscanner.app.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.vinscanner.app.data.InMemoryPrefs
import com.vinscanner.app.data.VinRecord
import com.vinscanner.app.data.VinRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class VinViewModelTest {

    @get:Rule val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: VinRepository
    private lateinit var viewModel: VinViewModel

    @Before fun setUp() {
        val backingMap = mutableMapOf<String, String>()
        val inMemPrefs = InMemoryPrefs(backingMap)
        repository = VinRepository(inMemPrefs)
        viewModel = VinViewModel(repository)
    }

    @Test fun `add record updates livedata`() {
        val r = VinRecord("LSGPC54U3KD123456", 1000L, "scan")
        assertTrue(viewModel.add(r))
        assertEquals(1, viewModel.count())
    }

    @Test fun `duplicate add returns false`() {
        val r1 = VinRecord("LSGPC54U3KD123456", 1000L, "scan")
        val r2 = VinRecord("LSGPC54U3KD123456", 2000L, "manual")
        assertTrue(viewModel.add(r1))
        assertFalse(viewModel.add(r2))
        assertEquals(1, viewModel.count())
    }

    @Test fun `clearAll removes everything`() {
        viewModel.add(VinRecord("LSGPC54U3KD123456", 1000L, "scan"))
        viewModel.add(VinRecord("LSGPC54U3KD123457", 2000L, "manual"))
        assertEquals(2, viewModel.count())
        viewModel.clearAll()
        assertEquals(0, viewModel.count())
    }

    @Test fun `removeAt deletes correct record`() {
        viewModel.add(VinRecord("LSGPC54U3KD123456", 1000L, "scan"))
        viewModel.add(VinRecord("LSGPC54U3KD123457", 2000L, "manual"))
        assertTrue(viewModel.removeAt(1))
        assertEquals(1, viewModel.count())
    }

    @Test fun `updateVinAt updates livedata`() {
        viewModel.add(VinRecord("LSGPC54U3KD123456", 1000L, "scan"))

        assertTrue(viewModel.updateVinAt(0, "LSGPC54U3KD654321"))

        assertEquals("LSGPC54U3KD654321", viewModel.records.value?.first()?.vin)
    }

    @Test fun `updateVinAt rejects duplicate vin`() {
        viewModel.add(VinRecord("LSGPC54U3KD123456", 1000L, "scan"))
        viewModel.add(VinRecord("LSGPC54U3KD123457", 2000L, "manual"))

        assertFalse(viewModel.updateVinAt(0, "LSGPC54U3KD123456"))

        assertEquals("LSGPC54U3KD123457", viewModel.records.value?.first()?.vin)
    }
}
