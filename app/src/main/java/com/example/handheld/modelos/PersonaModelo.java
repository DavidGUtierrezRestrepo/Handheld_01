package com.example.handheld.modelos;

public class PersonaModelo {
    String nombres;
    String nit;
    String centro;
    String cargo;

    public PersonaModelo(String nombres, String nit, String centro, String cargo) {
        this.nombres = nombres;
        this.nit = nit;
        this.centro = centro;
        this.cargo = cargo;
    }

    public PersonaModelo() {
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getNit() {
        return nit;
    }

    public void setNit(String nit) {
        this.nit = nit;
    }

    public String getCentro() {
        return centro;
    }

    public void setCentro(String centro) {
        this.centro = centro;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }
}
