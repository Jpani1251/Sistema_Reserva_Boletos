package com.example.ms_lugares.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Lugar {
    @JsonProperty("id_lugar")
    private Integer idLugar;
    private String nombre;
    private String direccion;

    public Lugar() {
    }

    public Lugar(Integer idLugar, String nombre, String direccion) {
        this.idLugar = idLugar;
        this.nombre = nombre;
        this.direccion = direccion;
    }

    public Integer getIdLugar() {
        return idLugar;
    }

    public void setIdLugar(Integer idLugar) {
        this.idLugar = idLugar;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
}
