package com.example.handheld.modelos;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.handheld.R;

public class CustomSpinnerAdapter extends BaseAdapter {

    private final int[] images;
    private final String[] texts;
    private final LayoutInflater inflater;

    public CustomSpinnerAdapter(Context context, int[] images, String[] texts) {
        this.images = images;
        this.texts = texts;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return images.length;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.spinner_item_image_tornilleria, parent, false);
        }

        ImageView imageView = convertView.findViewById(R.id.imageButton25);
        TextView textView = convertView.findViewById(R.id.textViewImageButton25);

        imageView.setImageResource(images[position]);
        textView.setText(texts[position]);

        return convertView;
    }
}

