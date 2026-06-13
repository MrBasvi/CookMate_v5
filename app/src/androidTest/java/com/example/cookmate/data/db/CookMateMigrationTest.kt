package com.example.cookmate.data.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CookMateMigrationTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @After
    fun tearDown() {
        context.deleteDatabase(TEST_DB)
    }

    @Test
    fun migrate1To4_preservesFavouriteMeals() {
        createVersion1Database().use { db ->
            db.insertFavouriteMeal("fav-1", "Saved Soup")
        }

        openMigratedDatabase().use { db ->
            assertEquals("Saved Soup", db.singleString("SELECT strMeal FROM favourite_meals WHERE idMeal = 'fav-1'"))
            assertEquals("Saved Soup", db.singleString("SELECT strMeal FROM cached_meals WHERE idMeal = 'fav-1'"))
        }
    }

    @Test
    fun migrate2To4_createsShoppingListTableAndKeepsHistory() {
        createVersion2Database().use { db ->
            db.insertCachedMeal("cached-1", "Cached Pasta")
            db.execSQL("INSERT INTO recent_meals (mealId, viewedAt) VALUES ('cached-1', 10)")
        }

        openMigratedDatabase().use { db ->
            assertEquals("cached-1", db.singleString("SELECT mealId FROM recent_meals"))
            assertEquals(0, db.singleLong("SELECT COUNT(*) FROM shopping_list_items"))
        }
    }

    @Test
    fun migrate3To4_keepsUserDataReferencingFavouriteMealOnly() {
        createVersion3Database().use { db ->
            db.insertFavouriteMeal("fav-2", "Favourite Curry")
            db.execSQL("INSERT INTO recent_meals (mealId, viewedAt) VALUES ('fav-2', 20)")
            db.execSQL("INSERT INTO meal_notes (mealId, noteText, rating, updatedAt) VALUES ('fav-2', 'Good', 5, 30)")
            db.execSQL("INSERT INTO meal_collections (collectionId, title, description, isPinned, createdAt, updatedAt) VALUES (1, 'Dinner', '', 0, 1, 1)")
            db.execSQL("INSERT INTO collection_meals (collectionId, mealId, addedAt) VALUES (1, 'fav-2', 40)")
        }

        openMigratedDatabase().use { db ->
            assertEquals("Favourite Curry", db.singleString("SELECT strMeal FROM cached_meals WHERE idMeal = 'fav-2'"))
            assertEquals("fav-2", db.singleString("SELECT mealId FROM recent_meals"))
            assertEquals("Good", db.singleString("SELECT noteText FROM meal_notes WHERE mealId = 'fav-2'"))
            assertEquals("fav-2", db.singleString("SELECT mealId FROM collection_meals WHERE collectionId = 1"))
        }
    }

    private fun createVersion1Database(): SQLiteDatabase {
        return openRawDatabase().apply {
            createVersion1Schema()
            setVersion(1)
        }
    }

    private fun createVersion2Database(): SQLiteDatabase {
        return openRawDatabase().apply {
            createVersion2Schema()
            setVersion(2)
        }
    }

    private fun createVersion3Database(): SQLiteDatabase {
        return openRawDatabase().apply {
            createVersion3Schema()
            setVersion(3)
        }
    }

    private fun openRawDatabase(): SQLiteDatabase {
        context.deleteDatabase(TEST_DB)
        return SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath(TEST_DB), null)
    }

    private fun openMigratedDatabase(): SQLiteDatabase {
        val roomDb = Room.databaseBuilder(context, CookMateDatabase::class.java, TEST_DB)
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
            .build()
        roomDb.openHelper.writableDatabase.close()
        roomDb.close()
        return SQLiteDatabase.openDatabase(context.getDatabasePath(TEST_DB).path, null, SQLiteDatabase.OPEN_READONLY)
    }

    private fun SQLiteDatabase.createVersion1Schema() {
        execSQL(
            """
            CREATE TABLE cached_meals (
                idMeal TEXT NOT NULL PRIMARY KEY,
                strMeal TEXT NOT NULL,
                strCategory TEXT NOT NULL,
                strArea TEXT NOT NULL,
                strInstructions TEXT NOT NULL,
                strMealThumb TEXT NOT NULL,
                ingredients TEXT NOT NULL,
                cachedAt INTEGER NOT NULL
            )
            """.trimIndent()
        )
        execSQL(
            """
            CREATE TABLE favourite_meals (
                idMeal TEXT NOT NULL PRIMARY KEY,
                strMeal TEXT NOT NULL,
                strCategory TEXT NOT NULL,
                strArea TEXT NOT NULL,
                strInstructions TEXT NOT NULL,
                strMealThumb TEXT NOT NULL,
                ingredients TEXT NOT NULL,
                addedAt INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }

    private fun SQLiteDatabase.createVersion2Schema() {
        createVersion1Schema()
        execSQL("ALTER TABLE cached_meals ADD COLUMN lastSyncedAt INTEGER NOT NULL DEFAULT 0")
        execSQL(
            """
            CREATE TABLE meal_collections (
                collectionId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                title TEXT NOT NULL,
                description TEXT NOT NULL,
                isPinned INTEGER NOT NULL,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL
            )
            """.trimIndent()
        )
        execSQL("CREATE TABLE recent_meals (mealId TEXT NOT NULL PRIMARY KEY, viewedAt INTEGER NOT NULL)")
        execSQL("CREATE TABLE meal_notes (mealId TEXT NOT NULL PRIMARY KEY, noteText TEXT NOT NULL, rating INTEGER NOT NULL, updatedAt INTEGER NOT NULL)")
        execSQL("CREATE TABLE collection_meals (collectionId INTEGER NOT NULL, mealId TEXT NOT NULL, addedAt INTEGER NOT NULL, PRIMARY KEY(collectionId, mealId))")
    }

    private fun SQLiteDatabase.createVersion3Schema() {
        createVersion2Schema()
        execSQL(
            """
            CREATE TABLE shopping_list_items (
                itemId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                ingredientName TEXT NOT NULL,
                measure TEXT NOT NULL,
                quantityCount INTEGER NOT NULL,
                isChecked INTEGER NOT NULL,
                addedAt INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }

    private fun SQLiteDatabase.insertCachedMeal(id: String, title: String) {
        execSQL(
            """
            INSERT INTO cached_meals (
                idMeal, strMeal, strCategory, strArea, strInstructions, strMealThumb, ingredients, cachedAt, lastSyncedAt
            ) VALUES (?, ?, 'Main', 'US', 'Cook', '', '[]', 1, 1)
            """.trimIndent(),
            arrayOf(id, title)
        )
    }

    private fun SQLiteDatabase.insertFavouriteMeal(id: String, title: String) {
        execSQL(
            """
            INSERT INTO favourite_meals (
                idMeal, strMeal, strCategory, strArea, strInstructions, strMealThumb, ingredients, addedAt
            ) VALUES (?, ?, 'Main', 'US', 'Cook', '', '[]', 1)
            """.trimIndent(),
            arrayOf(id, title)
        )
    }

    private fun SQLiteDatabase.singleString(sql: String): String {
        rawQuery(sql, emptyArray()).use { cursor ->
            cursor.moveToFirst()
            return cursor.getString(0)
        }
    }

    private fun SQLiteDatabase.singleLong(sql: String): Long {
        rawQuery(sql, emptyArray()).use { cursor ->
            cursor.moveToFirst()
            return cursor.getLong(0)
        }
    }

    private companion object {
        const val TEST_DB = "cookmate-migration-test"
    }
}
