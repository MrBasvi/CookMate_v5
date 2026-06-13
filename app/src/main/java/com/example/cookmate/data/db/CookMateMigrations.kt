package com.example.cookmate.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.createCoreTablesIfMissing()
        database.addColumnIfMissing(
            tableName = "cached_meals",
            columnName = "lastSyncedAt",
            definition = "INTEGER NOT NULL DEFAULT 0"
        )
        database.rebuildCachedMealsWithoutColumnDefaults()
        database.rebuildFavouriteMealsWithoutColumnDefaults()
        database.createUserDataTablesIfMissing()
        database.ensureUserDataColumns()
        database.rebuildMealCollectionsWithoutColumnDefaults()
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.rebuildFavouriteMealsWithoutColumnDefaults()
        database.createUserDataTablesIfMissing()
        database.ensureUserDataColumns()
        database.rebuildMealCollectionsWithoutColumnDefaults()
        database.createShoppingListTableIfMissing()
        database.rebuildShoppingListWithoutColumnDefaults()
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.createCoreTablesIfMissing()
        database.rebuildCachedMealsWithoutColumnDefaults()
        database.rebuildFavouriteMealsWithoutColumnDefaults()
        database.createUserDataTablesIfMissing()
        database.ensureUserDataColumns()
        database.rebuildMealCollectionsWithoutColumnDefaults()
        database.copyFavouriteMealsIntoCacheIfMissing()
        database.rebuildRecentMealsWithForeignKey()
        database.rebuildMealNotesWithForeignKey()
        database.rebuildCollectionMealsWithForeignKeys()
        database.createShoppingListTableIfMissing()
        database.rebuildShoppingListWithoutColumnDefaults()
    }
}

