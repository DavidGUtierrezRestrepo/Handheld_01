package com.example.handheld.modelos;

public class RollosRecocidoInven{

    String codigo;
    String nombre;
    String cod_orden_rec;
    String id_detalle_rec;
    String id_rollo_rec;
    String peso;

    public RollosRecocidoInven(String codigo, String nombre, String cod_orden_rec, String id_detalle_rec, String id_rollo_rec, String peso) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.cod_orden_rec = cod_orden_rec;
        this.id_detalle_rec = id_detalle_rec;
        this.id_rollo_rec = id_rollo_rec;
        this.peso = peso;
    }

    public RollosRecocidoInven() {
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

    public String getCod_orden_rec() {
        return cod_orden_rec;
    }

    public void setCod_orden_rec(String cod_orden_rec) {
        this.cod_orden_rec = cod_orden_rec;
    }

    public String getId_detalle_rec() {
        return id_detalle_rec;
    }

    public void setId_detalle_rec(String id_detalle_rec) {
        this.id_detalle_rec = id_detalle_rec;
    }

    public String getId_rollo_rec() {
        return id_rollo_rec;
    }

    public void setId_rollo_rec(String id_rollo_rec) {
        this.id_rollo_rec = id_rollo_rec;
    }

    public String getPeso() {
        return peso;
    }

    public void setPeso(String peso) {
        this.peso = peso;
    }
}
