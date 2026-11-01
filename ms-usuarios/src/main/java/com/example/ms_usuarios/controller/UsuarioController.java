package com.example.ms_usuarios.controller;

import com.example.ms_usuarios.dto.AltaUsuarioRequest;
import com.example.ms_usuarios.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    // 1. POST /usuarios/alta_usuario
    @PostMapping("/alta_usuario")
    public ResponseEntity<Map<String, Object>> altaUsuario(@RequestBody Map<String, Object> body) {
        Map<String, Object> respuesta = new HashMap<>();
        try {
            AltaUsuarioRequest request = new AltaUsuarioRequest();
            request.setEmail((String) body.get("email"));
            request.setPassword((String) body.get("password"));
            request.setNombre((String) body.get("nombre"));
            request.setApellidos((String) body.get("apellidos"));
            if (body.get("rol") != null) {
                request.setRol((String) body.get("rol"));
            }

            usuarioService.registrarUsuario(request);
            respuesta.put("mensaje", "OK");
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            respuesta.put("mensaje", "Error al registrar usuario: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
        }
    }

    // 2. GET /usuarios/login
    @GetMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String password,
            @RequestBody(required = false) Map<String, Object> body) {

        Map<String, Object> respuesta = new HashMap<>();
        try {
            if (body != null) {
                if (email == null) email = (String) body.get("email");
                if (password == null) password = (String) body.get("password");
            }

            Map<String, Object> resultado = usuarioService.iniciarSesion(email, password);
            if (resultado == null) {
                respuesta.put("mensaje", "Acceso denegado");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
            }

            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            respuesta.put("mensaje", "Error en login: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
        }
    }

    // 3. GET /usuarios/verifica_acceso
    @GetMapping("/verifica_acceso")
    public ResponseEntity<Map<String, Object>> verificaAcceso(
            @RequestParam(required = false) Integer id_usuario,
            @RequestParam(required = false) String token,
            @RequestBody(required = false) Map<String, Object> body) {

        Map<String, Object> respuesta = new HashMap<>();
        try {
            if (body != null) {
                if (id_usuario == null && body.get("id_usuario") != null) {
                    id_usuario = Integer.parseInt(body.get("id_usuario").toString());
                }
                if (token == null) token = (String) body.get("token");
            }

            Map<String, Object> resultado = usuarioService.verificarAcceso(id_usuario, token);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            respuesta.put("mensaje", "Error al verificar acceso: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
        }
    }
}
