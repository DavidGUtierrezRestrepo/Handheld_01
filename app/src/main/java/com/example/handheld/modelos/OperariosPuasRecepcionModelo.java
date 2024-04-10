package com.example.handheld.modelos;

public class OperariosPuasRecepcionModelo {
    String nit;
    String nombre;

    public OperariosPuasRecepcionModelo() {
    }

    public OperariosPuasRecepcionModelo(String nit, String nombre) {
        this.nit = nit;
        this.nombre = nombre;
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
}
