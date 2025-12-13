package com.princelumpy.breakvault.data

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.princelumpy.breakvault.data.dao.MoveDao
import com.princelumpy.breakvault.data.dao.SavedComboDao
import com.princelumpy.breakvault.data.model.move.Move
import com.princelumpy.breakvault.data.model.move.MoveTag
import com.princelumpy.breakvault.data.model.move.MoveTagCrossRef
import com.princelumpy.breakvault.data.model.savedcombo.SavedCombo
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PracticeDaoTest {

    private lateinit var db: AppDB
    private lateinit var moveDao: MoveDao
    private lateinit var savedComboDao: SavedComboDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, AppDB::class.java
        ).build()
        moveDao = db.moveDao()
        savedComboDao = db.savedComboDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndGetMoveWithTags() = runBlocking {
        // Given
        val move = Move("m1", "Windmill")
        val moveTag = MoveTag("t1", "Power")

        // When
        moveDao.insertMove(move)
        moveDao.insertMoveTag(moveTag)
        moveDao.link(MoveTagCrossRef(move.id, moveTag.id))

        // Then
        val loaded = moveDao.getMoveWithTags("m1")
        assertNotNull(loaded)
        assertEquals("Windmill", loaded?.move?.name)
        assertEquals(1, loaded?.moveTags?.size)
        assertEquals("Power", loaded?.moveTags?.first()?.name)
    }

    @Test
    fun insertAndGetSavedCombo() = runBlocking {
        // Given
        val combo = SavedCombo(
            id = "c1",
            name = "My Combo",
            moves = listOf("Move1", "Move2")
        )

        // When
        savedComboDao.insertSavedCombo(combo)

        // Then
        val loaded = savedComboDao.getSavedComboById("c1")
        assertNotNull(loaded)
        assertEquals("My Combo", loaded?.name)
        assertEquals(2, loaded?.moves?.size)
        assertEquals("Move1", loaded?.moves?.get(0))
    }

    @Test
    fun deleteMoveCascadesToCrossRef() = runBlocking {
        // Given
        val move = Move("m1", "Delete Me")
        val moveTag = MoveTag("t1", "MoveTag")
        moveDao.insertMove(move)
        moveDao.insertMoveTag(moveTag)
        moveDao.link(MoveTagCrossRef(move.id, moveTag.id))

        // When
        moveDao.deleteMove(move)

        // Then
        val refs = moveDao.getAllMoveTagCrossRefsList()
        assertTrue(refs.isEmpty()) // Should be empty due to CASCADE
    }

    @Test
    fun deleteTagRemovesTagFromMoves() = runBlocking {
        // Given: A move with two moveTags (Power, Style)
        val move = Move("m1", "Halo")
        val moveTag1 = MoveTag("t1", "Power")
        val moveTag2 = MoveTag("t2", "Style")

        moveDao.insertMove(move)
        moveDao.insertMoveTag(moveTag1)
        moveDao.insertMoveTag(moveTag2)

        moveDao.link(MoveTagCrossRef(move.id, moveTag1.id))
        moveDao.link(MoveTagCrossRef(move.id, moveTag2.id))

        // Verify setup
        val initialLoad = moveDao.getMoveWithTags("m1")
        assertEquals(2, initialLoad?.moveTags?.size)

        // When: We delete one moveTag (Power)
        moveDao.deleteTagCompletely(moveTag1)

        // Then:
        // 1. The Move should still exist
        val loadedMove = moveDao.getMoveWithTags("m1")
        assertNotNull(loadedMove)

        // 2. The Move should only have 1 moveTag left (Style)
        assertEquals(1, loadedMove?.moveTags?.size)
        assertEquals("Style", loadedMove?.moveTags?.first()?.name)

        // 3. The CrossRef for the deleted moveTag should be gone
        val allRefs = moveDao.getAllMoveTagCrossRefsList()
        assertEquals(1, allRefs.size)
        assertEquals("t2", allRefs.first().tagId)
    }

    // --- Referential Integrity Tests ---

    @Test
    fun deleteMoveDoesNotBreakSavedCombo() = runBlocking {
        // Given: A move "Windmill" and a SavedCombo using it
        val move = Move("m1", "Windmill")
        moveDao.insertMove(move)

        val savedCombo = SavedCombo(
            id = "c1",
            name = "Windmill Combo",
            moves = listOf("Windmill", "Flare")
        )
        savedComboDao.insertSavedCombo(savedCombo)

        // When: We delete the Move "Windmill"
        moveDao.deleteMove(move)

        // Then: The SavedCombo should STILL exist (loose coupling)
        val loadedCombo = savedComboDao.getSavedComboById("c1")
        assertNotNull(loadedCombo)
        assertEquals("Windmill", loadedCombo?.moves?.get(0))
    }

    @Test
    fun cannotLinkInvalidMoveOrTag() = runBlocking {
        // Given: IDs that don't exist
        val badRef = MoveTagCrossRef("bad-move", "bad-moveTag")

        // When / Then
        try {
            moveDao.link(badRef)
            fail("Should have thrown SQLiteConstraintException")
        } catch (_: SQLiteConstraintException) {
            // Expected: FK constraint failed
        }
    }
}
