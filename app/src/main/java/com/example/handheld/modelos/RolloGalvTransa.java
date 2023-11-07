package com.example.handheld.modelos;

public class RolloGalvTransa {
    String nro_orden;
    String nro_rollo;
    String referencia;
    String fecha_recepcion;
    String trb1;

    public RolloGalvTransa() {
    }

    public RolloGalvTransa(String nro_orden, String nro_rollo, String referencia, String fecha_recepcion, String trb1) {
        this.nro_orden = nro_orden;
        this.nro_rollo = nro_rollo;
        this.referencia = referencia;
        this.fecha_recepcion = fecha_recepcion;
        this.trb1 = trb1;
    }

    public String getNro_orden() {
        return nro_orden;
    }

    public void setNro_orden(String nro_orden) {
        this.nro_orden = nro_orden;
    }

    public String getNro_rollo() {
        return nro_rollo;
    }

    public void setNro_rollo(String nro_rollo) {
        this.nro_rollo = nro_rollo;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
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
