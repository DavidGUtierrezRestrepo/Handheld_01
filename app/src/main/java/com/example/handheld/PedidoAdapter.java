package com.example.handheld;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.handheld.modelos.PedidoModelo;

import java.util.List;

public class PedidoAdapter extends ArrayAdapter<PedidoModelo> {

    private Context context;
    private int resource;
    private List<PedidoModelo> pedidos;

    public PedidoAdapter(Context context, int resource, List<PedidoModelo> pedidos) {
        super(context, resource, pedidos);
        this.context = context;
        this.resource = resource;
        this.pedidos = pedidos;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(resource, parent, false);
        }

        PedidoModelo pedido = pedidos.get(position);

        TextView textViewNumero = convertView.findViewById(R.id.textView19);
        TextView textViewFecha = convertView.findViewById(R.id.textViewfecha);
        TextView textViewCodigo = convertView.findViewById(R.id.textViewcodigo);
        TextView textViewPendiente = convertView.findViewById(R.id.textViewpendiente);
        TextView textViewDescripcion = convertView.findViewById(R.id.textViewdescripcion);

        if (textViewNumero != null) {
            textViewNumero.setText("Número: " + String.valueOf(pedido.getNumero()));
        }
        textViewFecha.setText("Fecha: " + pedido.getFecha());
        textViewCodigo.setText("Código: " + pedido.getCodigo());
        textViewPendiente.setText("Pendiente: " + pedido.getPendiente());
        textViewDescripcion.setText("Descripción: " + pedido.getDescripcion());

        return convertView;
    }
}

