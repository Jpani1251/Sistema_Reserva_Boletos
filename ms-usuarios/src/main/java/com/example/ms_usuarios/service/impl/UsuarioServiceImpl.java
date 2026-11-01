package com.example.ms_usuarios.service.impl;

import com.example.ms_usuarios.dto.AltaUsuarioRequest;
import com.example.ms_usuarios.model.Usuario;
import com.example.ms_usuarios.repository.UsuarioRepository;
import com.example.ms_usuarios.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    private static final String CARACTERES = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private String generarToken40() {
        StringBuilder sb = new StringBuilder(40);
        for (int i = 0; i < 40; i++) {
            sb.append(CARACTERES.charAt(RANDOM.nextInt(CARACTERES.length())));
        }
        return sb.toString();
    }

    @Override
    public void registrarUsuario(AltaUsuarioRequest request) {
        if (request.getEmail() == null || request.getPassword() == null
                || request.getNombre() == null || request.getApellidos() == null) {
            throw new IllegalArgumentException("Faltan datos obligatorios del usuario");
        }

        Usuario usuario = new Usuario();
        usuario.setEmail(request.getEmail());
        usuario.setPassword(request.getPassword());
        usuario.setNombre(request.getNombre());
        usuario.setApellidos(request.getApellidos());
        usuario.setRol(request.getRol());

        usuarioRepository.guardar(usuario);
    }

    @Override
    public Map<String, Object> iniciarSesion(String email, String password) {
        Optional<Usuario> usuarioOpt = usuarioRepository.buscarPorEmailYPassword(email, password);
        if (usuarioOpt.isEmpty()) {
            return null;
        }

        Usuario usuario = usuarioOpt.get();
        String nuevoToken = generarToken40();
        usuarioRepository.actualizarToken(usuario.getIdUsuario(), nuevoToken);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("id_usuario", usuario.getIdUsuario());
        respuesta.put("token", nuevoToken);
        respuesta.put("rol", usuario.getRol());
        return respuesta;
    }

    @Override
    public Map<String, Object> verificarAcceso(Integer idUsuario, String token) {
        Map<String, Object> respuesta = new HashMap<>();
        if (idUsuario == null || token == null) {
            throw new IllegalArgumentException("Faltan parámetros id_usuario o token");
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.buscarPorIdYToken(idUsuario, token);
        if (usuarioOpt.isPresent()) {
            respuesta.put("acceso", true);
            respuesta.put("rol", usuarioOpt.get().getRol());
        } else {
            respuesta.put("acceso", false);
        }
        return respuesta;
    }
}
