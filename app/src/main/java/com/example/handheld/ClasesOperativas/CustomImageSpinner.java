package com.example.handheld.ClasesOperativas;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.handheld.R;

import java.util.List;

public class CustomImageSpinner extends ArrayAdapter<String> {
    private Context context;
    private List<String> values;
    private int[] images;

    public CustomImageSpinner(Context context, List<String> values, int[] images) {
        super(context, 0, values); // Llama al constructor de ArrayAdapter con contexto, recurso (0 indica que no se usa recurso de layout) y lista de valores
        this.context = context;
        this.values = values;
        this.images = images;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createViewFromResource(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return createViewFromResource(position, convertView, parent);
    }

    private View createViewFromResource(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.spinner_item_image_tornilleria, parent, false);
        }

        TextView textView = view.findViewById(R.id.textViewImageButton25); // Ensure ID matches
        ImageView imageView = view.findViewById(R.id.imageButton25); // Ensure ID matches

        textView.setText(values.get(position));
        imageView.setImageResource(images[position]);

        return view;
    }
}


