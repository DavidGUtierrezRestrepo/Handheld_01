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
import com.example.handheld.modelos.PedidoModelo;

import java.util.List;

public class listpedidoAdapter extends ArrayAdapter<PedidoModelo> {

    private final List<PedidoModelo> mlist;
    private final Context mContext;
    private final int resourceLayout;

    public listpedidoAdapter(@NonNull Context context, int resource, List<PedidoModelo> objects) {
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

        PedidoModelo modelo = mlist.get(position);

        TextView textonumero = view.findViewById(R.id.txtnumero);
        textonumero.setText(modelo.getNumero().toString());

        TextView textocodigo = view.findViewById(R.id.txtPeso);
        textocodigo.setText(modelo.getCodigo());

        TextView textofecha = view.findViewById(R.id.txtNumtrans);
        textofecha.setText(modelo.getFecha());

        TextView textopendiente = view.findViewById(R.id.txtNumimp);
        textopendiente.setText(modelo.getPendiente());

        TextView textodescripcion = view.findViewById(R.id.textViewdescripcion);
        textodescripcion.setText(modelo.getDescripcion());

        return view;
    }
}
