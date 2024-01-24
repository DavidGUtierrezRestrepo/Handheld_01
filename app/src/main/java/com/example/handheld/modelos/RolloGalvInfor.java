package com.example.handheld.modelos;

public class RolloGalvInfor {
    String nro_orden;
    String anulado;
    String id_revision;
    String fecha_revision;
    String revisor;
    String estado;
    String num_transa;
    String fecha_recepcion;
    String entrega;
    String Recibe;

    public RolloGalvInfor() {
    }

    public RolloGalvInfor(String nro_orden, String anulado, String id_revision, String fecha_revision, String revisor, String estado, String num_transa, String fecha_recepcion, String entrega, String recibe) {
        this.nro_orden = nro_orden;
        this.anulado = anulado;
        this.id_revision = id_revision;
        this.fecha_revision = fecha_revision;
        this.revisor = revisor;
        this.estado = estado;
        this.num_transa = num_transa;
        this.fecha_recepcion = fecha_recepcion;
        this.entrega = entrega;
        Recibe = recibe;
    }

    public String getNro_orden() {
        return nro_orden;
    }

    public void setNro_orden(String nro_orden) {
        this.nro_orden = nro_orden;
    }

    public String getAnulado() {
        return anulado;
    }

    public void setAnulado(String anulado) {
        this.anulado = anulado;
    }

    public String getId_revision() {
        return id_revision;
    }

    public void setId_revision(String id_revision) {
        this.id_revision = id_revision;
    }

    public String getFecha_revision() {
        return fecha_revision;
    }

    public void setFecha_revision(String fecha_revision) {
        this.fecha_revision = fecha_revision;
    }

    public String getRevisor() {
        return revisor;
    }

    public void setRevisor(String revisor) {
        this.revisor = revisor;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
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
        return Recibe;
    }

    public void setRecibe(String recibe) {
        Recibe = recibe;
    }
}
