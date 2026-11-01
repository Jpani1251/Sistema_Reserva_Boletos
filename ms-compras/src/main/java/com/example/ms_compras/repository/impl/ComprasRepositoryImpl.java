package com.example.ms_compras.repository.impl;

import com.example.ms_compras.model.CarritoItem;
import com.example.ms_compras.repository.ComprasRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class ComprasRepositoryImpl implements ComprasRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<CarritoItem> carritoRowMapper = (rs, rowNum) -> {
        CarritoItem item = new CarritoItem();
        item.setIdUsuario(rs.getInt("id_usuario"));
        item.setIdEvento(rs.getInt("id_evento"));
        item.setCantidadBoletos(rs.getInt("cantidad_boletos"));
        return item;
    };

    @Override
    public void guardarBoletos(int idEvento, int boletosDisponibles) {
        String sql = "INSERT INTO boletos (id_evento, boletos_disponibles) VALUES (?, ?)";
        jdbcTemplate.update(sql, idEvento, boletosDisponibles);
    }

    @Override
    public Optional<Integer> obtenerBoletosDisponibles(int idEvento) {
        String sql = "SELECT boletos_disponibles FROM boletos WHERE id_evento = ?";
        List<Map<String, Object>> dispList = jdbcTemplate.queryForList(sql, idEvento);
        if (dispList.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(((Number) dispList.get(0).get("boletos_disponibles")).intValue());
    }

    @Override
    public void decrementarBoletos(int idEvento, int cantidad) {
        String sql = "UPDATE boletos SET boletos_disponibles = boletos_disponibles - ? WHERE id_evento = ?";
        jdbcTemplate.update(sql, cantidad, idEvento);
    }

    @Override
    public void incrementarBoletos(int idEvento, int cantidad) {
        String sql = "UPDATE boletos SET boletos_disponibles = boletos_disponibles + ? WHERE id_evento = ?";
        jdbcTemplate.update(sql, cantidad, idEvento);
    }

    @Override
    public Optional<Integer> obtenerCantidadEnCarrito(int idUsuario, int idEvento) {
        String sql = "SELECT cantidad_boletos FROM carrito_compra WHERE id_usuario = ? AND id_evento = ?";
        List<Map<String, Object>> carrito = jdbcTemplate.queryForList(sql, idUsuario, idEvento);
        if (carrito.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(((Number) carrito.get(0).get("cantidad_boletos")).intValue());
    }

    @Override
    public void agregarAlCarrito(int idUsuario, int idEvento, int cantidad) {
        String sql = "INSERT INTO carrito_compra (id_usuario, id_evento, cantidad_boletos) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, idUsuario, idEvento, cantidad);
    }

    @Override
    public void actualizarCarrito(int idUsuario, int idEvento, int cantidad) {
        String sql = "UPDATE carrito_compra SET cantidad_boletos = cantidad_boletos + ? WHERE id_usuario = ? AND id_evento = ?";
        jdbcTemplate.update(sql, cantidad, idUsuario, idEvento);
    }

    @Override
    public void eliminarEventoDelCarrito(int idUsuario, int idEvento) {
        String sql = "DELETE FROM carrito_compra WHERE id_usuario = ? AND id_evento = ?";
        jdbcTemplate.update(sql, idUsuario, idEvento);
    }

    @Override
    public void vaciarCarrito(int idUsuario) {
        String sql = "DELETE FROM carrito_compra WHERE id_usuario = ?";
        jdbcTemplate.update(sql, idUsuario);
    }

    @Override
    public List<CarritoItem> obtenerCarritoPorUsuario(int idUsuario) {
        String sql = "SELECT id_usuario, id_evento, cantidad_boletos FROM carrito_compra WHERE id_usuario = ?";
        return jdbcTemplate.query(sql, carritoRowMapper, idUsuario);
    }
}
