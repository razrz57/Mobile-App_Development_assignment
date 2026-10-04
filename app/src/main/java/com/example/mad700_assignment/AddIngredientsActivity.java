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

public class AddIngredientsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_ingredients);

        // Connect the variables to the controls
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
            // Remove accidental spaces before and after the input.
            String ingredientName = nameInput.getText().toString().trim();
            String quantityText = quantityInput.getText().toString().trim();

            // Clear errors from the previous attempt.
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

            // Testing string before db connection
            Toast.makeText(
                    AddIngredientsActivity.this,
                    "Valid input: " + ingredientName + " — "
                            + quantity + " " + unit + ". Not saved yet.",
                    Toast.LENGTH_LONG
            ).show();
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}