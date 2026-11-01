package com.example.ms_eventos.repository;

import com.example.ms_eventos.model.Evento;

import java.util.List;
import java.util.Map;

public interface EventoRepository {
    int guardarEvento(Evento evento);
    void guardarFoto(byte[] foto, int idEvento);
    List<Map<String, Object>> buscarEventosPorNombreODescripcion(String patron);
}
