package com.princelumpy.breakvault.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.princelumpy.breakvault.data.dao.BattleDao
import com.princelumpy.breakvault.data.dao.MoveDao
import com.princelumpy.breakvault.data.dao.SavedComboDao
import com.princelumpy.breakvault.data.model.battlecombo.BattleCombo
import com.princelumpy.breakvault.data.model.battlecombo.BattleComboTagCrossRef
import com.princelumpy.breakvault.data.model.battlecombo.BattleTag
import com.princelumpy.breakvault.data.model.battlecombo.EnergyLevel
import com.princelumpy.breakvault.data.model.battlecombo.TrainingStatus
import com.princelumpy.breakvault.data.model.move.Move
import com.princelumpy.breakvault.data.model.move.MoveTag
import com.princelumpy.breakvault.data.model.move.MoveTagCrossRef
import com.princelumpy.breakvault.data.model.savedcombo.SavedCombo
import com.princelumpy.breakvault.data.transfer.AppDataExport
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ImportExportIntegrationTest {

    private lateinit var db: AppDB

    // DAOs
    private lateinit var moveDao: MoveDao
    private lateinit var savedComboDao: SavedComboDao
    private lateinit var battleDao: BattleDao
    private lateinit var battleTagDao: BattleTagDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        // Use an in-memory database for testing.
        // The data will be wiped when the process dies.
        db = Room.inMemoryDatabaseBuilder(context, AppDB::class.java).build()

        moveDao = db.moveDao()
        savedComboDao = db.savedComboDao()
        battleDao = db.battleDao()
        battleTagDao = db.battleTagDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testFullImportCycle() = runBlocking {
        // 1. Prepare Complex Import Data
        val move1 = Move("m1", "Windmill")
        val moveTag1 = MoveTag("t1", "Power")
        val crossRef1 = MoveTagCrossRef("m1", "t1")

        val savedCombo = SavedCombo(
            id = "s1",
            name = "Power Set",
            moves = listOf("Windmill", "Windmill")
        )

        val battleCombo = BattleCombo(
            id = "b1",
            description = "Battle Round 1",
            energy = EnergyLevel.HIGH,
            status = TrainingStatus.READY
        )
        val battleTag = BattleTag("bt1", "Aggressive")
        val battleRef = BattleComboTagCrossRef("b1", "bt1")

        val exportData = AppDataExport(
            moves = listOf(move1),
            moveTags = listOf(moveTag1),
            moveTagCrossRefs = listOf(crossRef1),
            savedCombos = listOf(savedCombo),
            battleCombos = listOf(battleCombo),
            battleTags = listOf(battleTag),
            battleComboTagCrossRefs = listOf(battleRef)
        )

        // 2. Execute Import Logic (Simulating ViewModel logic manually)
        db.clearAllTables() // Ensure clean slate

        moveDao.insertAllMoves(exportData.moves)
        moveDao.insertAllTags(exportData.moveTags)
        moveDao.insertAllMoveTagCrossRefs(exportData.moveTagCrossRefs)
        savedComboDao.insertAllSavedCombos(exportData.savedCombos)

        exportData.battleCombos.forEach { battleDao.insertBattleCombo(it) }
        exportData.battleTags.forEach { battleTagDao.insertBattleTag(it) }
        exportData.battleComboTagCrossRefs.forEach { battleDao.link(it) }

        // 3. Verify Practice Mode Data
        val loadedMove = moveDao.getMoveWithTags("m1")
        assertNotNull("Move should exist", loadedMove)
        assertEquals("Windmill", loadedMove?.move?.name)
        assertEquals("Power", loadedMove?.moveTags?.first()?.name)

        val loadedSavedCombo = savedComboDao.getSavedComboById("s1")
        assertNotNull("Saved Combo should exist", loadedSavedCombo)
        assertEquals(2, loadedSavedCombo?.moves?.size)

        // 4. Verify Battle Mode Data
        val loadedBattleCombo = battleDao.getBattleComboWithTags("b1")
        assertNotNull("Battle Combo should exist", loadedBattleCombo)
        assertEquals(EnergyLevel.HIGH, loadedBattleCombo?.battleCombo?.energy)
        assertEquals("Aggressive", loadedBattleCombo?.tags?.first()?.name)
    }
}
