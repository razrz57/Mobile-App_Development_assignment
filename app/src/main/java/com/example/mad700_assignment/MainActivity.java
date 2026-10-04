package com.example.mad700_assignment;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.widget.Button;
import android.widget.ListView;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private PantryAdapter pantryAdapter;

    private final ExecutorService databaseExecutor =
            Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        Button recipeButton = findViewById(R.id.buttonRecipe);

        recipeButton.setOnClickListener(view -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    Recipe.class
            );
            startActivity(intent);
        });

        ListView pantryList = findViewById(R.id.listPantry);

        pantryAdapter = new PantryAdapter(this);
        pantryList.setAdapter(pantryAdapter);

        // opens the selected ingredient for editing
        pantryList.setOnItemClickListener((parent, view, position, rowId) -> {
            PantryItem ingredient = pantryAdapter.getItem(position);

            if (ingredient == null) {
                return;
            }

            Intent intent = new Intent(
                    MainActivity.this,
                    AddIngredientsActivity.class
            );
            // ensures that the data in the activity screen is pulled from the db and not blank
            intent.putExtra("ingredient_id", ingredient.getId());
            intent.putExtra("ingredient_name", ingredient.getName());
            intent.putExtra("ingredient_quantity", ingredient.getQuantity());
            intent.putExtra("ingredient_unit", ingredient.getUnit());

            startActivity(intent);
        });

        // find the button from the main screen layout
        Button addIngredientButton = findViewById(R.id.buttonAddIngredient);

        // opens the add ingredient screen when the user taps the button
        addIngredientButton.setOnClickListener(view -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    AddIngredientsActivity.class
            );
            startActivity(intent);
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadPantry();
    }

    private void loadPantry() {
        databaseExecutor.execute(() -> {
            try (PantryDatabaseHelper databaseHelper =
                         new PantryDatabaseHelper(getApplicationContext())) {

                List<PantryItem> ingredients =
                        databaseHelper.getAllIngredients();

                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    pantryAdapter.clear();
                    pantryAdapter.addAll(ingredients);
                    pantryAdapter.notifyDataSetChanged();
                });

            }
        });
    }

    @Override
    protected void onDestroy() {
        databaseExecutor.shutdown();
        super.onDestroy();
    }
}