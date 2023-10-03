package com.example.handheld.modelos;


public class Persona {
    private String codigo;

    private double diametro;
private double velocidad_bobina;
private int bobina;
private double longitud;

private double traccion;

private double zaba;


    public Persona(){}

    public Persona(String codigo, double diametro, double velocidad_bobina, int bobina, double longitud, double traccion, double zaba) {
        this.codigo = codigo;
        this.diametro = diametro;
        this.velocidad_bobina = velocidad_bobina;
        this.bobina = bobina;
        this.longitud = longitud;
        this.traccion = traccion;
        this.zaba = zaba;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public double getDiametro() {
        return diametro;
    }

    public void setDiametro(double diametro) {
        this.diametro = diametro;
    }

    public double getVelocidad_bobina() {
        return velocidad_bobina;
    }

    public void setVelocidad_bobina(double velocidad_bobina) {
        this.velocidad_bobina = velocidad_bobina;
    }

    public int getBobina() {
        return bobina;
    }

    public void setBobina(int bobina) {
        this.bobina = bobina;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    public double getTraccion() {
        return traccion;
    }

    public void setTraccion(double traccion) {
        this.traccion = traccion;
    }

    public double getZaba() {
        return zaba;
    }

    public void setZaba(double zaba) {
        this.zaba = zaba;
    }
}
