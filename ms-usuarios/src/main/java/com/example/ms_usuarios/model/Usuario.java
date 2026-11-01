package com.example.ms_usuarios.model;

public class Usuario {
    private Integer idUsuario;
    private String email;
    private String password;
    private String token;
    private String nombre;
    private String apellidos;
    private String rol;

    public Usuario() {
    }

    public Usuario(Integer idUsuario, String email, String password, String token, String nombre, String apellidos, String rol) {
        this.idUsuario = idUsuario;
        this.email = email;
        this.password = password;
        this.token = token;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.rol = rol;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }
}
