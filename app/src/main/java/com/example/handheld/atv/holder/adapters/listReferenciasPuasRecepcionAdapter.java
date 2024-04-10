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
import com.example.handheld.modelos.ReferenciasPuasRecepcionModelo;

import java.util.List;

public class listReferenciasPuasRecepcionAdapter extends ArrayAdapter<ReferenciasPuasRecepcionModelo> {

    private final List<ReferenciasPuasRecepcionModelo> mlist;
    private final Context mContext;
    private final int resourceLayout;

    public listReferenciasPuasRecepcionAdapter(@NonNull Context context, int resource, List<ReferenciasPuasRecepcionModelo> objects) {
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

        ReferenciasPuasRecepcionModelo modelo = mlist.get(position);

        TextView textoCodigoReferencia = view.findViewById(R.id.txtReferencia);
        textoCodigoReferencia.setText(modelo.getCodigo());

        TextView textoDescripcionReferencia = view.findViewById(R.id.txtDescripRefe);
        textoDescripcionReferencia.setText(modelo.getDescripcion());

        TextView textoCantidadReferencia = view.findViewById(R.id.txtCantidad);
        textoCantidadReferencia.setText(modelo.getCantidad());



        return view;
    }
}
