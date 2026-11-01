package com.example.ms_usuarios.dto;

public class AltaUsuarioRequest {
    private String email;
    private String password;
    private String nombre;
    private String apellidos;
    private String rol;

    public AltaUsuarioRequest() {
    }

    public AltaUsuarioRequest(String email, String password, String nombre, String apellidos, String rol) {
        this.email = email;
        this.password = password;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.rol = rol;
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
        return rol != null ? rol : "cliente";
    }

    public void setRol(String rol) {
        this.rol = rol;
    }
}
