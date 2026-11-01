package com.example.ms_eventos.service;

import java.util.List;
import java.util.Map;

public interface EventoService {
    void altaEvento(Map<String, Object> body);
    List<Map<String, Object>> consultarEventos(String palabra, Integer idUsuario, String token);
}
