package com.example.handheld.atv.holder.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.handheld.R;
import com.example.handheld.modelos.OperariosPuasRecepcionModelo;

import java.util.List;

public class listOperariosPuasRecepcionAdapter extends ArrayAdapter<OperariosPuasRecepcionModelo> {

    private final List<OperariosPuasRecepcionModelo> mlist;
    private final Context mContext;
    private final int resourceLayout;

    public listOperariosPuasRecepcionAdapter(@NonNull Context context, int resource, List<OperariosPuasRecepcionModelo> objects) {
        super(context, resource, objects);
        this.mlist = objects;
        this.mContext = context;
        this.resourceLayout = resource;
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;

        if(view == null)
            view = LayoutInflater.from(mContext).inflate(resourceLayout, null);

        OperariosPuasRecepcionModelo modelo = mlist.get(position);

        TextView textoCedulaOperarioPuas = view.findViewById(R.id.txtDocuOperario);
        textoCedulaOperarioPuas.setText(modelo.getNit());

        TextView textoNombreOperarioPuas = view.findViewById(R.id.txtNomOperario);
        textoNombreOperarioPuas.setText(modelo.getNombre());


        return view;
    }

}
