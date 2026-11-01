package com.example.ms_compras.repository;

import com.example.ms_compras.model.CarritoItem;

import java.util.List;
import java.util.Optional;

public interface ComprasRepository {
    void guardarBoletos(int idEvento, int boletosDisponibles);
    Optional<Integer> obtenerBoletosDisponibles(int idEvento);
    void decrementarBoletos(int idEvento, int cantidad);
    void incrementarBoletos(int idEvento, int cantidad);

    Optional<Integer> obtenerCantidadEnCarrito(int idUsuario, int idEvento);
    void agregarAlCarrito(int idUsuario, int idEvento, int cantidad);
    void actualizarCarrito(int idUsuario, int idEvento, int cantidad);
    void eliminarEventoDelCarrito(int idUsuario, int idEvento);
    void vaciarCarrito(int idUsuario);
    List<CarritoItem> obtenerCarritoPorUsuario(int idUsuario);
}
