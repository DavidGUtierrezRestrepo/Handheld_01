package com.example.handheld.atv.holder.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.handheld.R;
import com.example.handheld.modelos.DetalleTranMateriaPrimaScaeModelo;

import java.util.List;

public class listTrasMateriaPrimaScaeAdapter extends ArrayAdapter<DetalleTranMateriaPrimaScaeModelo> {

    private final List<DetalleTranMateriaPrimaScaeModelo> mlist;
    private final Context mContext;
    private final int resourceLayout;

    public listTrasMateriaPrimaScaeAdapter(@NonNull Context context, int resource, List<DetalleTranMateriaPrimaScaeModelo> objects) {
        super(context, resource, objects);
        this.mlist = objects;
        this.mContext = context;
        this.resourceLayout = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;

        if(view == null)
            view = LayoutInflater.from(mContext).inflate(resourceLayout, null);

        DetalleTranMateriaPrimaScaeModelo modelo = mlist.get(position);

        TextView setConsecutivo = view.findViewById(R.id.SetConsecutivo);
        setConsecutivo.setText(modelo.getNro_orden());

        TextView setIdDetalle = view.findViewById(R.id.SetIdDetalle);
        setIdDetalle.setText(modelo.getId_detalle());

        TextView setNumRollo = view.findViewById(R.id.SetNumRollo);
        setNumRollo.setText(modelo.getNumRollo());

        TextView setCodigo = view.findViewById(R.id.SetCodigo);
        setCodigo.setText(modelo.getCodigo());

        TextView setPeso = view.findViewById(R.id.SetPeso);
        setPeso.setText(modelo.getPeso());

        TextView setTipoTransa = view.findViewById(R.id.SetTipoTransa);
        setTipoTransa.setText(modelo.getTipoTransa());

        TextView setNumTransa = view.findViewById(R.id.SetNumTransa);
        setNumTransa.setText(modelo.getNumTransa());

        return view;
    }
}
