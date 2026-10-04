package com.example.mad700_assignment;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PantryDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "food_saver.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_PANTRY = "pantry_items";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_QUANTITY = "quantity";
    public static final String COLUMN_UNIT = "unit";

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
    }

    // runs when DATABASE_VERSION increases
    @Override
    public void onUpgrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion
    ) {

        // this will stop unsupported upgrades instead of deleting the data
        throw new IllegalStateException(
                "Database migration required from version "
                        + oldVersion + " to " + newVersion
        );
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
}