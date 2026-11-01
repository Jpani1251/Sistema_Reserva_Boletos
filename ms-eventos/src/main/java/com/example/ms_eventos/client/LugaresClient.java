package com.example.ms_eventos.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class LugaresClient {

    @Autowired
    private RestTemplate restTemplate;

    private final String urlLugares = System.getenv().getOrDefault("URL_LUGARES", "http://localhost:8082");

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> obtenerLugares() {
        try {
            List<Map<String, Object>> lista = restTemplate.getForObject(
                    urlLugares + "/lugares/obtiene_lugares", List.class);
            return lista != null ? lista : Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
