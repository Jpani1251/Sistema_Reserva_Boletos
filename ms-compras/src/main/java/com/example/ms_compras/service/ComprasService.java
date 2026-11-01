package com.example.ms_compras.service;

import com.example.ms_compras.model.CarritoItem;

import java.util.List;
import java.util.Map;

public interface ComprasService {
    void altaBoletos(Map<String, Object> body);
    void comprarBoletos(Map<String, Object> body);
    void eliminarEventoDelCarrito(Integer idUsuario, Integer idEvento, String token, Map<String, Object> body);
    void vaciarCarrito(Integer idUsuario, String token, Map<String, Object> body);
    List<CarritoItem> obtenerCarrito(Integer idUsuario, String token);
}
