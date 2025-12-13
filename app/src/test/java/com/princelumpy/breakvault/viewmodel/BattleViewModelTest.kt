package com.princelumpy.breakvault.viewmodel

import android.app.Application
import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.princelumpy.breakvault.data.AppDB
import com.princelumpy.breakvault.data.dao.BattleDao
import com.princelumpy.breakvault.data.model.battlecombo.BattleCombo
import com.princelumpy.breakvault.data.model.battlecombo.BattleComboWithTags
import com.princelumpy.breakvault.data.model.battlecombo.BattleTag
import com.princelumpy.breakvault.data.model.battlecombo.EnergyLevel
import com.princelumpy.breakvault.data.model.battlecombo.TrainingStatus
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BattleViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private val app = mockk<Application>(relaxed = true)
    private val db = mockk<AppDB>()
    private val battleDao = mockk<BattleDao>(relaxed = true)
    private lateinit var viewModel: BattleComboListViewModel

    private val battleCombosLiveData = MutableLiveData<List<BattleComboWithTags>>()
    private val battleTagsLiveData = MutableLiveData<List<BattleTag>>()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Mock Log
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0

        // Mock DB
        mockkObject(AppDB)
        every { AppDB.getDatabase(any()) } returns db
        every { db.battleDao() } returns battleDao

        // Mock LiveData
        every { battleDao.getAllBattleCombosWithTags() } returns battleCombosLiveData
        every { battleDao.getAllBattleTags() } returns battleTagsLiveData

        viewModel = BattleComboListViewModel(app)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `toggleUsed flips isUsed flag and updates DAO`() = runTest {
        // Given
        val combo = BattleCombo(
            id = "1",
            description = "Test Combo",
            isUsed = false
        )

        // When
        viewModel.toggleUsed(combo)
        advanceUntilIdle()

        // Then
        coVerify {
            battleDao.updateBattleCombo(match { updatedCombo ->
                updatedCombo.id == "1" && updatedCombo.isUsed == true
            })
        }
    }

    @Test
    fun `resetBattle calls reset on DAO`() = runTest {
        // When
        viewModel.resetBattle()
        advanceUntilIdle()

        // Then
        coVerify { battleDao.resetAllBattleCombosUsage() }
    }

    @Test
    fun `addBattleCombo inserts combo and links existing tags`() = runTest {
        // Given
        val description = "Power Combo"
        val energy = EnergyLevel.HIGH
        val status = TrainingStatus.READY
        val tagName = "Power"
        val existingTag = BattleTag(id = "moveTag-1", name = tagName)

        // Mock: MoveTag exists
        coEvery { battleDao.getBattleTagByName(tagName) } returns existingTag

        // When
        viewModel.addBattleCombo(description, energy, status, listOf(tagName))
        advanceUntilIdle()

        // Then
        // 1. Verify Combo Insert
        coVerify {
            battleDao.insertBattleCombo(match {
                it.description == description && it.energy == energy
            })
        }
        // 2. Verify CrossRef Insert (linking combo to existing moveTag)
        coVerify {
            battleDao.link(match {
                it.battleTagId == "moveTag-1"
            })
        }
    }

    @Test
    fun `addBattleCombo creates new tag if not found`() = runTest {
        // Given
        val tagName = "New Style"
        // Mock: MoveTag does NOT exist
        coEvery { battleDao.getBattleTagByName(tagName) } returns null

        // When
        viewModel.addBattleCombo("Desc", EnergyLevel.LOW, TrainingStatus.TRAINING, listOf(tagName))
        advanceUntilIdle()

        // Then
        // 1. Verify MoveTag Creation
        coVerify {
            battleDao.insertBattleTag(match { it.name == tagName })
        }
        // 2. Verify CrossRef Insert
        coVerify {
            battleDao.link(any())
        }
    }
}
