package com.example.handheld.modelos;

public class RollosTrefiInvenNo_conforme {

    String codigo;
    String nombre;
    String consecutivo;
    String id_rollo;
    String id_detalle;
    String operario;
    String diametro;
    String materia_prima;
    String colada;
    String traccion;
    String  peso;
    String cod_orden;
    String fecha_hora;
    String cliente;

    String manual;

    String anulado;
    String destino;
    String no_conforme;

    public RollosTrefiInvenNo_conforme(String codigo, String nombre, String consecutivo, String id_rollo, String id_detalle, String operario, String diametro, String materia_prima, String colada, String traccion, String peso, String cod_orden, String fecha_hora, String cliente, String manual, String anulado, String destino, String no_conforme) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.consecutivo = consecutivo;
        this.id_rollo = id_rollo;
        this.id_detalle = id_detalle;
        this.operario = operario;
        this.diametro = diametro;
        this.materia_prima = materia_prima;
        this.colada = colada;
        this.traccion = traccion;
        this.peso = peso;
        this.cod_orden = cod_orden;
        this.fecha_hora = fecha_hora;
        this.cliente = cliente;
        this.manual = manual;
        this.anulado = anulado;
        this.destino = destino;
        this.no_conforme = no_conforme;
    }

    public RollosTrefiInvenNo_conforme() {
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

    public String getConsecutivo() {
        return consecutivo;
    }

    public void setConsecutivo(String consecutivo) {
        this.consecutivo = consecutivo;
    }

    public String getId_rollo() {
        return id_rollo;
    }

    public void setId_rollo(String id_rollo) {
        this.id_rollo = id_rollo;
    }

    public String getId_detalle() {
        return id_detalle;
    }

    public void setId_detalle(String id_detalle) {
        this.id_detalle = id_detalle;
    }

    public String getOperario() {
        return operario;
    }

    public void setOperario(String operario) {
        this.operario = operario;
    }

    public String getDiametro() {
        return diametro;
    }

    public void setDiametro(String diametro) {
        this.diametro = diametro;
    }

    public String getMateria_prima() {
        return materia_prima;
    }

    public void setMateria_prima(String materia_prima) {
        this.materia_prima = materia_prima;
    }

    public String getColada() {
        return colada;
    }

    public void setColada(String colada) {
        this.colada = colada;
    }

    public String getTraccion() {
        return traccion;
    }

    public void setTraccion(String traccion) {
        this.traccion = traccion;
    }

    public String getPeso() {
        return peso;
    }

    public void setPeso(String peso) {
        this.peso = peso;
    }

    public String getCod_orden() {
        return cod_orden;
    }

    public void setCod_orden(String cod_orden) {
        this.cod_orden = cod_orden;
    }

    public String getFecha_hora() {
        return fecha_hora;
    }

    public void setFecha_hora(String fecha_hora) {
        this.fecha_hora = fecha_hora;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public String getManual() {
        return manual;
    }

    public void setManual(String manual) {
        this.manual = manual;
    }

    public String getAnulado() {
        return anulado;
    }

    public void setAnulado(String anulado) {
        this.anulado = anulado;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public String getNo_conforme() {
        return no_conforme;
    }

    public void setNo_conforme(String no_conforme) {
        this.no_conforme = no_conforme;
    }
}