private fun SupportSQLiteDatabase.createCoreTablesIfMissing() {
    execSQL(
        """
        CREATE TABLE IF NOT EXISTS cached_meals (
            idMeal TEXT NOT NULL PRIMARY KEY,
            strMeal TEXT NOT NULL,
            strCategory TEXT NOT NULL,
            strArea TEXT NOT NULL,
            strInstructions TEXT NOT NULL,
            strMealThumb TEXT NOT NULL,
            ingredients TEXT NOT NULL,
            cachedAt INTEGER NOT NULL,
            lastSyncedAt INTEGER NOT NULL
        )
        """.trimIndent()
    )
    execSQL(
        """
        CREATE TABLE IF NOT EXISTS favourite_meals (
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

private fun SupportSQLiteDatabase.createUserDataTablesIfMissing() {
    execSQL(
        """
        CREATE TABLE IF NOT EXISTS meal_collections (
            collectionId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            title TEXT NOT NULL,
            description TEXT NOT NULL,
            isPinned INTEGER NOT NULL,
            createdAt INTEGER NOT NULL,
            updatedAt INTEGER NOT NULL
        )
        """.trimIndent()
    )
    execSQL(
        """
        CREATE TABLE IF NOT EXISTS recent_meals (
            mealId TEXT NOT NULL PRIMARY KEY,
            viewedAt INTEGER NOT NULL
        )
        """.trimIndent()
    )
    execSQL(
        """
        CREATE TABLE IF NOT EXISTS meal_notes (
            mealId TEXT NOT NULL PRIMARY KEY,
            noteText TEXT NOT NULL,
            rating INTEGER NOT NULL,
            updatedAt INTEGER NOT NULL
        )
        """.trimIndent()
    )
    execSQL(
        """
        CREATE TABLE IF NOT EXISTS collection_meals (
            collectionId INTEGER NOT NULL,
            mealId TEXT NOT NULL,
            addedAt INTEGER NOT NULL,
            PRIMARY KEY(collectionId, mealId)
        )
        """.trimIndent()
    )
}

private fun SupportSQLiteDatabase.createShoppingListTableIfMissing() {
    execSQL(
        """
        CREATE TABLE IF NOT EXISTS shopping_list_items (
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

private fun SupportSQLiteDatabase.ensureUserDataColumns() {
    addColumnIfMissing("recent_meals", "viewedAt", "INTEGER NOT NULL DEFAULT 0")
    addColumnIfMissing("meal_notes", "updatedAt", "INTEGER NOT NULL DEFAULT 0")
    addColumnIfMissing("collection_meals", "addedAt", "INTEGER NOT NULL DEFAULT 0")
    addColumnIfMissing("meal_collections", "description", "TEXT NOT NULL DEFAULT ''")
    addColumnIfMissing("meal_collections", "isPinned", "INTEGER NOT NULL DEFAULT 0")
    addColumnIfMissing("meal_collections", "createdAt", "INTEGER NOT NULL DEFAULT 0")
    addColumnIfMissing("meal_collections", "updatedAt", "INTEGER NOT NULL DEFAULT 0")
}

private fun SupportSQLiteDatabase.copyFavouriteMealsIntoCacheIfMissing() {
    if (!hasTable("favourite_meals") || !hasTable("cached_meals")) return

    execSQL(
        """
        INSERT OR IGNORE INTO cached_meals (
            idMeal,
            strMeal,
            strCategory,
            strArea,
            strInstructions,
            strMealThumb,
            ingredients,
            cachedAt,
            lastSyncedAt
        )
        SELECT
            idMeal,
            strMeal,
            strCategory,
            strArea,
            strInstructions,
            strMealThumb,
            ingredients,
            addedAt,
            addedAt
        FROM favourite_meals
        """.trimIndent()
    )
}

private fun SupportSQLiteDatabase.rebuildCachedMealsWithoutColumnDefaults() {
    if (!hasTable("cached_meals")) return

    execSQL("DROP TABLE IF EXISTS cached_meals_new")
    execSQL(
        """
        CREATE TABLE IF NOT EXISTS cached_meals_new (
            idMeal TEXT NOT NULL PRIMARY KEY,
            strMeal TEXT NOT NULL,
            strCategory TEXT NOT NULL,
            strArea TEXT NOT NULL,
            strInstructions TEXT NOT NULL,
            strMealThumb TEXT NOT NULL,
            ingredients TEXT NOT NULL,
            cachedAt INTEGER NOT NULL,
            lastSyncedAt INTEGER NOT NULL
        )
        """.trimIndent()
    )
    execSQL(
        """
        INSERT OR REPLACE INTO cached_meals_new (
            idMeal,
            strMeal,
            strCategory,
            strArea,
            strInstructions,
            strMealThumb,
            ingredients,
            cachedAt,
            lastSyncedAt
        )
        SELECT
            idMeal,
            strMeal,
            strCategory,
            strArea,
            strInstructions,
            strMealThumb,
            ingredients,
            cachedAt,
            lastSyncedAt
        FROM cached_meals
        """.trimIndent()
    )
    execSQL("DROP TABLE cached_meals")
    execSQL("ALTER TABLE cached_meals_new RENAME TO cached_meals")
}

private fun SupportSQLiteDatabase.rebuildFavouriteMealsWithoutColumnDefaults() {
    if (!hasTable("favourite_meals")) return

    execSQL("DROP TABLE IF EXISTS favourite_meals_new")
    execSQL(
        """
        CREATE TABLE IF NOT EXISTS favourite_meals_new (
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
    execSQL(
        """
        INSERT OR REPLACE INTO favourite_meals_new (
            idMeal,
            strMeal,
            strCategory,
            strArea,
            strInstructions,
            strMealThumb,
            ingredients,
            addedAt
        )
        SELECT
            idMeal,
            strMeal,
            strCategory,
            strArea,
            strInstructions,
            strMealThumb,
            ingredients,
            addedAt
        FROM favourite_meals
        """.trimIndent()
    )
    execSQL("DROP TABLE favourite_meals")
    execSQL("ALTER TABLE favourite_meals_new RENAME TO favourite_meals")
}

private fun SupportSQLiteDatabase.rebuildShoppingListWithoutColumnDefaults() {
    if (!hasTable("shopping_list_items")) return

    execSQL("DROP TABLE IF EXISTS shopping_list_items_new")
    execSQL(
        """
        CREATE TABLE IF NOT EXISTS shopping_list_items_new (
            itemId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            ingredientName TEXT NOT NULL,
            measure TEXT NOT NULL,
            quantityCount INTEGER NOT NULL,
            isChecked INTEGER NOT NULL,
            addedAt INTEGER NOT NULL
        )
        """.trimIndent()
    )
    execSQL(
        """
        INSERT OR REPLACE INTO shopping_list_items_new (
            itemId,
            ingredientName,
            measure,
            quantityCount,
            isChecked,
            addedAt
        )
        SELECT
            itemId,
            ingredientName,
            measure,
            quantityCount,
            isChecked,
            addedAt
        FROM shopping_list_items
        """.trimIndent()
    )
    execSQL("DROP TABLE shopping_list_items")
    execSQL("ALTER TABLE shopping_list_items_new RENAME TO shopping_list_items")
}

private fun SupportSQLiteDatabase.rebuildMealCollectionsWithoutColumnDefaults() {
    if (!hasTable("meal_collections")) return

    execSQL("DROP TABLE IF EXISTS meal_collections_new")
    execSQL(
        """
        CREATE TABLE IF NOT EXISTS meal_collections_new (
            collectionId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            title TEXT NOT NULL,
            description TEXT NOT NULL,
            isPinned INTEGER NOT NULL,
            createdAt INTEGER NOT NULL,
            updatedAt INTEGER NOT NULL
        )
        """.trimIndent()
    )
    execSQL(
        """
        INSERT OR REPLACE INTO meal_collections_new (
            collectionId,
            title,
            description,
            isPinned,
            createdAt,
            updatedAt
        )
        SELECT
            collectionId,
            title,
            description,
            isPinned,
            createdAt,
            updatedAt
        FROM meal_collections
        """.trimIndent()
    )
    execSQL("DROP TABLE meal_collections")
    execSQL("ALTER TABLE meal_collections_new RENAME TO meal_collections")
}

private fun SupportSQLiteDatabase.rebuildRecentMealsWithForeignKey() {
    execSQL("DROP TABLE IF EXISTS recent_meals_new")
    execSQL(
        """
        CREATE TABLE IF NOT EXISTS recent_meals_new (
            mealId TEXT NOT NULL PRIMARY KEY,
            viewedAt INTEGER NOT NULL,
            FOREIGN KEY(mealId) REFERENCES cached_meals(idMeal) ON DELETE CASCADE
        )
        """.trimIndent()
    )
    if (hasTable("recent_meals")) {
        execSQL(
            """
            INSERT OR REPLACE INTO recent_meals_new (mealId, viewedAt)
            SELECT mealId, viewedAt
            FROM recent_meals
            WHERE EXISTS (SELECT 1 FROM cached_meals WHERE cached_meals.idMeal = recent_meals.mealId)
            """.trimIndent()
        )
        execSQL("DROP TABLE recent_meals")
    }
    execSQL("ALTER TABLE recent_meals_new RENAME TO recent_meals")
    execSQL("CREATE INDEX IF NOT EXISTS index_recent_meals_mealId ON recent_meals(mealId)")
}

private fun SupportSQLiteDatabase.rebuildMealNotesWithForeignKey() {
    execSQL("DROP TABLE IF EXISTS meal_notes_new")
    execSQL(
        """
        CREATE TABLE IF NOT EXISTS meal_notes_new (
            mealId TEXT NOT NULL PRIMARY KEY,
            noteText TEXT NOT NULL,
            rating INTEGER NOT NULL,
            updatedAt INTEGER NOT NULL,
            FOREIGN KEY(mealId) REFERENCES cached_meals(idMeal) ON DELETE CASCADE
        )
        """.trimIndent()
    )
    if (hasTable("meal_notes")) {
        execSQL(
            """
            INSERT OR REPLACE INTO meal_notes_new (mealId, noteText, rating, updatedAt)
            SELECT mealId, noteText, rating, updatedAt
            FROM meal_notes
            WHERE EXISTS (SELECT 1 FROM cached_meals WHERE cached_meals.idMeal = meal_notes.mealId)
            """.trimIndent()
        )
        execSQL("DROP TABLE meal_notes")
    }
    execSQL("ALTER TABLE meal_notes_new RENAME TO meal_notes")
    execSQL("CREATE INDEX IF NOT EXISTS index_meal_notes_mealId ON meal_notes(mealId)")
}

private fun SupportSQLiteDatabase.rebuildCollectionMealsWithForeignKeys() {
    execSQL("DROP TABLE IF EXISTS collection_meals_new")
    execSQL(
        """
        CREATE TABLE IF NOT EXISTS collection_meals_new (
            collectionId INTEGER NOT NULL,
            mealId TEXT NOT NULL,
            addedAt INTEGER NOT NULL,
            PRIMARY KEY(collectionId, mealId),
            FOREIGN KEY(collectionId) REFERENCES meal_collections(collectionId) ON DELETE CASCADE,
            FOREIGN KEY(mealId) REFERENCES cached_meals(idMeal) ON DELETE CASCADE
        )
        """.trimIndent()
    )
    if (hasTable("collection_meals")) {
        execSQL(
            """
            INSERT OR REPLACE INTO collection_meals_new (collectionId, mealId, addedAt)
            SELECT collectionId, mealId, addedAt
            FROM collection_meals
            WHERE EXISTS (
                SELECT 1 FROM meal_collections
                WHERE meal_collections.collectionId = collection_meals.collectionId
            )
            AND EXISTS (
                SELECT 1 FROM cached_meals
                WHERE cached_meals.idMeal = collection_meals.mealId
            )
            """.trimIndent()
        )
        execSQL("DROP TABLE collection_meals")
    }
    execSQL("ALTER TABLE collection_meals_new RENAME TO collection_meals")
    execSQL("CREATE INDEX IF NOT EXISTS index_collection_meals_collectionId ON collection_meals(collectionId)")
    execSQL("CREATE INDEX IF NOT EXISTS index_collection_meals_mealId ON collection_meals(mealId)")
}

private fun SupportSQLiteDatabase.addColumnIfMissing(
    tableName: String,
    columnName: String,
    definition: String
) {
    if (!hasColumn(tableName, columnName)) {
        execSQL("ALTER TABLE $tableName ADD COLUMN $columnName $definition")
    }
}

private fun SupportSQLiteDatabase.hasTable(tableName: String): Boolean {
    query(
        "SELECT name FROM sqlite_master WHERE type = 'table' AND name = ?",
        arrayOf(tableName)
    ).use { cursor ->
        return cursor.moveToFirst()
    }
}

private fun SupportSQLiteDatabase.hasColumn(tableName: String, columnName: String): Boolean {
    query("PRAGMA table_info($tableName)").use { cursor ->
        val nameIndex = cursor.getColumnIndex("name")
        while (cursor.moveToNext()) {
            if (cursor.getString(nameIndex) == columnName) {
                return true
            }
        }
    }
    return false
}
