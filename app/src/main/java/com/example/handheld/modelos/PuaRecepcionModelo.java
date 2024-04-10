package com.example.handheld.modelos;

public class PuaRecepcionModelo {
    String nro_orden;
    String consecutivo_rollo;
    String codigo;
    String descripcion;
    String peso;
    String Color;

    public PuaRecepcionModelo() {
    }

    public PuaRecepcionModelo(String nro_orden, String consecutivo_rollo, String codigo, String descripcion, String peso, String color) {
        this.nro_orden = nro_orden;
        this.consecutivo_rollo = consecutivo_rollo;
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.peso = peso;
        Color = color;
    }

    public String getNro_orden() {
        return nro_orden;
    }

    public void setNro_orden(String nro_orden) {
        this.nro_orden = nro_orden;
    }

    public String getConsecutivo_rollo() {
        return consecutivo_rollo;
    }

    public void setConsecutivo_rollo(String consecutivo_rollo) {
        this.consecutivo_rollo = consecutivo_rollo;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getPeso() {
        return peso;
    }

    public void setPeso(String peso) {
        this.peso = peso;
    }

    public String getColor() {
        return Color;
    }

    public void setColor(String color) {
        Color = color;
    }
}
