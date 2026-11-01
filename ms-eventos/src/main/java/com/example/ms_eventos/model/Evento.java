package com.example.ms_eventos.model;

import java.sql.Timestamp;

public class Evento {
    private Integer idEvento;
    private String nombre;
    private String descripcion;
    private Integer idLugar;
    private Timestamp fechaHoraEvento;
    private Double precioBoleto;

    public Evento() {
    }

    public Evento(Integer idEvento, String nombre, String descripcion, Integer idLugar, Timestamp fechaHoraEvento, Double precioBoleto) {
        this.idEvento = idEvento;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.idLugar = idLugar;
        this.fechaHoraEvento = fechaHoraEvento;
        this.precioBoleto = precioBoleto;
    }

    public Integer getIdEvento() {
        return idEvento;
    }

    public void setIdEvento(Integer idEvento) {
        this.idEvento = idEvento;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Integer getIdLugar() {
        return idLugar;
    }

    public void setIdLugar(Integer idLugar) {
        this.idLugar = idLugar;
    }

    public Timestamp getFechaHoraEvento() {
        return fechaHoraEvento;
    }

    public void setFechaHoraEvento(Timestamp fechaHoraEvento) {
        this.fechaHoraEvento = fechaHoraEvento;
    }

    public Double getPrecioBoleto() {
        return precioBoleto;
    }

    public void setPrecioBoleto(Double precioBoleto) {
        this.precioBoleto = precioBoleto;
    }
}
