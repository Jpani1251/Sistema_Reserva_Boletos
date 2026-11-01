package com.example.ms_usuarios.repository.impl;

import com.example.ms_usuarios.model.Usuario;
import com.example.ms_usuarios.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UsuarioRepositoryImpl implements UsuarioRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<Usuario> usuarioRowMapper = (rs, rowNum) -> {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(rs.getInt("id_usuario"));
        usuario.setEmail(rs.getString("email"));
        usuario.setPassword(rs.getString("password"));
        usuario.setToken(rs.getString("token"));
        usuario.setNombre(rs.getString("nombre"));
        usuario.setApellidos(rs.getString("apellidos"));
        usuario.setRol(rs.getString("rol"));
        return usuario;
    };

    @Override
    public void guardar(Usuario usuario) {
        String sql = "INSERT INTO usuarios (email, password, nombre, apellidos, rol) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                usuario.getEmail(),
                usuario.getPassword(),
                usuario.getNombre(),
                usuario.getApellidos(),
                usuario.getRol() != null ? usuario.getRol() : "cliente");
    }

    @Override
    public Optional<Usuario> buscarPorEmailYPassword(String email, String password) {
        String sql = "SELECT id_usuario, email, password, token, nombre, apellidos, rol FROM usuarios WHERE email = ? AND password = ?";
        List<Usuario> resultados = jdbcTemplate.query(sql, usuarioRowMapper, email, password);
        return resultados.stream().findFirst();
    }

    @Override
    public Optional<Usuario> buscarPorIdYToken(Integer idUsuario, String token) {
        String sql = "SELECT id_usuario, email, password, token, nombre, apellidos, rol FROM usuarios WHERE id_usuario = ? AND token = ?";
        List<Usuario> resultados = jdbcTemplate.query(sql, usuarioRowMapper, idUsuario, token);
        return resultados.stream().findFirst();
    }

    @Override
    public void actualizarToken(Integer idUsuario, String token) {
        String sql = "UPDATE usuarios SET token = ? WHERE id_usuario = ?";
        jdbcTemplate.update(sql, token, idUsuario);
    }
}
