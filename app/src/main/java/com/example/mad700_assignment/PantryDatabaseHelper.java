package com.example.mad700_assignment;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;
import java.util.ArrayList;
import java.util.List;

public class PantryDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "food_saver.db";
    private static final int DATABASE_VERSION = 3;
    public static final String TABLE_PANTRY = "pantry_items";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_QUANTITY = "quantity";
    public static final String COLUMN_UNIT = "unit";
    public static final String TABLE_RECIPES = "recipes";
    public static final String TABLE_RECIPE_INGREDIENTS = "recipe_ingredients";

    // configures the database file and its version
    public PantryDatabaseHelper(Context context) {
        super(
                context.getApplicationContext(),
                DATABASE_NAME,
                null,
                DATABASE_VERSION
        );
    }

    // runs when this database is first created.
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createPantryTable =
                "CREATE TABLE " + TABLE_PANTRY + " (" +
                        COLUMN_ID + " INTEGER PRIMARY KEY, " +
                        COLUMN_NAME + " TEXT NOT NULL " +
                        "CHECK(length(trim(name)) > 0), " +
                        COLUMN_QUANTITY + " REAL NOT NULL " +
                        "CHECK(quantity > 0), " +
                        COLUMN_UNIT + " TEXT NOT NULL " +
                        "CHECK(unit IN ('g', 'kg', 'ml', 'L', 'unit'))" +
                        ")";

        db.execSQL(createPantryTable);
        createRecipeTables(db);
        addDhalCurry(db);
    }

    // runs when DATABASE_VERSION increases
    @Override
    public void onUpgrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion
    ) {

        if (oldVersion < 2) {
            // add recipe tables while preserving existing ingredients
            createRecipeTables(db);
        }

        if (oldVersion < 3) {
            addDhalCurry(db);
        }
    }

    // insert one ingredient and return its database ID
    public long addIngredient(
            String name,
            double quantity,
            String unit
    ) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Ingredient name is required"
            );
        }

        if (Double.isNaN(quantity)
                || Double.isInfinite(quantity)
                || quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than 0"
            );
        }

        if (!("g".equals(unit)
                || "kg".equals(unit)
                || "ml".equals(unit)
                || "L".equals(unit)
                || "unit".equals(unit))) {
            throw new IllegalArgumentException("Invalid unit");
        }

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name.trim());
        values.put(COLUMN_QUANTITY, quantity);
        values.put(COLUMN_UNIT, unit);

        return db.insertOrThrow(TABLE_PANTRY, null, values);
    }

    // reads all pantry ingredients and sorts them alphabetically using ASC
    // this method getss called from the background thread and not the main UI thread
    public List<PantryItem> getAllIngredients() {
        List<PantryItem> ingredients = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        String[] columns = {
                COLUMN_ID,
                COLUMN_NAME,
                COLUMN_QUANTITY,
                COLUMN_UNIT
        };

        try (Cursor cursor = db.query(
                TABLE_PANTRY,
                columns,
                null,
                null,
                null,
                null,
                COLUMN_NAME + " COLLATE NOCASE ASC, " + COLUMN_ID + " ASC"
        )) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(
                        cursor.getColumnIndexOrThrow(COLUMN_ID)
                );

                String name = cursor.getString(
                        cursor.getColumnIndexOrThrow(COLUMN_NAME)
                );

                double quantity = cursor.getDouble(
                        cursor.getColumnIndexOrThrow(COLUMN_QUANTITY)
                );

                String unit = cursor.getString(
                        cursor.getColumnIndexOrThrow(COLUMN_UNIT)
                );

                ingredients.add(
                        new PantryItem(id, name, quantity, unit)
                );
            }
        }

        return ingredients;
    }

    // update the pantry record by its db ID
    public int updateIngredient(
            long id,
            String name,
            double quantity,
            String unit
    ) {
        if (id <= 0) {
            throw new IllegalArgumentException("Invalid ingredient ID");
        }

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Ingredient name is required");
        }

        if (Double.isNaN(quantity)
                || Double.isInfinite(quantity)
                || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        if (!("g".equals(unit)
                || "kg".equals(unit)
                || "ml".equals(unit)
                || "L".equals(unit)
                || "unit".equals(unit))) {
            throw new IllegalArgumentException("Invalid unit");
        }

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name.trim());
        values.put(COLUMN_QUANTITY, quantity);
        values.put(COLUMN_UNIT, unit);

        return db.update(
                TABLE_PANTRY,
                values,
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)}
        );
    }
    // deletes the ingredient matching the selected ID
    public int deleteIngredient(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Invalid ingredient ID");
        }

        SQLiteDatabase db = getWritableDatabase();

        return db.delete(
                TABLE_PANTRY,
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)}
        );
    }

    private void createRecipeTables(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + TABLE_RECIPES + " (" +
                        "_id INTEGER PRIMARY KEY, " +
                        "name TEXT NOT NULL, " +
                        "method TEXT NOT NULL" +
                        ")"
        );

        db.execSQL(
                "CREATE TABLE " + TABLE_RECIPE_INGREDIENTS + " (" +
                        "_id INTEGER PRIMARY KEY, " +
                        "recipe_id INTEGER NOT NULL, " +
                        "ingredient_name TEXT NOT NULL, " +
                        "quantity REAL NOT NULL CHECK(quantity > 0), " +
                        "unit TEXT NOT NULL " +
                        "CHECK(unit IN ('g', 'kg', 'ml', 'L', 'unit')), " +
                        "FOREIGN KEY(recipe_id) REFERENCES " +
                        TABLE_RECIPES + "(_id) ON DELETE CASCADE" +
                        ")"
        );

        db.execSQL(
                "CREATE INDEX index_recipe_ingredients_recipe_id ON " +
                        TABLE_RECIPE_INGREDIENTS + "(recipe_id)"
        );
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }
    // adds the dhal curry recipe to the db
    private void addDhalCurry(SQLiteDatabase db) {
        ContentValues recipe = new ContentValues();
        recipe.put("name", "Dhal Curry");
        recipe.put(
                "method",
                "Preheat pot at medium heat for 3 minutes"
                        + "Add chillies and curry leaf"
                        + "Simmer for 5 minutes"
                        + "Add the dhal"
                        + "Stir for 5 minutes until medium consistency"
                        + "Remove from heat and serve hot/warm"
                        + "Remember to turn off your stove"
        );

        long recipeId = db.insertOrThrow(
                TABLE_RECIPES,
                null,
                recipe
        );

        // adds the first ingredient required
        ContentValues dhal = new ContentValues();
        dhal.put("recipe_id", recipeId);
        dhal.put("ingredient_name", "canned dhal");
        dhal.put("quantity", 1);
        dhal.put("unit", "unit");

        db.insertOrThrow(TABLE_RECIPE_INGREDIENTS, null, dhal);

        // adds the second ingredient required
        ContentValues chillies = new ContentValues();
        chillies.put("recipe_id", recipeId);
        chillies.put("ingredient_name", "chilli");
        chillies.put("quantity", 2);
        chillies.put("unit", "unit");

        db.insertOrThrow(TABLE_RECIPE_INGREDIENTS, null, chillies);

        // adds the third ingredient required
        ContentValues curryLeaf = new ContentValues();
        curryLeaf.put("recipe_id", recipeId);
        curryLeaf.put("ingredient_name", "curry leaf");
        curryLeaf.put("quantity", 1);
        curryLeaf.put("unit", "unit");

        db.insertOrThrow(TABLE_RECIPE_INGREDIENTS, null, curryLeaf);
    }
}