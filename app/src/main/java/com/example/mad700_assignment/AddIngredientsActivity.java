package com.example.mad700_assignment;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddIngredientsActivity extends AppCompatActivity {

    // starts the database operations without stalling the functionality on the app
    private final ExecutorService databaseExecutor =
            Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_ingredients);

        // connects the variables to the controls
        EditText nameInput = findViewById(R.id.inputIngredientName);
        EditText quantityInput = findViewById(R.id.inputQuantity);
        Spinner unitSpinner = findViewById(R.id.spinnerUnit);
        Button saveButton = findViewById(R.id.buttonSave);

        // populate the unit dropdown
        String[] units = {"g", "kg", "ml", "L", "unit"};

        // adapter to connect the array of units to the spinner item
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                units
        );

        unitAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        unitSpinner.setAdapter(unitAdapter);

        // validation on save
        saveButton.setOnClickListener(view -> {
            // removes accidental spaces before and after the inputs
            String ingredientName = nameInput.getText().toString().trim();
            String quantityText = quantityInput.getText().toString().trim();

            // clears error from the previous attempt
            nameInput.setError(null);
            quantityInput.setError(null);

            if (ingredientName.isEmpty()) {
                nameInput.setError("Enter an ingredient name");
                nameInput.requestFocus();
                return;
            }

            if (quantityText.isEmpty()) {
                quantityInput.setError("Enter a quantity");
                quantityInput.requestFocus();
                return;
            }

            double quantity;

            try {
                quantity = Double.parseDouble(quantityText);
            } catch (NumberFormatException exception) {
                quantityInput.setError("Enter a number");
                quantityInput.requestFocus();
                return;
            }

            if (Double.isNaN(quantity)
                    || Double.isInfinite(quantity)
                    || quantity <= 0) {
                quantityInput.setError("Input a number greater than 0");
                quantityInput.requestFocus();
                return;
            }

            String unit = unitSpinner.getSelectedItem().toString();

            // stops the user from contionuously adding by tapping the save button while saving

            saveButton.setEnabled(false);

            databaseExecutor.execute(() -> {
                try (PantryDatabaseHelper databaseHelper =
                             new PantryDatabaseHelper(getApplicationContext())) {

                    long ingredientId = databaseHelper.addIngredient(
                            ingredientName,
                            quantity,
                            unit
                    );

                    Log.d("PantrySave", "Saved ingredient ID: " + ingredientId);

                    // to ensure that the UI changes happen on the main thread not background
                    runOnUiThread(() -> {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        Toast.makeText(
                                AddIngredientsActivity.this,
                                "Ingredient saved",
                                Toast.LENGTH_SHORT
                        ).show();

                        finish();
                    });

                } catch (SQLiteException
                         | IllegalArgumentException
                         | IllegalStateException exception) {

                    Log.e("PantrySave", "Could not save ingredient", exception);

                    runOnUiThread(() -> {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        saveButton.setEnabled(true);

                        Toast.makeText(
                                AddIngredientsActivity.this,
                                "Could not save ingredient, try again",
                                Toast.LENGTH_LONG
                        ).show();
                    });
                }
            });
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}