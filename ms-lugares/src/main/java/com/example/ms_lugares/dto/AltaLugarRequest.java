package com.example.ms_lugares.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AltaLugarRequest {
    @JsonProperty("id_usuario")
    private Integer idUsuario;
    private String token;
    private String nombre;
    private String direccion;

    public AltaLugarRequest() {
    }

    public AltaLugarRequest(Integer idUsuario, String token, String nombre, String direccion) {
        this.idUsuario = idUsuario;
        this.token = token;
        this.nombre = nombre;
        this.direccion = direccion;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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
