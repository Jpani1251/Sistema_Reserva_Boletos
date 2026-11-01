package com.example.ms_eventos.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class ComprasClient {

    @Autowired
    private RestTemplate restTemplate;

    private final String urlCompras = System.getenv().getOrDefault("URL_COMPRAS", "http://localhost:8084");

    public void darAltaBoletos(Integer idEvento, Integer cantidadBoletos, Object idUsuario, Object token) {
        Map<String, Object> reqBoletos = new HashMap<>();
        reqBoletos.put("id_evento", idEvento);
        reqBoletos.put("cantidad_boletos", cantidadBoletos);
        reqBoletos.put("id_usuario", idUsuario);
        reqBoletos.put("token", token);

        restTemplate.postForObject(urlCompras + "/compras/alta_boletos", reqBoletos, Map.class);
    }
}
