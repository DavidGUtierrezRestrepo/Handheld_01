package com.example.handheld.modelos;

public class Galvanizado_muestras_modelo {

   public String codigo;
   public int longitud;
   public double calibre;
   public double diametro;

    public Galvanizado_muestras_modelo(String codigo, int longitud, double calibre, double diametro) {
        this.codigo = codigo;
        this.longitud = longitud;
        this.calibre = calibre;
        this.diametro = diametro;
    }

    public Galvanizado_muestras_modelo( ) {

    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public int getLongitud() {
        return longitud;
    }

    public void setLongitud(int longitud) {
        this.longitud = longitud;
    }

    public double getCalibre() {
        return calibre;
    }

    public void setCalibre(double calibre) {
        this.calibre = calibre;
    }

    public double getDiametro() {
        return diametro;
    }

    public void setDiametro(double diametro) {
        this.diametro = diametro;
    }
}
