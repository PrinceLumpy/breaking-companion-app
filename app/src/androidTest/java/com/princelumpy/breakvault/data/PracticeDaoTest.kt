package com.princelumpy.breakvault.data

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
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
    private lateinit var moveTagDao: MoveTagDao
    private lateinit var savedComboDao: SavedComboDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, AppDB::class.java
        ).build()
        moveTagDao = db.moveTagDao()
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
        val tag = Tag("t1", "Power")
        
        // When
        moveTagDao.addMove(move)
        moveTagDao.addTag(tag)
        moveTagDao.link(MoveTagCrossRef(move.id, tag.id))

        // Then
        val loaded = moveTagDao.getMoveWithTagsById("m1")
        assertNotNull(loaded)
        assertEquals("Windmill", loaded?.move?.name)
        assertEquals(1, loaded?.tags?.size)
        assertEquals("Power", loaded?.tags?.first()?.name)
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
        val tag = Tag("t1", "Tag")
        moveTagDao.addMove(move)
        moveTagDao.addTag(tag)
        moveTagDao.link(MoveTagCrossRef(move.id, tag.id))

        // When
        moveTagDao.deleteMove(move)

        // Then
        val refs = moveTagDao.getAllMoveTagCrossRefsList()
        assertTrue(refs.isEmpty()) // Should be empty due to CASCADE
    }

    @Test
    fun deleteTagRemovesTagFromMoves() = runBlocking {
        // Given: A move with two tags (Power, Style)
        val move = Move("m1", "Halo")
        val tag1 = Tag("t1", "Power")
        val tag2 = Tag("t2", "Style")
        
        moveTagDao.addMove(move)
        moveTagDao.addTag(tag1)
        moveTagDao.addTag(tag2)
        
        moveTagDao.link(MoveTagCrossRef(move.id, tag1.id))
        moveTagDao.link(MoveTagCrossRef(move.id, tag2.id))

        // Verify setup
        val initialLoad = moveTagDao.getMoveWithTagsById("m1")
        assertEquals(2, initialLoad?.tags?.size)

        // When: We delete one tag (Power)
        moveTagDao.deleteTagCompletely(tag1)

        // Then:
        // 1. The Move should still exist
        val loadedMove = moveTagDao.getMoveWithTagsById("m1")
        assertNotNull(loadedMove)
        
        // 2. The Move should only have 1 tag left (Style)
        assertEquals(1, loadedMove?.tags?.size)
        assertEquals("Style", loadedMove?.tags?.first()?.name)
        
        // 3. The CrossRef for the deleted tag should be gone
        val allRefs = moveTagDao.getAllMoveTagCrossRefsList()
        assertEquals(1, allRefs.size)
        assertEquals("t2", allRefs.first().tagId)
    }

    // --- Referential Integrity Tests ---

    @Test
    fun deleteMoveDoesNotBreakSavedCombo() = runBlocking {
        // Given: A move "Windmill" and a SavedCombo using it
        val move = Move("m1", "Windmill")
        moveTagDao.addMove(move)
        
        val savedCombo = SavedCombo(
            id = "c1",
            name = "Windmill Combo",
            moves = listOf("Windmill", "Flare")
        )
        savedComboDao.insertSavedCombo(savedCombo)

        // When: We delete the Move "Windmill"
        moveTagDao.deleteMove(move)

        // Then: The SavedCombo should STILL exist (loose coupling)
        val loadedCombo = savedComboDao.getSavedComboById("c1")
        assertNotNull(loadedCombo)
        assertEquals("Windmill", loadedCombo?.moves?.get(0)) 
    }

    @Test
    fun cannotLinkInvalidMoveOrTag() = runBlocking {
        // Given: IDs that don't exist
        val badRef = MoveTagCrossRef("bad-move", "bad-tag")

        // When / Then
        try {
            moveTagDao.link(badRef)
            fail("Should have thrown SQLiteConstraintException")
        } catch (_: SQLiteConstraintException) {
            // Expected: FK constraint failed
        }
    }
}
