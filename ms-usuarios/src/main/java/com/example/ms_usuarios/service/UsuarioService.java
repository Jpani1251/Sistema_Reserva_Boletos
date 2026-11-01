package com.example.ms_usuarios.service;

import com.example.ms_usuarios.dto.AltaUsuarioRequest;
import com.example.ms_usuarios.model.Usuario;

import java.util.Map;
import java.util.Optional;

public interface UsuarioService {
    void registrarUsuario(AltaUsuarioRequest request);
    Map<String, Object> iniciarSesion(String email, String password);
    Map<String, Object> verificarAcceso(Integer idUsuario, String token);
}
