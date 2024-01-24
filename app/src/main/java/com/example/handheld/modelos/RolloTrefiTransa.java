package com.example.handheld.modelos;

public class RolloTrefiTransa {
    String cod_orden;
    String id_detalle;
    String id_rollo;
    String estado;
    String fecha_recepcion;
    String trb1;

    public RolloTrefiTransa() {
    }

    public RolloTrefiTransa(String cod_orden, String id_detalle, String id_rollo, String estado, String fecha_recepcion, String trb1) {
        this.cod_orden = cod_orden;
        this.id_detalle = id_detalle;
        this.id_rollo = id_rollo;
        this.estado = estado;
        this.fecha_recepcion = fecha_recepcion;
        this.trb1 = trb1;
    }

    public String getCod_orden() {
        return cod_orden;
    }

    public void setCod_orden(String cod_orden) {
        this.cod_orden = cod_orden;
    }

    public String getId_detalle() {
        return id_detalle;
    }

    public void setId_detalle(String id_detalle) {
        this.id_detalle = id_detalle;
    }

    public String getId_rollo() {
        return id_rollo;
    }

    public void setId_rollo(String id_rollo) {
        this.id_rollo = id_rollo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getFecha_recepcion() {
        return fecha_recepcion;
    }

    public void setFecha_recepcion(String fecha_recepcion) {
        this.fecha_recepcion = fecha_recepcion;
    }

    public String getTrb1() {
        return trb1;
    }

    public void setTrb1(String trb1) {
        this.trb1 = trb1;
    }
}
