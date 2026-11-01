package com.example.ms_compras.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class UsuarioClient {

    @Autowired
    private RestTemplate restTemplate;

    private final String urlUsuarios = System.getenv().getOrDefault("URL_USUARIOS", "http://localhost:8081");

    @SuppressWarnings("unchecked")
    public Map<String, Object> verificarAcceso(Object idUsuario, Object token) {
        String url = urlUsuarios + "/usuarios/verifica_acceso?id_usuario=" + idUsuario + "&token=" + token;
        return restTemplate.getForObject(url, Map.class);
    }
}
