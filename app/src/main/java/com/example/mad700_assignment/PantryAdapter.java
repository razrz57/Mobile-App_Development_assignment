// this class will construct how the ingredients appear on the list

package com.example.mad700_assignment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.text.NumberFormat;
import java.util.ArrayList;

public class PantryAdapter extends ArrayAdapter<PantryItem> {

    public PantryAdapter(Context context) {
        super(context, 0, new ArrayList<PantryItem>());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;

        // attempts to reuse an existing row when possible.
        if (row == null) {
            row = LayoutInflater.from(getContext()).inflate(
                    android.R.layout.simple_list_item_2,
                    parent,
                    false
            );
        }

        PantryItem ingredient = getItem(position);

        TextView nameText = row.findViewById(android.R.id.text1);
        TextView quantityText = row.findViewById(android.R.id.text2);

        if (ingredient != null) {
            nameText.setText(ingredient.getName());

            String amount = NumberFormat.getNumberInstance()
                    .format(ingredient.getQuantity());

            quantityText.setText(amount + " " + ingredient.getUnit());
        } else {
            nameText.setText("");
            quantityText.setText("");
        }

        return row;
    }
}