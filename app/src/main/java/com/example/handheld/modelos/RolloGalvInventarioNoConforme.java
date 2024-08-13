package com.example.handheld.modelos;

public class RolloGalvInventarioNoConforme {
    String codigo;
    String nombre;
    String nro_orden;
    String nro_rollo;
    String tipo_trans;
    String traslado;
    String peso;
    String fecha_hora;
    String no_conforme;

    public RolloGalvInventarioNoConforme(String codigo, String nombre, String nro_orden, String nro_rollo, String tipo_trans, String traslado, String peso, String fecha_hora, String no_conforme) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.nro_orden = nro_orden;
        this.nro_rollo = nro_rollo;
        this.tipo_trans = tipo_trans;
        this.traslado = traslado;
        this.peso = peso;
        this.fecha_hora = fecha_hora;
        this.no_conforme = no_conforme;
    }

    public RolloGalvInventarioNoConforme() {
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
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

    public String getTipo_trans() {
        return tipo_trans;
    }

    public void setTipo_trans(String tipo_trans) {
        this.tipo_trans = tipo_trans;
    }

    public String getTraslado() {
        return traslado;
    }

    public void setTraslado(String traslado) {
        this.traslado = traslado;
    }

    public String getPeso() {
        return peso;
    }

    public void setPeso(String peso) {
        this.peso = peso;
    }

    public String getFecha_hora() {
        return fecha_hora;
    }

    public void setFecha_hora(String fecha_hora) {
        this.fecha_hora = fecha_hora;
    }

    public String getNo_conforme() {
        return no_conforme;
    }

    public void setNo_conforme(String no_conforme) {
        this.no_conforme = no_conforme;
    }
}
