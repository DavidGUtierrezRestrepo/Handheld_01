package com.example.handheld.modelos;

public class DatosRecepcionLogistica {
    String num_transa;
    String fecha_recepcion;
    String entrega;
    String recibe;

    public DatosRecepcionLogistica() {
    }

    public DatosRecepcionLogistica(String num_transa, String fecha_recepcion, String entrega, String recibe) {
        this.num_transa = num_transa;
        this.fecha_recepcion = fecha_recepcion;
        this.entrega = entrega;
        this.recibe = recibe;
    }

    public String getNum_transa() {
        return num_transa;
    }

    public void setNum_transa(String num_transa) {
        this.num_transa = num_transa;
    }

    public String getFecha_recepcion() {
        return fecha_recepcion;
    }

    public void setFecha_recepcion(String fecha_recepcion) {
        this.fecha_recepcion = fecha_recepcion;
    }

    public String getEntrega() {
        return entrega;
    }

    public void setEntrega(String entrega) {
        this.entrega = entrega;
    }

    public String getRecibe() {
        return recibe;
    }

    public void setRecibe(String recibe) {
        this.recibe = recibe;
    }
}
