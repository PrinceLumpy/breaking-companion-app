package com.princelumpy.breakvault.data

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.princelumpy.breakvault.data.dao.BattleDao
import com.princelumpy.breakvault.data.model.battlecombo.BattleCombo
import com.princelumpy.breakvault.data.model.battlecombo.BattleComboTagCrossRef
import com.princelumpy.breakvault.data.model.battlecombo.BattleTag
import com.princelumpy.breakvault.data.model.battlecombo.EnergyLevel
import com.princelumpy.breakvault.data.model.battlecombo.TrainingStatus
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class BattleDaoTest {

    private lateinit var db: AppDB
    private lateinit var battleDao: BattleDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, AppDB::class.java
        ).build()
        battleDao = db.battleDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndGetBattleCombo() = runBlocking {
        val combo = BattleCombo(
            id = UUID.randomUUID().toString(),
            description = "Test Combo",
            energy = EnergyLevel.MEDIUM,
            status = TrainingStatus.TRAINING,
            isUsed = false
        )

        battleDao.insertBattleCombo(combo)

        val loadedCombo = battleDao.getBattleComboWithTags(combo.id)
        assertNotNull(loadedCombo)
        assertEquals("Test Combo", loadedCombo?.battleCombo?.description)
        assertEquals(EnergyLevel.MEDIUM, loadedCombo?.battleCombo?.energy)
    }

    @Test
    fun insertBattleComboWithTags() = runBlocking {
        // 1. Insert Combo
        val comboId = "combo-1"
        val combo = BattleCombo(id = comboId, description = "Tagged Combo")
        battleDao.insertBattleCombo(combo)

        // 2. Insert Tags
        val tag1 = BattleTag(id = "moveTag-1", name = "Power")
        val tag2 = BattleTag(id = "moveTag-2", name = "Style")
        battleDao.insertBattleTag(tag1)
        battleDao.insertBattleTag(tag2)

        // 3. Insert CrossRefs
        val crossRef1 = BattleComboTagCrossRef(comboId, "moveTag-1")
        val crossRef2 = BattleComboTagCrossRef(comboId, "moveTag-2")
        battleDao.link(crossRef1)
        battleDao.link(crossRef2)

        // 4. Query
        val loaded = battleDao.getBattleComboWithTags(comboId)

        // 5. Verify
        assertNotNull(loaded)
        assertEquals(2, loaded?.tags?.size)
        val tagNames = loaded?.tags?.map { it.name }
        assert(tagNames!!.contains("Power"))
        assert(tagNames.contains("Style"))
    }

    // --- Referential Integrity Tests ---

    @Test
    fun deleteBattleComboCascadesToCrossRef() = runBlocking {
        // Given: Combo linked to MoveTag
        val comboId = "c1"
        val tagId = "t1"
        battleDao.insertBattleCombo(BattleCombo(id = comboId, description = "Desc"))
        battleDao.insertBattleTag(BattleTag(id = tagId, name = "MoveTag"))
        battleDao.link(BattleComboTagCrossRef(comboId, tagId))

        // When: Delete Combo
        battleDao.deleteBattleCombo(BattleCombo(id = comboId, description = ""))

        // Then: CrossRef should be gone
        val refs = battleDao.getAllBattleComboTagCrossRefs()
        assertTrue(refs.isEmpty())
    }

    @Test
    fun deleteBattleTagRemovesTagFromCombo() = runBlocking {
        // Given: Combo linked to MoveTag
        val comboId = "c1"
        val tagId = "t1"
        battleDao.insertBattleCombo(BattleCombo(id = comboId, description = "Desc"))
        battleDao.insertBattleTag(BattleTag(id = tagId, name = "MoveTag"))
        battleDao.link(BattleComboTagCrossRef(comboId, tagId))

        // When: Delete MoveTag
        battleDao.deleteBattleTag(BattleTag(id = tagId, name = ""))

        // Then:
        // 1. CrossRef Gone
        val refs = battleDao.getAllBattleComboTagCrossRefs()
        assertTrue(refs.isEmpty())

        // 2. Combo still exists
        val combo = battleDao.getBattleComboWithTags(comboId)
        assertNotNull(combo)
        assertEquals(0, combo?.tags?.size)
    }

    @Test
    fun cannotInsertCrossRefWithInvalidIds() = runBlocking {
        // Given: IDs that don't exist in DB
        val invalidRef = BattleComboTagCrossRef("bad-combo-id", "bad-moveTag-id")

        // When / Then
        try {
            battleDao.link(invalidRef)
            fail("Should have thrown SQLiteConstraintException")
        } catch (_: SQLiteConstraintException) {
            // Expected behavior: Foreign Key constraint failed
        }
    }
}
