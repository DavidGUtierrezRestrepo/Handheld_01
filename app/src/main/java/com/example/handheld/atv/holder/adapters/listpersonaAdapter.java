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
import com.example.handheld.modelos.Persona;

import java.util.List;

public class listpersonaAdapter extends ArrayAdapter<Persona> {

    private final List<Persona> mlist;
    private final Context mContext;
    private final int resourceLayout;

    public listpersonaAdapter(@NonNull Context context, int resource, List<Persona> objects) {
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

        Persona modelo = mlist.get(position);

        TextView textonumero = view.findViewById(R.id.codigo);
        textonumero.setText("Codigo: " + modelo.getCodigo().toString());

        TextView textocodigo = view.findViewById(R.id.diametro);
        textocodigo.setText("Diametro: "+modelo.getDiametro());

        TextView textofecha = view.findViewById(R.id.bobina);
        textofecha.setText("Bobina: "+modelo.getBobina());

        TextView textotracion = view.findViewById(R.id.tracion);
        textotracion.setText("Traccion: "+modelo.getTraccion());

        TextView textozaba = view.findViewById(R.id.zaba);
        textozaba.setText("Zaba: "+modelo.getZaba());

        return view;
    }
}
