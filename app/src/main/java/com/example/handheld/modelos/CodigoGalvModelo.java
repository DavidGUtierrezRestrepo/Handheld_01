package com.example.handheld.modelos;

public class CodigoGalvModelo {
    String codigo;
    String descripcion;

    String longitud;

    public CodigoGalvModelo(String codigo, String descripcion, String longitud) {
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.longitud = longitud;
    }

    public CodigoGalvModelo(){

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

    public String getLongitud() {
        return longitud;
    }

    public void setLongitud(String longitud) {
        this.longitud = longitud;
    }
}
