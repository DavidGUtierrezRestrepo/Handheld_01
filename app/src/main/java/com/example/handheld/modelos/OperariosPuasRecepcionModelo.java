package com.example.handheld.modelos;

public class OperariosPuasRecepcionModelo {
    String nit;
    String nombre;
    String codigo;

    public OperariosPuasRecepcionModelo() {
    }

    public OperariosPuasRecepcionModelo(String nit, String nombre, String codigo) {
        this.nit = nit;
        this.nombre = nombre;
        this.codigo = codigo;
    }

    public String getNit() {
        return nit;
    }

    public void setNit(String nit) {
        this.nit = nit;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }
}
