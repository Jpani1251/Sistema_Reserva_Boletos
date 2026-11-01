package com.example.ms_usuarios.repository;

import com.example.ms_usuarios.model.Usuario;
import java.util.Optional;

public interface UsuarioRepository {
    void guardar(Usuario usuario);
    Optional<Usuario> buscarPorEmailYPassword(String email, String password);
    Optional<Usuario> buscarPorIdYToken(Integer idUsuario, String token);
    void actualizarToken(Integer idUsuario, String token);
}
